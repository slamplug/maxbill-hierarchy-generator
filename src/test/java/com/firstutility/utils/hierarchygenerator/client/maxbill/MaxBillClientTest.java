package com.firstutility.utils.hierarchygenerator.client.maxbill;

import static com.firstutility.reach.customerservice.response.update.dto.LognetCustomerBuilder.aLognetCustomer;
import static com.firstutility.reach.customerservice.response.update.dto.ResponseBuilder.aResponse;
import static com.firstutility.reach.response.ErrorBuilder.anError;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import com.firstutility.reach.customerservice.response.update.dto.Response;
import com.firstutility.utils.hierarchygenerator.exception.CantProcessAccountsException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

public class MaxBillClientTest {

    private MaxBillClientImpl maxBillClient;

    @Mock
    private RestOperations mockRestOpertations;

    private static final String CUSTOMER_NO = "123456789";

    @Before
    public void setup() {

        initMocks(this);

        maxBillClient = new MaxBillClientImpl("http://localhost", mockRestOpertations);
    }

    @Test
    public void shouldReturnTrueWhenAccountActive() {

        final Response response = aResponse()
                .with(aLognetCustomer().withStatus("Active").build())
                .build();

        final ResponseEntity<Response> responseEntity = new ResponseEntity<Response>(response, OK);

        given(mockRestOpertations.exchange("http://localhost/ocs/rest/customers/" + CUSTOMER_NO,
                HttpMethod.GET, null, Response.class)).willReturn(responseEntity);

        final boolean result = maxBillClient.isAccountActive(CUSTOMER_NO);

        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenAccountActive() {

        final Response response = aResponse()
                .with(aLognetCustomer().withStatus("Closed").build())
                .build();

        final ResponseEntity<Response> responseEntity = new ResponseEntity<Response>(response, OK);

        given(mockRestOpertations.exchange("http://localhost/ocs/rest/customers/" + CUSTOMER_NO,
                HttpMethod.GET, null, Response.class)).willReturn(responseEntity);

        final boolean result = maxBillClient.isAccountActive(CUSTOMER_NO);

        assertFalse(result);
    }

    @Test(expected = CantProcessAccountsException.class)
    public void shouldThrowCantProcessAccountsExceptionWhenAccountNotFound() {

        final Response response = aResponse()
                .with(anError().withCode("404").withDescription("Customers were not found for received criterias."))
                .withId(2)
                .build();

        final ResponseEntity<Response> responseEntity = new ResponseEntity<Response>(response, NOT_FOUND);

        given(mockRestOpertations.exchange("http://localhost/ocs/rest/customers/" + CUSTOMER_NO,
                HttpMethod.GET, null, Response.class)).willReturn(responseEntity);

        maxBillClient.isAccountActive(CUSTOMER_NO);
    }

    @Test(expected = CantProcessAccountsException.class)
    public void shouldThrowCantProcessAccountsExceptionWhenMaxBillReturns500() {

        final RestClientException exception = new RestClientException("an error");

        given(mockRestOpertations.exchange("http://localhost/ocs/rest/customers/" + CUSTOMER_NO,
                HttpMethod.GET, null, Response.class)).willThrow(exception);

        maxBillClient.isAccountActive(CUSTOMER_NO);
    }
}
