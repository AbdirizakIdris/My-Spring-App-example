package com.idris;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;


@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = {CustomerControllerTest.Initializer.class})
@ExtendWith(SpringExtension.class)
class CustomerControllerTest {

    /*
    Step 1 : override app properties to point to test container Db
    Step 2: Spin up your test container DB - use test containers
    Step 3: Set up your customer table with the fields - use liquibase
    Step 4: Add some data into the table - use JDBI
    */
    private static final int POSTGRES_CONTAINER_PORT_NUMBER = 5432;
    private static final int APP_PORT_NUMBER = 9000;
    private static final String DATABASE_NAME = "customer";
    private static final String DATABASE_USER = "user";
    private static final String DATABASE_PASSWORD = "password";

    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                .withDatabaseName(DATABASE_NAME)
                .withUsername(DATABASE_USER)
                .withPassword(DATABASE_PASSWORD)
                .withExposedPorts(POSTGRES_CONTAINER_PORT_NUMBER);

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @BeforeAll
    static void start() {
        postgres.start();
    }

    @AfterAll
    static void stop() {
        postgres.stop();
    }

    @Test
    void callingGetCustomerShouldReturnAllFields() {
        Jdbi jdbi = Jdbi.create(postgres.getJdbcUrl(), DATABASE_USER, DATABASE_PASSWORD);
        jdbi.withHandle(handle -> handle.createUpdate("INSERT INTO customer(id, name, email, age) VALUES (:id, :name, :email, :age)")
                .bind("id", 1)
                .bind("name", "alice")
                .bind("email", "alice@gmail.com")
                .bind("age", 13)
                .execute());

        /*
        JSON output looks like this:
        [
            {
                "id": 1,
                "name": "alice",
                "email": "alice@gmail.com",
                "age": 31
            }
        ]
         */

        given()
                .port(APP_PORT_NUMBER)
                .when()
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("[0].id", is(1))
                .body("[0].name", is("alice"))
                .body("[0].email", is("alice@gmail.com"));

    }

    @Test
    void shouldDeleteCustomer() {
        Jdbi jdbi = Jdbi.create(postgres.getJdbcUrl(), DATABASE_USER, DATABASE_PASSWORD);
        jdbi.withHandle(handle -> handle.execute("INSERT INTO \"customer\" (id, \"name\", \"email\") VALUES (?, ?, ?)", 0, "Alice", "Alicce@gmail.com"));

        given()
                .port(APP_PORT_NUMBER)
                .when()
                .get("api/v1/customers")
                .then()
                .statusCode(200)
                .body("id", is(null))
                .body("name", is(null))
                .body("email", is(null));

    }
}
