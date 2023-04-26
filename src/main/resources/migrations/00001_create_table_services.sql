--liquibase formatted sql

--changeset com.idris:add_table-customers
CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(100),
    age INTEGER
);
--rollback drop table customers;

