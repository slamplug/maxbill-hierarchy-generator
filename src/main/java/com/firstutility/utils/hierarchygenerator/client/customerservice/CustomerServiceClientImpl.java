package com.firstutility.utils.hierarchygenerator.client.customerservice;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import com.firstutility.reach.customer.dto.Customer;
import com.firstutility.reach.customer.dto.CustomerRelations;
import com.firstutility.utils.hierarchygenerator.exception.CantProcessAccountsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

@Component
public class CustomerServiceClientImpl implements CustomerServiceClient {

    private final RestOperations restOperations;

    private final String customerRelationsServiceUrl;

    private final String parentCustomerServiceUrl;

    @Autowired
    public CustomerServiceClientImpl(@Value("${customer.service.baseurl}") final String customerServiceBaseUrl,
            final RestOperations customerServiceClientRestOperations) {
        this.customerRelationsServiceUrl = customerServiceBaseUrl + "/v1/customerrelations";
        this.parentCustomerServiceUrl = customerServiceBaseUrl + "/v1/parentcustomer";
        this.restOperations = customerServiceClientRestOperations;
    }

    @Override
    public CustomerRelations findCustomerRelations(String customerNumber) {

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("customerno", customerNumber);
        queryParams.add("relationship", "Linked Account");

        final String url = fromUriString(customerRelationsServiceUrl).queryParams(queryParams).build().toUriString();

        return execute(url, GET, null, CustomerRelations.class);
    }

    @Override
    public Customer createParentCustomer() {
        return execute(parentCustomerServiceUrl, POST, null, Customer.class);
    }

    @Override
    public CustomerRelations createCustomerRelations(CustomerRelations customerRelations) {
        return execute(customerRelationsServiceUrl, POST,
                entityWithXmlHeadersAndBody(customerRelations), CustomerRelations.class);
    }

    private <R> R execute(String url, HttpMethod httpMethod, HttpEntity<?> httpEntity, Class<R> responseType) {
        try {
            ResponseEntity<R> response = restOperations.exchange(url, httpMethod, httpEntity, responseType);

            return response.getBody();

        } catch (RestClientException e) {
            throw new CantProcessAccountsException("url: " + url + ", method: " + httpMethod.toString() +
                    ", returned error: " + e.getMessage());
        }
    }

    private static <T> HttpEntity<T> entityWithXmlHeadersAndBody(final T body) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(APPLICATION_XML));

        if (null != body) {
            headers.setContentType(APPLICATION_XML);
            return new HttpEntity<>(body, headers);
        }

        return new HttpEntity<>(headers);
    }
}
