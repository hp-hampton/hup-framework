package com.hup.framework.openapi.start;

import com.hup.framework.openapi.Interceptor.OpenApiInterceptor;
import com.hup.framework.openapi.method.CommonExceptionAdvice;
import com.hup.framework.openapi.method.DefinitionResponseBodyAdvice;
import io.swagger.annotations.Api;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ConditionalOnClass(Docket.class)
public class OpenApiAutoConfiguration extends WebMvcConfigurerAdapter {

    @Configuration
    class SpringFoxConfiguration extends WebMvcConfigurerAdapter {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(openApiInterceptor());
        }

        private OpenApiInterceptor openApiInterceptor() {
            return new OpenApiInterceptor();
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
