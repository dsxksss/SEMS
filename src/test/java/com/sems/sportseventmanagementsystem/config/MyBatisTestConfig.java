package com.sems.sportseventmanagementsystem.config;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@TestConfiguration
@MapperScan(basePackages = "com.sems.sportseventmanagementsystem.mapper", sqlSessionFactoryRef = "testSqlSessionFactory")
public class MyBatisTestConfig {
    
    @Bean(name = "testDataSource")
    @Primary
    public DataSource testDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:h2:mem:sems_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .driverClassName("org.h2.Driver")
                .build();
    }
    
    @Bean(name = "testSqlSessionFactory")
    @Primary
    public SqlSessionFactoryBean testSqlSessionFactory(@Qualifier("testDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        
        // 指定具体的entity包，避免扫描到旧版本的model类
        sqlSessionFactoryBean.setTypeAliasesPackage("com.sems.sportseventmanagementsystem.model.entity");
        
        // 指定mapper xml位置
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:mapper/*.xml"));
        
        // 自动转换驼峰命名
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        sqlSessionFactoryBean.setConfiguration(configuration);
        
        return sqlSessionFactoryBean;
    }
} 