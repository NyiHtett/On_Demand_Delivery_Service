package com.example.demo.service;

import java.util.List;
import com.example.demo.dao.CustomerDao;
import com.example.demo.model.Customer;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<Customer> getAllCustomers() {
        return customerDao.getAllCustomers();
    }
}