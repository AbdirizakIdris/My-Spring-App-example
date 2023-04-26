package com.idris;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepo customerRepo;

    public List<Customer> getCustomers() {
        return customerRepo.findAll();
    }

    public void addCustomer(NewCustomerRequest request) {
        Customer  customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setAge(request.getAge());
        customerRepo.save(customer);
    }

    public void deleteCustomer(Integer id) {
        customerRepo.deleteById(id);
    }

    public void updateCustomer(Integer id, NewCustomerRequest request) {
        customerRepo.deleteById(id);
        Customer  customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setAge(request.getAge());
        customerRepo.save(customer);
    }
}
