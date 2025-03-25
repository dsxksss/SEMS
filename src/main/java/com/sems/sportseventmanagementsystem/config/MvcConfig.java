package com.sems.sportseventmanagementsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Filter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // 配置跨域请求
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    // 添加字符编码过滤器
    @Bean
    public Filter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }
    
    // 配置消息转换器
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converters.add(converter);
    }
    
    // 添加静态资源处理
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加swagger-ui资源映射
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
                .resourceChain(false);
                
        // 添加H2控制台资源映射
        registry.addResourceHandler("/h2-console/**")
                .addResourceLocations("classpath:/h2-console/");
                
        // 添加静态资源文件夹映射
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
                
        // 添加图片资源映射
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
} 