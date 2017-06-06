package com.firstutility.utils.hierarchygenerator.config;

import com.firstutility.utils.hierarchygenerator.exception.CantProcessAccountsException;
import com.firstutility.utils.hierarchygenerator.listener.JobCompletionListener;
import com.firstutility.utils.hierarchygenerator.listener.SkipAccountsListener;
import com.firstutility.utils.hierarchygenerator.model.Accounts;
import com.firstutility.utils.hierarchygenerator.processor.AccountsProcessor;
import com.firstutility.utils.hierarchygenerator.service.AccountsHierarchyService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final String inputDataFile;

    private final String outputErrorFile;

    private final AccountsHierarchyService accountsHierarchyService;

    @Autowired
    public BatchConfig(final JobBuilderFactory jobBuilderFactory,
            final StepBuilderFactory stepBuilderFactory,
            @Value("${input.data.file}") final String inputDataFile,
            @Value("${output.error.file}") final String outputErrorFile,
            final AccountsHierarchyService accountsHierarchyService) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.inputDataFile = inputDataFile;
        this.outputErrorFile = outputErrorFile;
        this.accountsHierarchyService = accountsHierarchyService;
    }

    @Bean
    public FlatFileItemReader<Accounts> inputFileReader() {
        final FlatFileItemReader<Accounts> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(new File(inputDataFile)));
        reader.setStrict(false);
        reader.setLineMapper(new DefaultLineMapper<Accounts>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] {
                        "energyCustomerNumber",
                        "telcoCustomerNumber" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Accounts>() {{
                setTargetType(Accounts.class);
            }});
        }});
        return reader;
    }

    @Bean
    public AccountsProcessor processor() {
        return new AccountsProcessor(accountsHierarchyService);
    }

    @Bean
    public ItemStreamWriter<Accounts> errorItemWriter() {
        final FlatFileItemWriter<Accounts> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(new File(outputErrorFile)));
        writer.setLineAggregator(new DelimitedLineAggregator<Accounts>() {{
            setFieldExtractor(new BeanWrapperFieldExtractor<Accounts>() {{
                setNames(new String[] {
                        "energyCustomerNumber",
                        "telcoCustomerNumber",
                        "parentCustomerNumber",
                        "errorMessage" });
            }});
        }});
        return writer;
    }

    @Bean
    public SkipListener<Accounts, Accounts> skipAccountsListener() {
        return new SkipAccountsListener(errorItemWriter());
    }

    @Bean
    public Job processAccountsJob(final JobCompletionListener listener) {
        return jobBuilderFactory.get("processAccountsJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(processAccountsStep())
                .end()
                .build();
    }

    @Bean
    public Step processAccountsStep() {
        return stepBuilderFactory.get("processAccounts")
                .<Accounts, Accounts> chunk(10)
                .faultTolerant()
                .skip(CantProcessAccountsException.class)
                .skipLimit(10)
                .listener(skipAccountsListener())
                .reader(inputFileReader())
                .processor(processor())
                .stream(errorItemWriter())
                .build();
    }
}
