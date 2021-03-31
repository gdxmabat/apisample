package com.sabadellzurich.olimpo.renewal;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Application configuration.
 * ==========================
 * <p>
 * Configures the Spring components.
 *
 * @author msellers
 */
@Configuration
public class Config implements WebMvcConfigurer {
    @Value("${swagger.desc}")
    private String desc;

    @Value("${swagger.title}")
    private String title;

//    @Override
//    public void addCorsMappings(CorsRegistry reg) {
//        reg.addMapping("/**").allowedMethods("*");
//    }

    /**
     * @return Swagger docket.
     */
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(
                        new ApiInfoBuilder()
                                .title(title)
                                .description(desc)
                                .build()
                )
                .directModelSubstitute(Pageable.class, SwaggerPageable.class)
                .select()
                // All handlers
                .apis(RequestHandlerSelectors.basePackage("com.sabadellzurich.olimpo.renewal"))
                // All endpoints
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public ModelMapper mapper() {
        return new ModelMapper();
    }
}
