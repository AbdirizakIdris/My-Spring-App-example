# What is the application?
REST API monolith Spring application that uses a postgres database to store customers. A REST API is exposed to the user. 

### Endpoints

1. **Get all customers**
    - **Request:**
        - Method: GET
        - URL: `/api/v1/customers`
          - Example Request:
            ```
            GET /api/v1/customers
            ```

    - **Response:**
        - Status Code: 200 OK
        - Example Response:
          ```
          [
          {
            "id" : 1
            "name": "Alice"
            "email": "Alice@gmail.com"
            "age": 30
          },
           {
           "id": 2,
           "name": "Bob",
           "email": "bob@example.com",
           "age": 30
           },
           {
            "id": 3,
            "name": "Charlie",
            "email": "charlie@example.com",
            "age": 28
            }
          ]
          ```

2. **Add a new customer**
    - **Request:**
        - Method: POST
        - URL: `/api/v1/customers`
        - Example Request:
          ```
          POST /api/v1/customers
          Content-Type: application/json
   
          {
            "name": "John Doe",
            "email": "john@example.com",
            "age": 30
          }
          ```

    - **Response:**
        - Status Code: 201 Created
        - Example Response:
          ```
          {
            "id" 1
            "name": "John Doe",
            "email": "john@example.com",
            "age": 30
          }
          ```

3. **Delete a customer**
    - **Request:**
        - Method: DELETE
        - URL: `/api/v1/customers/{customerId}`
        - Example Request:
          ```
          DELETE /api/v1/customers/123
          ```

    - **Response:**
        - Status Code: 200 OK
      - Example Response:
         ```
           {"message": "Customer with ID {id} was deleted successfully."}
         ```

4. **Update a customer**
    - **Request:**
        - Method: PUT
        - URL: `/api/v1/customers/{customerId}`
        - Example Request:
          ```
          PUT /api/v1/customers/123
          Content-Type: application/json
   
          {
            "name": "Updated Name",
            "email": "updated@example.com",
            "age": 35
          }
          ```

    - **Response:**
        - Status Code: 201 Created
        -  Example Response:
             ```
             {
             "id" 123
             "name": "Updated Name",
             "email": "john@example.com",
             "age": 35
             }
             ```

 These endpoints are defined in the `CustomerController` class.

### How the Test works
- The  integration test spins up the controller, service and database layer. As part of the test it uses the TestContainers 
library to set up a Postgres container.
- The ``CustomerControllerTest.Initializer`` static class overrides the application properties in ``application.yml`` so that our 
application points to the Postgres container
- Next liquibase runs the database migrations defined in the `migrations.xml` file to configure the PostgreSQL container to include a Customer table.
- After, our tests use JDBI to connect to the Postgres container to set up test data into our Postgres container (e.g. adding and removing customers from our table) for our tests to work. 
An @AfterEach hook runs after each test to clear away the test data.
- Rest Assured is used in the test to verify the correct behavior of the API by sending requests and checking responses against expected values.

###  How the Application works
- The application uses SpringDoc OpenAPI to generate Swagger documentation which can be accessed at http://localhost:9000/swagger-ui.html
- The `application.yml` file contains the app's configuration details, like Liquibase config for database and Swagger
- Liquibase reads and applies database migrations defined in `00001_create_table_customer.sql` and creates the Customer table
- Jackson is used to serialize/deserialize JSON 
- Hibernate is an ORM that implements JPA and uses @Entity to map onto the Customer table
- The CustomerRepo interface extends JpaRepository<Customer, Integer>, which provides some built-in methods for database operations (e.g., save(), findById() etc) which are available to us without explicitly defining them
- Spring uses the @Autowired annotation to do dependency injection
  
 ### Database schema 
``` 
CREATE TABLE customer (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50),
  email VARCHAR(100),
  age INTEGER
  );
  ```

#### Error handling:
- When a customer is not found, a `CustomerNotFoundException` is thrown which is handled by our `ExceptionHandler` which returns a `404` status code and an error message.
### Sequence diagram
![sequence diagram](sequenceDiagram.png)
# How to run
To start up the database:

 ```docker run -p 5432:5432 -e POSTGRES_PASSWORD=password -e POSTGRES_USER=user postgres```

To start up the Spring Boot application: 
```
mvn clean package
java -jar target/spring-boot-example-0.0.1-SNAPSHOT.jar server application.yml
```