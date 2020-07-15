package com.ppdai.stargate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Value("${com.ppdai.appId}")
    private String appId;
    @Value("${spring.application.name}")
    private String serviceName;
    @Value("${info.app.version}")
    private String version;

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(serviceName)
                .version(version)
                .extensions(Arrays.asList(new StringVendorExtension("x-appId", appId),
                        new StringVendorExtension("x-serviceName", serviceName)))
                .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ppdai.stargate.controller.api"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

}
