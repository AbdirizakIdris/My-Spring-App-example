package com.idris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {
    private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerRepo customerRepo;

    public List<Customer> getCustomers() {
        return customerRepo.findAll();
    }

    public Customer addCustomer(NewCustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setAge(request.getAge());
        Customer savedCustomer = customerRepo.save(customer);
        logger.info("Added new customer: {}", savedCustomer);
        return savedCustomer;
    }


    public void deleteCustomer(Integer id) {
        customerRepo
                .findById(id)
                .map(customer -> {
                    customerRepo.delete(customer);
                    return customer;
                })
                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID " + id + " not found"));
    }

    public Customer updateCustomer(Integer id, NewCustomerRequest request) {
        return customerRepo
                .findById(id)
                .map(customer -> {
                    customer.setName(request.getName());
                    customer.setEmail(request.getEmail());
                    customer.setAge(request.getAge());
                    customerRepo.save(customer);
                    return customer;
                })
                .orElseThrow(() ->
                        new CustomerNotFoundException("Customer with ID " + id + " not found"));
    }
}
