package com.springbatch.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerRestController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @PostMapping("/customers")
    public void loadCsvToDb() throws Exception {
        JobParameters jobParameters =
                new JobParametersBuilder().addLong("Start-At", System.currentTimeMillis()).toJobParameters(); //So, when the Spring Batch job is launched with these JobParameters, it will have access to this "Start-At" parameter, which contains the timestamp representing when the job was started.
        jobLauncher.run(job,jobParameters);
    }

}
