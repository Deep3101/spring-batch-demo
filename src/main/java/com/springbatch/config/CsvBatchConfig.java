package com.springbatch.config;

import com.springbatch.entity.Customer;
import com.springbatch.repository.CustomerRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CsvBatchConfig {

    @Autowired
    private CustomerRepository customerRepository;

    //creating Reader
    @Bean
    public FlatFileItemReader<Customer> customReader() {
        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource("/home/root435/Downloads/Projects/Batch Processing/Batch-Processing/src/main/resources/customer.csv"));
        reader.setName("csv-reader");
        reader.setLinesToSkip(1);   //skipping header line
        reader.setLineMapper(lineMapper()); //take each line and convert that to java object
        return reader;
    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>(); //to map lines from csv to Customer objects

        //tokenizing data using comma as delimiters
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer(); //map tokenize field to the properties of customer class
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);  //if one column data value unavailable consider it as null
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        //to convert data to java object
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    //create processor
    @Bean
    public CustomerProcessor customerProcessor() {
        return new CustomerProcessor();
    }

    //create writer
    @Bean
    public RepositoryItemWriter<Customer> customerWriter() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    //create step
    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("step-1",jobRepository)
                .<Customer,Customer>chunk(10,transactionManager)
                .reader(customReader())
                .processor(customerProcessor())
                .writer(customerWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    //create job
    @Bean
    public Job runJob(JobRepository jobRepository, PlatformTransactionManager transactionManager){
        return new JobBuilder("customers-job",jobRepository)
                .flow(step(jobRepository,transactionManager)).end().build();
        //flow to specify sequence of steps in job
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(10);
        return taskExecutor;
    }
}
