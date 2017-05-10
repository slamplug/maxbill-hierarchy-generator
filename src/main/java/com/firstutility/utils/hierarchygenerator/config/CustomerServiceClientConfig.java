package com.firstutility.utils.hierarchygenerator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.NeverRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class CustomerServiceClientConfig {

    @Bean
    public RestTemplate customerServiceClientRestOperations(
            final ClientHttpRequestFactory customerServiceClientHttpRequestFactory) {
        // Seems like spring-boot does not auto-wire the spring MVC ObjectMapper into rest template
        // so we do this here

        final RestTemplate restTemplate = new RestTemplate(customerServiceClientHttpRequestFactory);
        final List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.add(0, new Jaxb2RootElementHttpMessageConverter());

        return restTemplate;
    }

    @Bean
    public RetryPolicy customerServiceClientRetryPolicy() {
        return new NeverRetryPolicy();
    }

    @Bean
    public RetryOperations customerServiceClientRetryOperations(final RetryPolicy customerServiceClientRetryPolicy) {

        final RetryTemplate template = new RetryTemplate();

        template.setRetryPolicy(customerServiceClientRetryPolicy);

        return template;
    }

    @Bean
    public ClientHttpRequestFactory customerServiceClientHttpRequestFactory(
            @Value("${customer.service.client.connection.timeoutMs}") final Integer connectionTimeoutMs,
            @Value("${customer.service.client.readTimeoutMs}") final Integer readTimeoutMs) {
        final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        factory.setConnectTimeout(connectionTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        return factory;
    }
}
