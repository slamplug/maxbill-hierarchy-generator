package com.firstutility.utils.hierarchygenerator.client.customerservice;

import com.firstutility.reach.customer.dto.Customer;
import com.firstutility.reach.customer.dto.CustomerRelations;

public interface CustomerServiceClient {

    CustomerRelations findCustomerRelations(final String customerNumber);

    Customer createParentCustomer();

    CustomerRelations createCustomerRelations(CustomerRelations customerRelations);
}
