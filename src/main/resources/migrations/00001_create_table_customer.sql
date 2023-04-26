--liquibase formatted sql

--changeset com.idris:add_table-customer
CREATE TABLE customer (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50),
    email VARCHAR(100),
    age INTEGER
);
--rollback drop table customer;

