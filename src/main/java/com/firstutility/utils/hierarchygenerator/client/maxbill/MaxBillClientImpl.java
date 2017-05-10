package com.firstutility.utils.hierarchygenerator.client.maxbill;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import com.firstutility.reach.customerservice.response.update.dto.Response;
import com.firstutility.utils.hierarchygenerator.exception.CantProcessAccountsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

@Component
public class MaxBillClientImpl implements MaxBillClient {

    private final RestOperations restOperations;

    private final String maxbillFindCustomerUrl;

    @Autowired
    public MaxBillClientImpl(@Value("${maxbill.baseurl}") final String maxbillBaseUrl,
            final RestOperations maxbillClientRestOperations) {
        this.maxbillFindCustomerUrl = maxbillBaseUrl + "/ocs/rest/customers/{customerNo}";
        this.restOperations = maxbillClientRestOperations;
    }

    @Override
    public boolean isAccountActive(String customerNumber) {

        final String url = fromUriString(maxbillFindCustomerUrl).buildAndExpand(customerNumber).toUriString();

        Response response = execute(url, HttpMethod.GET, null, Response.class);

        return response.getOk().getCustomers().get(0).getStatus().equals("Active");
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
}
