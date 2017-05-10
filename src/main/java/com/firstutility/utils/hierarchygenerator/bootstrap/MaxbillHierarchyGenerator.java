package com.firstutility.utils.hierarchygenerator.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = { "com.firstutility.utils.hierarchygenerator" })
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class MaxbillHierarchyGenerator {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MaxbillHierarchyGenerator.class, args);
    }
}
