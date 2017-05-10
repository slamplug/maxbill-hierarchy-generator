package com.firstutility.utils.hierarchygenerator.service;

import static com.firstutility.reach.customer.dto.CustomerRelation.Relationship.LINKED_ACCOUNT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import com.firstutility.reach.customer.dto.Customer;
import com.firstutility.reach.customer.dto.CustomerRelation;
import com.firstutility.reach.customer.dto.CustomerRelations;
import com.firstutility.utils.hierarchygenerator.client.customerservice.CustomerServiceClient;
import com.firstutility.utils.hierarchygenerator.client.maxbill.MaxBillClient;
import com.firstutility.utils.hierarchygenerator.exception.CantProcessAccountsException;
import com.firstutility.utils.hierarchygenerator.model.Accounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class AccountsHierarchyServiceImpl implements AccountsHierarchyService {

    private static final Logger log = LoggerFactory.getLogger(AccountsHierarchyServiceImpl.class);

    private final MaxBillClient maxBillClient;

    private final CustomerServiceClient customerServiceClient;

    @Autowired
    public AccountsHierarchyServiceImpl(final MaxBillClient maxBillClient,
            final CustomerServiceClient customerServiceClient) {
        this.maxBillClient = maxBillClient;
        this.customerServiceClient = customerServiceClient;
    }

    @Override
    public void createAccountsHierarchy(Accounts accounts) {

        checkAccountActive(accounts.getEnergyCustomerNumber());
        checkAccountActive(accounts.getTelcoCustomerNumber());

        CustomerRelations customerRelations =
                customerServiceClient.findCustomerRelations(accounts.getEnergyCustomerNumber());

        log.info("customer relations :" + customerRelations);

        final String parentCustomerNumber = getOrCreateParentCustomerNumber(customerRelations);

        log.info("parent customer number :" + parentCustomerNumber);

        CustomerRelations hierarchy = createHierarchy(accounts.getTelcoCustomerNumber(),
                accounts.getEnergyCustomerNumber(),
                customerRelations,
                parentCustomerNumber);

        log.info("hierarchy :" + hierarchy);
    }

    private void checkAccountActive(final String customerNumber) {
        if (!maxBillClient.isAccountActive(customerNumber)) {
            throw new CantProcessAccountsException("customerNumber: " + customerNumber + " is not active");
        }
    }

    private String getOrCreateParentCustomerNumber(final CustomerRelations customerRelations) {

        if (hasExistingHierarchy(customerRelations)) {
            return customerRelations.getCustomerRelations().get(0).getParentCustomerNo();
        }

        final Customer parentCustomer = customerServiceClient.createParentCustomer();

        return parentCustomer.getCustomerNo();
    }

    private CustomerRelations createHierarchy(final String telcoCustomerNumber, final String energyCustomerNumber,
            final CustomerRelations customerRelations,
            final String parentCustomerNumber) {

        final CustomerRelations hierarchy =
                getCreateCustomerRelationsRequest(telcoCustomerNumber, energyCustomerNumber, customerRelations,
                        parentCustomerNumber);

        return customerServiceClient.createCustomerRelations(hierarchy);
    }

    private CustomerRelations getCreateCustomerRelationsRequest(final String telcoCustomerNumber,
            final String energyCustomerNumber,
            final CustomerRelations customerRelations,
            final String parentCustomerNumber) {
        if (hasExistingHierarchy(customerRelations)) {
            return anAddNewRelationRequest(telcoCustomerNumber, parentCustomerNumber);
        }

        return aCreateHierarchyRequest(parentCustomerNumber, telcoCustomerNumber, energyCustomerNumber);
    }

    private CustomerRelations anAddNewRelationRequest(final String telcoCustomerNumber, final String parentCustomerNumber) {
        final CustomerRelations customerRelations = new CustomerRelations();

        customerRelations.setCustomerRelations(
                singletonList(createRelation(parentCustomerNumber, telcoCustomerNumber)));

        return customerRelations;
    }

    private CustomerRelations aCreateHierarchyRequest(final String parentCustomerNumber, final String telcoCustomerNumber,
            final String energyCustomerNumber) {
        final CustomerRelations relations = new CustomerRelations();

        //@formatter:off
        relations.setCustomerRelations(asList(
                createRelation(parentCustomerNumber, energyCustomerNumber),
                createRelation(parentCustomerNumber, telcoCustomerNumber)));
        //@formatter:on

        return relations;
    }

    private CustomerRelation createRelation(final String parentCustomerNumber, final String childCustomerNumber) {
        final CustomerRelation relation = new CustomerRelation();

        relation.setChildCustomerNo(childCustomerNumber);
        relation.setParentCustomerNo(parentCustomerNumber);
        relation.setRelationship(LINKED_ACCOUNT);

        return relation;
    }

    private boolean hasExistingHierarchy(final CustomerRelations customerRelations) {
        return !customerRelations.getCustomerRelations().isEmpty();
    }
}
