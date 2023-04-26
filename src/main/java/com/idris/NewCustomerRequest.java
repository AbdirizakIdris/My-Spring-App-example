package com.idris;

public class NewCustomerRequest {
    String name;
    String email;
    Integer age;

    public NewCustomerRequest(String name, String email, Integer age) {
           this.name = name;
           this.email = email;
           this.age = age;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
