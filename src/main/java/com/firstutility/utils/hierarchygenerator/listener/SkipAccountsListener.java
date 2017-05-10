package com.firstutility.utils.hierarchygenerator.listener;

import static java.util.Collections.singletonList;

import com.firstutility.utils.hierarchygenerator.model.Accounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.stereotype.Component;

@Component
public class SkipAccountsListener implements SkipListener<Accounts, Accounts> {

    private static final Logger log = LoggerFactory.getLogger(SkipAccountsListener.class);

    private final ItemStreamWriter<Accounts> writer;

    public SkipAccountsListener(final ItemStreamWriter<Accounts> writer) {
        this.writer = writer;
    }

    @Override
    public void onSkipInRead(final Throwable throwable) {
        log.info("onSkipInRead : ", throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(final Accounts person, final Throwable throwable) {
        log.info("onSkipInWrite : ", throwable.getMessage());
    }

    @Override
    public void onSkipInProcess(final Accounts accounts, final Throwable throwable) {
        log.info("onSkipInProcess : {}", throwable.getMessage());
        accounts.setErrorMessage(throwable.getMessage());
        try {
            writer.write(singletonList(accounts));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
