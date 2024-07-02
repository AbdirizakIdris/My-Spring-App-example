package com.idris;

import io.restassured.http.ContentType;
import org.jdbi.v3.core.Jdbi;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = {CustomerControllerTest.Initializer.class})
@ExtendWith(SpringExtension.class)
class CustomerControllerTest {

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

    @AfterEach
    void cleanUpDatabase() {
        Jdbi jdbi = Jdbi.create(postgres.getJdbcUrl(), DATABASE_USER, DATABASE_PASSWORD);
        jdbi.withHandle(handle -> handle.execute("DELETE FROM customer;"));
    }

    @Test
    void callingGetCustomerShouldReturnAllFields() {
        Jdbi jdbi = Jdbi.create(postgres.getJdbcUrl(), DATABASE_USER, DATABASE_PASSWORD);
        jdbi.withHandle(handle -> handle.execute("INSERT INTO \"customer\" (id, \"name\", \"email\", age) VALUES (?, ?, ?, ?)", 1, "alice", "alice@gmail.com", 14));

        given()
                .port(APP_PORT_NUMBER)
                .when()
                .get("/api/v1/customers")
                .then()
                .statusCode(200)
                .body("[0].id", is(1))
                .body("[0].name", is("alice"))
                .body("[0].email", is("alice@gmail.com"))
                .body("[0].age", is(14));
    }

    @Test
    void shouldDeleteCustomer() {
        Jdbi jdbi = Jdbi.create(postgres.getJdbcUrl(), DATABASE_USER, DATABASE_PASSWORD);
        jdbi.withHandle(handle -> handle.execute("INSERT INTO \"customer\" (id, \"name\", \"email\") VALUES (?, ?, ?)", 1, "James", "james@gmail.com"));

        given()
                .port(APP_PORT_NUMBER)
                .when()
                .delete("/api/v1/customers/1")
                .then()
                .statusCode(200)
                .body("message", is("Customer with ID 1 was deleted successfully."));
    }

    @Test
    void shouldAddCustomerSuccessfully() {
        String jsonString = """
            {
                "name": "Abdi Ali",
                "email": "AbdiAli@example.com",
                "age": 30
            }
            """;

        given()
                .port(APP_PORT_NUMBER)
                .contentType(ContentType.JSON)
                .body(jsonString)
                .when()
                .post("/api/v1/customers")
                .then()
                .statusCode(201)
                .body("name", is("Abdi Ali"))
                .body("email", is("AbdiAli@example.com"))
                .body("age", is(30));
    }

    @Test
    void shouldUpdateExistingCustomerFromJamesToAlice() throws JSONException {
        Jdbi jdbi = Jdbi.create(postgres.getJdbcUrl(), DATABASE_USER, DATABASE_PASSWORD);
        jdbi.withHandle(handle -> handle.execute("INSERT INTO \"customer\" (id, \"name\", \"email\", age) VALUES (?, ?, ?, ?)", 2, "james", "james@gmail.com", 14));

        String jsonString = """
                {
                    "id": 2,
                    "name": "alice",
                    "age": 20,
                    "email": "alice@gmail.com"
                }
                """;

        given()
                .port(APP_PORT_NUMBER)
                .contentType(ContentType.JSON)
                .body(jsonString)
                .when()
                .put("/api/v1/customers/2")
                .then()
                .statusCode(200)
                .body("name", is("alice"))
                .body("email", is("alice@gmail.com"))
                .body("age", is(20));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentCustomer() {
        int nonExistentCustomerId = 999;

        String jsonString = """
                {
                    "name": "alice",
                    "age": 20,
                    "email": "alice@gmail.com"
                }
                """;

        given()
                .port(APP_PORT_NUMBER)
                .contentType("application/json")
                .body(jsonString)
                .put("/api/v1/customers/" + nonExistentCustomerId)
                .then()
                .statusCode(404)
                .body("message", is("Customer with ID " + nonExistentCustomerId + " not found"));
    }
}
