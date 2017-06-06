package com.firstutility.utils.hierarchygenerator.processor;

import com.firstutility.utils.hierarchygenerator.model.Accounts;
import com.firstutility.utils.hierarchygenerator.service.AccountsHierarchyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountsProcessor implements ItemProcessor<Accounts, Accounts> {

    private static final Logger log = LoggerFactory.getLogger(AccountsProcessor.class);

    private final AccountsHierarchyService accountsHierarchyService;

    @Autowired
    public AccountsProcessor(final AccountsHierarchyService accountsHierarchyService) {
        this.accountsHierarchyService = accountsHierarchyService;

    }

    @Override
    public Accounts process(Accounts accounts) throws Exception {

        log.info(accounts.toString());

        final String parentCustomerNumber =
                accountsHierarchyService.createAccountsHierarchy(accounts);

        accounts.setParentCustomerNumber(parentCustomerNumber);

        return accounts;
    }
}
