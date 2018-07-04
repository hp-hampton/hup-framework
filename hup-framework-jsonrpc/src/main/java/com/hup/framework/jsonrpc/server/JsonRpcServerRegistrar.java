package com.hup.framework.jsonrpc.server;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonRpcServerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static String basePath = "/json-rpc";

    private static List<Class> serviceInterfaces = new ArrayList<>();

    private MetadataReaderFactory metadataReaderFactory;

    private ResourcePatternResolver resourcePatternResolver;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.metadataReaderFactory = new SimpleMetadataReaderFactory(resourceLoader);
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(JsonRpcServer.class.getName());
        JsonRpcServerRegistrar.basePath = (String) annotationAttributes.get("basePath");
        JsonRpcServerRegistrar.serviceInterfaces.addAll(Arrays.asList((Class[]) annotationAttributes.get("serviceInterfaces")));
        //
        String[] servicePackages = (String[]) annotationAttributes.get("servicePackages");
        //
        for (String servicePackage : servicePackages) {
            String scanPattern = "classpath*:" + ClassUtils.convertClassNameToResourcePath(servicePackage) + "/**/*.class";
            try {
                Resource[] resources = resourcePatternResolver.getResources(scanPattern);
                for (Resource resource : resources) {
                    ClassMetadata classMetadata = metadataReaderFactory.getMetadataReader(resource).getClassMetadata();
                    if (!classMetadata.isInterface()) {
                        continue;
                    }
                    //
                    Class serviceInterface = ClassUtils.forName(classMetadata.getClassName(), resourcePatternResolver.getClassLoader());
                    //
                    serviceInterfaces.add(serviceInterface);
                }
            } catch (IOException | ClassNotFoundException e) {
                // ignore exception
            }
        }
    }

    public static String getBasePath() {
        return basePath;
    }

    public static List<Class> getServiceInterfaces() {
        return serviceInterfaces;
    }
}
