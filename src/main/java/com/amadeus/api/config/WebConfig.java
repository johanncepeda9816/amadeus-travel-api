package com.amadeus.api.config;

import com.amadeus.api.security.JwtAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    private final SecurityProperties securityProperties;

    public WebConfig(JwtAuthenticationInterceptor jwtAuthenticationInterceptor, SecurityProperties securityProperties) {
        this.jwtAuthenticationInterceptor = jwtAuthenticationInterceptor;
        this.securityProperties = securityProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(securityProperties.getPublicEndpoints().toArray(new String[0]));
    }
}
