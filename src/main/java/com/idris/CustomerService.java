package com.idris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        customerRepo.deleteById(id);
        logger.info("Added new customer with: {} ", id);

    }

    public Customer updateCustomer(Integer id, NewCustomerRequest request) {
        Optional<Customer> optionalCustomer = customerRepo.findById(id);
        return optionalCustomer.map(customer -> {
            customer.setName(request.getName());
            customer.setEmail(request.getEmail());
            customer.setAge(request.getAge());
            customerRepo.save(customer);
            return customer;
        }).orElseThrow(() ->
             new CustomerNotFoundException("Customer with ID " + id + " not found"));
    }
}
