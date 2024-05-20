package com.springbatch.config;

import com.springbatch.entity.Customer;

import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer item) {

//        if(item.getCountry().equals("USA")){
//            return item;
//        }
        return item;
    }
}
