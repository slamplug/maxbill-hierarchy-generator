package com.firstutility.utils.hierarchygenerator.client.customerservice;

import static com.firstutility.reach.customer.dto.CustomerBuilder.aCustomer;
import static com.firstutility.reach.customer.dto.CustomerRelation.Relationship.LINKED_ACCOUNT;
import static com.firstutility.reach.customer.dto.CustomerRelationBuilder.aCustomerRelation;
import static com.firstutility.reach.customer.dto.CustomerRelationsBuilder.anCustomerRelations;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_XML;

import com.firstutility.reach.customer.dto.Customer;
import com.firstutility.reach.customer.dto.CustomerRelations;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

public class CustomerServiceClientTest {

    private CustomerServiceClientImpl customerServiceClient;

    @Mock
    private RestOperations mockRestOpertations;

    private static final String CUSTOMER_NO = "123456789";

    @Before
    public void setup() {

        initMocks(this);

        customerServiceClient = new CustomerServiceClientImpl(
                "http://localhost", mockRestOpertations);
    }

    @Test
    public void shouldFindEmptyCustomerRelationsWhenNoneExist() {

        final CustomerRelations customerRelations = anCustomerRelations().build();

        final ResponseEntity<CustomerRelations> responseEntity =
                new ResponseEntity<CustomerRelations>(customerRelations, OK);

        given(mockRestOpertations.exchange(
                "http://localhost/v1/customerrelations?customerno=" + CUSTOMER_NO + "&relationship=Linked Account",
                HttpMethod.GET, null, CustomerRelations.class)).willReturn(responseEntity);

        final CustomerRelations result = customerServiceClient.findCustomerRelations(CUSTOMER_NO);

        assertTrue(result.getCustomerRelations().isEmpty());
    }

    @Test
    public void shouldFindNonEmptyCustomerRelationsWhenSomeExist() {

        final CustomerRelations customerRelations = anCustomerRelations()
                .with(aCustomerRelation()
                        .withParentCustomerNo("112233")
                        .withChildCustomerNo("6666")
                        .withRelationship(LINKED_ACCOUNT),
                aCustomerRelation()
                        .withParentCustomerNo("112233")
                        .withChildCustomerNo("7777")
                        .withRelationship(LINKED_ACCOUNT))
                .build();

        final ResponseEntity<CustomerRelations> responseEntity =
                new ResponseEntity<CustomerRelations>(customerRelations, OK);

        given(mockRestOpertations.exchange(
                "http://localhost/v1/customerrelations?customerno=" + CUSTOMER_NO + "&relationship=Linked Account",
                HttpMethod.GET, null, CustomerRelations.class)).willReturn(responseEntity);

        final CustomerRelations result = customerServiceClient.findCustomerRelations(CUSTOMER_NO);

        assertFalse(result.getCustomerRelations().isEmpty());
        assertEquals(2, result.getCustomerRelations().size());
    }

    @Test
    public void shouldCreateParentCustomer() {

        final Customer customer = aCustomer().withCustomerNo("112233").build();

        final ResponseEntity<Customer> responseEntity =
                new ResponseEntity<Customer>(customer, OK);

        given(mockRestOpertations.exchange(
                "http://localhost/v1/parentcustomer",
                HttpMethod.POST, null, Customer.class)).willReturn(responseEntity);

        final Customer result = customerServiceClient.createParentCustomer();

        assertNotNull(result);
    }

    @Test
    public void shouldCreateCustomerRelations() {

        final CustomerRelations customerRelations = anCustomerRelations()
                .with(aCustomerRelation()
                                .withParentCustomerNo("112233")
                                .withChildCustomerNo("6666")
                                .withRelationId("Parent")
                                .withRelationship(LINKED_ACCOUNT),
                        aCustomerRelation()
                                .withParentCustomerNo("112233")
                                .withChildCustomerNo("7777")
                                .withRelationId("Parent")
                                .withRelationship(LINKED_ACCOUNT))
                .build();

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(singletonList(APPLICATION_XML));
        httpHeaders.setContentType(APPLICATION_XML);

        final HttpEntity<CustomerRelations> httpEntity =
                new HttpEntity<CustomerRelations>(customerRelations, httpHeaders);

        final ResponseEntity<CustomerRelations> responseEntity =
                new ResponseEntity<CustomerRelations>(customerRelations, OK);

        given(mockRestOpertations.exchange(
                "http://localhost/v1/customerrelations",
                HttpMethod.POST, httpEntity, CustomerRelations.class)).willReturn(responseEntity);

        final CustomerRelations result = customerServiceClient.createCustomerRelations(customerRelations);

        assertNotNull(result);
    }
}
