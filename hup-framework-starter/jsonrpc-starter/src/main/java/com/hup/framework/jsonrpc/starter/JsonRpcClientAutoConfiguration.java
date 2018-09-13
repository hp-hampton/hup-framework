package com.hup.framework.jsonrpc.starter;

import com.hup.framework.jsonrpc.client.JsonRpcClient;
import com.hup.framework.jsonrpc.client.JsonRpcFeignClient;
import com.hup.framework.jsonrpc.client.JsonRpcFeignExecutor;
import com.hup.framework.jsonrpc.client.JsonRpcProxyFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

@Slf4j
@Configuration
@ConditionalOnClass(JsonRpcProxyFactoryBean.class)
public class JsonRpcClientAutoConfiguration implements BeanClassLoaderAware, ResourceLoaderAware, BeanDefinitionRegistryPostProcessor {

    private MetadataReaderFactory metadataReaderFactory;

    private ResourcePatternResolver resourcePatternResolver;

    private ClassLoader beanClassLoader;

    private BeanNameGenerator beanNameGenerator = new DefaultBeanNameGenerator();

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.metadataReaderFactory = new SimpleMetadataReaderFactory(resourceLoader);
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.beanClassLoader = classLoader;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        //
        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            //
            String configurationClassName = beanDefinition.getBeanClassName();
            if (configurationClassName == null) {
                continue;
            }
            //
            Map<JsonRpcClient, Class> jsonRpcClients = getJsonRpcClientAnnotations(configurationClassName);
            if (CollectionUtils.isEmpty(jsonRpcClients)) {
                continue;
            }
            //
            registerJsonRpcProxyFactoryBeans(registry, jsonRpcClients);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // do nothing
    }

    private Map<JsonRpcClient, Class> getJsonRpcClientAnnotations(String configurationClassName) {
        Map<JsonRpcClient, Class> jsonRpcClients = new LinkedHashMap<>();
        //
        try {
            Class configurationClass = ClassUtils.forName(configurationClassName, beanClassLoader);
            //
            EnableFeignClients enableFeignClients = AnnotationUtils.findAnnotation(configurationClass, EnableFeignClients.class);
            if (enableFeignClients != null) {
                Class[] feignClientClasses = enableFeignClients.clients();
                for (Class feignClientClass : feignClientClasses) {
                    JsonRpcClient jsonRpcClient = AnnotationUtils.findAnnotation(feignClientClass, JsonRpcClient.class);
                    if (jsonRpcClient != null && ClassUtils.isAssignable(JsonRpcFeignClient.class, feignClientClass)) {
                        jsonRpcClients.put(jsonRpcClient, feignClientClass);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // ignore exception
            log.error(e.getMessage(), e);
        }
        //
        return jsonRpcClients;
    }

    private Map<Class, String> jsonRpcFeignExecutorDefinitionNameCache = new HashMap<>();

    private void registerJsonRpcProxyFactoryBeans(BeanDefinitionRegistry registry, Map<JsonRpcClient, Class> jsonRpcClients) {
        for (JsonRpcClient jsonRpcClient : jsonRpcClients.keySet()) {
            //
            Class jsonRpcFeignClientClass = jsonRpcClients.get(jsonRpcClient);
            List<Class> serviceInterfaces = getJsonRpcServiceInterfaces(jsonRpcClient);
            //
            String jsonRpcFeignExecutorDefinitionName = jsonRpcFeignExecutorDefinitionNameCache.get(jsonRpcFeignClientClass);
            if (jsonRpcFeignExecutorDefinitionName == null) {
                BeanDefinitionBuilder jsonRpcFeignExecutorDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(JsonRpcFeignExecutor.class)
                        .addPropertyValue("jsonRpcFeignClientClass", jsonRpcFeignClientClass);
                BeanDefinition jsonRpcFeignExecutorDefinition = jsonRpcFeignExecutorDefinitionBuilder.getBeanDefinition();
                jsonRpcFeignExecutorDefinitionName = beanNameGenerator.generateBeanName(jsonRpcFeignExecutorDefinition, registry);
                registry.registerBeanDefinition(jsonRpcFeignExecutorDefinitionName, jsonRpcFeignExecutorDefinition);
                //
                jsonRpcFeignExecutorDefinitionNameCache.put(jsonRpcFeignClientClass, jsonRpcFeignExecutorDefinitionName);
            }
            //
            for (Class serviceInterface : serviceInterfaces) {
                //
                BeanDefinitionBuilder jsonRpcProxyFactoryBeanBuilder = BeanDefinitionBuilder.genericBeanDefinition(JsonRpcProxyFactoryBean.class)
                        .addPropertyValue("serviceInterface", serviceInterface)
                        .addPropertyValue("serviceUrl", jsonRpcClient.basePath())
                        .addPropertyValue("httpInvokerRequestExecutor", new RuntimeBeanReference(jsonRpcFeignExecutorDefinitionName));
                //
                BeanDefinition jsonRpcFactoryBeanDefinition = jsonRpcProxyFactoryBeanBuilder.getBeanDefinition();
                String jsonRpcFactoryBeanDefinitionName = beanNameGenerator.generateBeanName(jsonRpcFactoryBeanDefinition, registry);
                registry.registerBeanDefinition(jsonRpcFactoryBeanDefinitionName, jsonRpcProxyFactoryBeanBuilder.getBeanDefinition());
            }
        }
    }

    private List<Class> getJsonRpcServiceInterfaces(JsonRpcClient jsonRpcClient) {
        List<Class> serviceInterfaces = new ArrayList<>();
        serviceInterfaces.addAll(Arrays.asList(jsonRpcClient.serviceInterfaces()));
        //
        for (String servicePackage : jsonRpcClient.servicePackages()) {
            String scanPattern = "classpath*:" + ClassUtils.convertClassNameToResourcePath(servicePackage) + "/**/*.class";
            try {
                Resource[] resources = resourcePatternResolver.getResources(scanPattern);
                for (Resource resource : resources) {
                    ClassMetadata classMetadata = metadataReaderFactory.getMetadataReader(resource).getClassMetadata();
                    if (!classMetadata.isInterface()) {
                        continue;
                    }
                    //
                    Class serviceInterface = ClassUtils.forName(classMetadata.getClassName(), beanClassLoader);
                    serviceInterfaces.add(serviceInterface);
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
        //
        return serviceInterfaces;
    }

}
