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
public class CustomerControllerTest {
    private static final int POSTGRES_CONTAINER_PORT_NUMBER = 5432;
    private static final int APP_PORT_NUMBER = 9000;

    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"))
                .withDatabaseName("customer")
                .withUsername("zak")
                .withPassword("password")
                .withExposedPorts(POSTGRES_CONTAINER_PORT_NUMBER);

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgres.getJdbcUrl(),
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword(),
                    "spring.liquibase.url=" + postgres.getJdbcUrl(),
                    "spring.liquibase.user=" + postgres.getUsername(),
                    "spring.liquibase.password=" + postgres.getPassword(),
                    "spring.liquibase.enabled=true",
                    "spring.liquibase.change-log=classpath:/migrations.xml" //path to your liquibase migrations
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }


    @BeforeAll
    public static void start() {
        postgres.start();
    }

    @AfterAll
    public static void stop() {
        postgres.stop();
    }

    @Test
    public void shouldGetCustomer() {
        System.out.println(postgres.getJdbcUrl());
        Jdbi jdbi = Jdbi.create(postgres.getJdbcUrl(), "zak", "password");

        jdbi.withHandle(handle -> handle.execute("INSERT INTO \"customers\" (id, \"name\", \"email\", \"age\") VALUES (?, ?, ?, ?)", 1, "Alice", "Alicce@gmail.com", 13));


        given()
                .port(APP_PORT_NUMBER)
                .when()
                .get("/api/customers")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("name", is("Alice"))
                .body("email", is("Alice@gmail.com"));

    }

    @Test
    public void shouldDeleteCustomer() {

        Properties properties = new Properties();
        properties.setProperty("username", postgres.getUsername());
        properties.setProperty("password", postgres.getPassword());
        Jdbi jdbi = Jdbi.create(postgres.getJdbcUrl(), properties);

        jdbi.withHandle(handle -> handle.execute("INSERT INTO \"customer\" (id, \"name\", \"email\") VALUES (?, ?, ?)", 0, "Alice", "Alicce@gmail.com"));


        given()
                .port(APP_PORT_NUMBER)
                .when()
                .get("http://localhost:8000/api/customers")
                .then()
                .statusCode(200)
                .body("id", is(null))
                .body("name", is(null))
                .body("email", is(null));

    }

}


   /*
    Step 1 : override app properties to point to test container Db
    Step 2: Spin up your test container DB - use test containers
    Step 3: Set up your customer table with the fields - use liquibase
    Step 4: Add some data into the table - use JDBI
    ALl of these must before the test
     */
