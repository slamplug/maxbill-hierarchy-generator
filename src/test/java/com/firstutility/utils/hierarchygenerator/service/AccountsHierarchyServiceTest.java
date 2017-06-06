package com.firstutility.utils.hierarchygenerator.service;

import static com.firstutility.reach.customer.dto.CustomerBuilder.aCustomer;
import static com.firstutility.reach.customer.dto.CustomerRelation.Relationship.LINKED_ACCOUNT;
import static com.firstutility.reach.customer.dto.CustomerRelationBuilder.aCustomerRelation;
import static com.firstutility.reach.customer.dto.CustomerRelationsBuilder.anCustomerRelations;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

import com.firstutility.reach.customer.dto.Customer;
import com.firstutility.reach.customer.dto.CustomerRelations;
import com.firstutility.utils.hierarchygenerator.client.customerservice.CustomerServiceClient;
import com.firstutility.utils.hierarchygenerator.client.maxbill.MaxBillClient;
import com.firstutility.utils.hierarchygenerator.exception.CantProcessAccountsException;
import com.firstutility.utils.hierarchygenerator.model.Accounts;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class AccountsHierarchyServiceTest {

    private AccountsHierarchyService service;

    @Mock
    private MaxBillClient mockMaxBillClient;

    @Mock
    private CustomerServiceClient mockCustomerServiceClient;

    private static final String ENERGY_CUSTOMER_NO = "123456789";

    private static final String ENERGY_CUSTOMER_NO_2 = "123451234";

    private static final String TELCO_CUSTOMER_NO = "987654321";

    private static final String PARENT_CUSTOMER_NO = "11223344";

    @Before
    public void setup() {

        initMocks(this);

        service = new AccountsHierarchyServiceImpl(mockMaxBillClient, mockCustomerServiceClient);
    }

    @Test(expected = CantProcessAccountsException.class)
    public void shouldThrowCantProcessAccountsExceptionWhenEnergyCustomerNotActive() {

        given(mockMaxBillClient.isAccountActive(ENERGY_CUSTOMER_NO)).willReturn(false);
        given(mockMaxBillClient.isAccountActive(TELCO_CUSTOMER_NO)).willReturn(true);

        final Accounts accounts = new Accounts() {{
            setEnergyCustomerNumber(ENERGY_CUSTOMER_NO);
            setTelcoCustomerNumber(TELCO_CUSTOMER_NO);
        }};

        final String parentCustomerNumber = service.createAccountsHierarchy(accounts);

        then(mockMaxBillClient).should(times(1)).isAccountActive(ENERGY_CUSTOMER_NO);
        then(mockMaxBillClient).should(never()).isAccountActive(ENERGY_CUSTOMER_NO);
    }

    @Test(expected = CantProcessAccountsException.class)
    public void shouldThrowCantProcessAccountsExceptionWhenEnergyCustomerActiveAndTelcoNotActive() {

        given(mockMaxBillClient.isAccountActive(ENERGY_CUSTOMER_NO)).willReturn(true);
        given(mockMaxBillClient.isAccountActive(TELCO_CUSTOMER_NO)).willReturn(false);

        final Accounts accounts = new Accounts() {{
            setEnergyCustomerNumber(ENERGY_CUSTOMER_NO);
            setTelcoCustomerNumber(TELCO_CUSTOMER_NO);
        }};

        final String parentCustomerNumber = service.createAccountsHierarchy(accounts);

        then(mockMaxBillClient).should(times(1)).isAccountActive(ENERGY_CUSTOMER_NO);
        then(mockMaxBillClient).should(times(1)).isAccountActive(ENERGY_CUSTOMER_NO);
    }

    @Test
    public void shouldCreateHierarchyAndReturnParentCustomerNumberWhenHierarchyDoesNotExist() {

        given(mockMaxBillClient.isAccountActive(ENERGY_CUSTOMER_NO)).willReturn(true);
        given(mockMaxBillClient.isAccountActive(TELCO_CUSTOMER_NO)).willReturn(true);

        final CustomerRelations emptyCustomerRelations = anCustomerRelations().build();

        given(mockCustomerServiceClient.findCustomerRelations(ENERGY_CUSTOMER_NO))
                .willReturn(emptyCustomerRelations);

        final Customer parentCustomer = aCustomer().withCustomerNo(PARENT_CUSTOMER_NO).build();

        given(mockCustomerServiceClient.createParentCustomer()).willReturn(parentCustomer);

        final CustomerRelations customerRelations = anCustomerRelations()
                .with(aCustomerRelation()
                                .withParentCustomerNo(PARENT_CUSTOMER_NO)
                                .withChildCustomerNo(ENERGY_CUSTOMER_NO)
                                .withRelationship(LINKED_ACCOUNT),
                        aCustomerRelation()
                                .withParentCustomerNo(PARENT_CUSTOMER_NO)
                                .withChildCustomerNo(TELCO_CUSTOMER_NO)
                                .withRelationship(LINKED_ACCOUNT))
                .build();

        given(mockCustomerServiceClient.createCustomerRelations(customerRelations)).willReturn(customerRelations);

        final Accounts accounts = new Accounts() {{
            setEnergyCustomerNumber(ENERGY_CUSTOMER_NO);
            setTelcoCustomerNumber(TELCO_CUSTOMER_NO);
        }};

        final String parentCustomerNumber = service.createAccountsHierarchy(accounts);

        then(mockMaxBillClient).should(times(1)).isAccountActive(ENERGY_CUSTOMER_NO);
        then(mockMaxBillClient).should(times(1)).isAccountActive(ENERGY_CUSTOMER_NO);

        then(mockCustomerServiceClient).should(times(1)).findCustomerRelations(ENERGY_CUSTOMER_NO);
        then(mockCustomerServiceClient).should(times(1)).createParentCustomer();
        then(mockCustomerServiceClient).should(times(1)).createCustomerRelations(customerRelations);

        assertEquals(PARENT_CUSTOMER_NO, parentCustomerNumber);
    }

    @Test
    public void shouldAddToExistingHierarchyAndReturnParentCustomerNumberWhenHierarchyExist() {

        given(mockMaxBillClient.isAccountActive(ENERGY_CUSTOMER_NO)).willReturn(true);
        given(mockMaxBillClient.isAccountActive(TELCO_CUSTOMER_NO)).willReturn(true);

        final CustomerRelations existingCustomerRelations = anCustomerRelations()
                .with(aCustomerRelation()
                                .withParentCustomerNo(PARENT_CUSTOMER_NO)
                                .withChildCustomerNo(ENERGY_CUSTOMER_NO)
                                .withRelationship(LINKED_ACCOUNT),
                        aCustomerRelation()
                                .withParentCustomerNo(PARENT_CUSTOMER_NO)
                                .withChildCustomerNo(ENERGY_CUSTOMER_NO_2)
                                .withRelationship(LINKED_ACCOUNT))
                .build();

        given(mockCustomerServiceClient.findCustomerRelations(ENERGY_CUSTOMER_NO))
                .willReturn(existingCustomerRelations);

        final Customer parentCustomer = aCustomer().withCustomerNo(PARENT_CUSTOMER_NO).build();

        given(mockCustomerServiceClient.createParentCustomer()).willReturn(parentCustomer);

        final CustomerRelations updatedCustomerRelations = anCustomerRelations()
                .with(aCustomerRelation()
                                .withParentCustomerNo(PARENT_CUSTOMER_NO)
                                .withChildCustomerNo(TELCO_CUSTOMER_NO)
                                .withRelationship(LINKED_ACCOUNT))
                .build();

        given(mockCustomerServiceClient.createCustomerRelations(updatedCustomerRelations))
                .willReturn(updatedCustomerRelations);

        final Accounts accounts = new Accounts() {{
            setEnergyCustomerNumber(ENERGY_CUSTOMER_NO);
            setTelcoCustomerNumber(TELCO_CUSTOMER_NO);
        }};

        final String parentCustomerNumber = service.createAccountsHierarchy(accounts);

        then(mockMaxBillClient).should(times(1)).isAccountActive(ENERGY_CUSTOMER_NO);
        then(mockMaxBillClient).should(times(1)).isAccountActive(ENERGY_CUSTOMER_NO);

        then(mockCustomerServiceClient).should(times(1)).findCustomerRelations(ENERGY_CUSTOMER_NO);
        then(mockCustomerServiceClient).should(never()).createParentCustomer();
        then(mockCustomerServiceClient).should(times(1)).createCustomerRelations(updatedCustomerRelations);

        assertEquals(PARENT_CUSTOMER_NO, parentCustomerNumber);
    }
}
