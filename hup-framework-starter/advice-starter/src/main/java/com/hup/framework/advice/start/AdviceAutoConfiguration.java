package com.hup.framework.advice.start;

import com.hup.framework.advice.Interceptor.AdviceInterceptor;
import com.hup.framework.advice.method.CommonExceptionAdvice;
import com.hup.framework.advice.method.DefinitionResponseBodyAdvice;
import io.swagger.annotations.Api;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ConditionalOnClass(Docket.class)
public class AdviceAutoConfiguration {

    @Configuration
    @EnableSwagger2
    class SpringFoxConfiguration {

        @Bean
        public Docket docket() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(apiInfo())
                    .select()
                    .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                    .paths(PathSelectors.any())
                    .build();
        }

        private ApiInfo apiInfo() {
            return new ApiInfoBuilder()
                    .title("springboot利用swagger构建api文档")
                    .description("简单优雅的restfun风格，http://blog.csdn.net/saytime")
                    .termsOfServiceUrl("http://blog.csdn.net/saytime")
                    .version("1.0")
                    .build();
        }


    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebMvcConfigurer openApiConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new AdviceInterceptor());
            }
        };
    }

    @Bean
    public DefinitionResponseBodyAdvice getDefinitionResponseBodyAdvice() {
        return new DefinitionResponseBodyAdvice();
    }

    @Bean
    public CommonExceptionAdvice getCommonExceptionAdvice() {
        return new CommonExceptionAdvice();
    }

}
