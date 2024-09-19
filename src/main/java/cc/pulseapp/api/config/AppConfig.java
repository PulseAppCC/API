package cc.pulseapp.api.config;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Braydon
 */
@Configuration @Log4j2(topic = "App Config")
public class AppConfig {
    @PostConstruct
    public void init() {
        log.info("App Config initialized");
    }

    @Bean
    public WebMvcConfigurer configureCors() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                log.info("Configuring CORS...");

                // Allow all origins to access the API
                registry.addMapping("*")
                        .allowedOrigins("*") // Allow all origins
                        .allowedMethods("*") // Allow all methods
                        .allowedHeaders("*"); // Allow all headers
            }
        };
    }
}
