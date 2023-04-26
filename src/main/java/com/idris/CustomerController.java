package com.idris;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/customers")
@Tag(name = "Book Repository", description = "My Book Store")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get a book by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the book",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Customer.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid id supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found",
                    content = @Content) })
    public List<Customer> getCustomers(){
        return customerService.getCustomers();
    }

    @PostMapping
    @Operation(summary = "Add customer")
    public void addCustomer(@RequestBody NewCustomerRequest request) {
        customerService.addCustomer(request);
    }

    @DeleteMapping("{customerId}")
    @Operation(summary = "Delete customer")
    public void deleteCustomer(@PathVariable("customerId") Integer id) {
        customerService.deleteCustomer(id);
    }

    @PutMapping("{customerId}")
    @Operation(summary = "Update customer")
    public void updateCustomer(@PathVariable("customerId") Integer id, @RequestBody NewCustomerRequest request) {
        customerService.updateCustomer(id, request);
    }
}
