# Jokes Provider Microservice

## Requirements
- Create a microservice that returns jokes over HTTP.
- Technology stack: Java 19 or newer versions, Spring Boot, Maven.
- Expose a REST GET /jokes endpoint allowing users to request from 1 to 100 jokes. If not specified, the default is to return 5 jokes.
- Retrieve jokes only from the [official-joke-api.appspot.com/random_joke](https://official-joke-api.appspot.com/random_joke) endpoint.
- When requesting more than one joke, fetch them in batches of 10 in parallel.
- Store received jokes in a NoSQL storage.

## Solution

The Jokes Provider Microservice fetches random jokes from an external API, stores them in MongoDB, and serves them to the requester.

### Component Diagram

![Component Diagram](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/Component%20Diagram.png)


### Technical Considerations
- **Enable Preview Features:** Preview features were enabled in IntelliJ IDEA following the steps outlined in [Enable Java Preview Feature in IntelliJ article](https://medium.com/javarevisited/enabling-the-preview-feature-in-intellij-735067948d6e). This step was necessary because JDK 22 includes features like Structured Concurrency and String Templates, which are still in preview (Second Preview) status. By enabling preview features, we ensure that these functionalities are accessible and usable within our development environment.
- **Enable Spring Boot Virtual Threads:** Setup was made to enable the embedded Tomcat server to handle each request in a virtual thread.
- **Using Concurrent Virtual Threads:** Fetch jokes in parallel using concurrent virtual threads. Due to IO operations and limitations of the random joke endpoint, this enhances performance by fetching jokes in configurable batch sizes concurrently.
- **Storing Jokes in MongoDB:** Avoid relying on random joke IDs as there's no guarantee they're unique. Use a UUID format for IDs in the database.
- **Encrypting Sensitive Information:** Encrypt sensitive configuration data with Jasypt for security.
- **Setting Resilience Mechanisms:** Utilize Resilience4j to set configurable retry and rate-limiting mechanisms for interacting with the external API.
- **No Infrastructure:** The project focuses solely on the microservice and does not cover infrastructure complexities such as service discovery, configuration services, gateways, or Kubernetes.

### Goal
Implement the requirements using the latest Java features, focusing on Virtual Threads and Structured Concurrency, along with features like Records, String Templates, and Pattern Matching.
Ensure an error-free solution by implementing unit and integration tests to cover all possible scenarios.


## Sequence Diagrams

### Get Random Jokes in Parallel
![Get Random Jokes in Parallel](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/get%20random%20jokes%20in%20parrallel.png)

### Rate Limit Mechanism in Action
![Rate Limit Mechanism in Action](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/rate%20limit%20mechanism%20in%20action.png)

### Retry Mechanism in Action
![Retry Mechanism in Action](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/retry%20mechanism%20in%20action.png)


## Getting Started

Follow these steps to set up and run the Jokes Provider Service:

1. **Set Up Java 22 Language Level and JDK**:
  - Ensure you have Java 22 installed on your system.
  - Set the language level and JDK in your IDE to Java 22. You can refer to the following captures for guidance:
    ![Set Language Level Capture](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/setup%20-%20project%20sdk%20level.PNG)
    ![Set SDK Level Capture](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/setup%20-%20platform%20settings%20sdk%20language%20level.PNG)

2. **Set Up Environment Variable for Jasypt**:
    - Ensure the following environment variable is set:
        - `JASYPT_ENCRYPTOR_PASSWORD=razvanbh0cW+opc6+FxUgQBD5T/dG1kKJIWyvS3`

3. **Create Docker Network**:
  - Create a Docker network named `razvanb-jokes-network` by running the following command:
    ```bash
    docker network create razvanb-jokes-network
    ```

4. **Start MongoDB Containers**:
  - Navigate to the Docker directory and locate the `mongo-setup.yml` file.
  - Start the MongoDB containers by running the following command:
    ```bash
    docker-compose -f ./mongo-setup.yml up -d
    ```

After completing these steps, you'll be ready to run the Jokes Provider Service locally.

**Note**: Ensure Docker is installed and running on your system before starting the MongoDB containers.

## API
To access the Swagger API documentation for the running Jokes Provider Service hosted locally, please visit [http://localhost:1005/swagger-ui/index.html](http://localhost:1005/swagger-ui/index.html).

### Screenshots

#### Collapsed Swagger
![Swagger Capture 1](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/swagger%20collapsed.PNG)

#### Expanded Swagger
![Swagger Capture 2](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/swagger%20capture.PNG)

## Database

To interact with the MongoDB database used by the Jokes Provider Service, follow these steps:

1. Access MongoDB via Mongo Express using the provided URL: [http://localhost:8082/](http://localhost:8082/).
2. After trying the endpoint (e.g., via Swagger), you can find the 'jokesdb' database containing the requested amount of jokes stored as documents.

### jokesdb
![jokesdb](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/jokesdb%20mongoexpress%20capture.PNG)

### Joke Document Structure
![JokeDocument](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/jokedocumend%20structure%20in%20db.PNG)

3. When running tests, the jokes are stored in the 'jokesdb-test' database. You can access it at: [http://localhost:8082/db/jokesdb-test/jokes](http://localhost:8082/db/jokesdb-test/jokes).

## Encrypt Sensitive Configuration With Jasypt
Describe the encryption algorithm, key usage, and provide a link to Jasypt documentation.

## Metrics

### Custom Metrics
- **"custom.retry.count"**: Counts the number of retries.
- **"custom.rateLimiter.success.count"**: Counts the successful rate limiter requests.
- **"custom.rateLimiter.failure.count"**: Counts the rate limiter failures.

### Implicit Metrics
- **"resilience4j.retry.calls"**: This metric is triggered for various scenarios:
    - **kind="successful.with.retry"**: Triggered when a retried request succeeds.
    - **kind="successful.without.retry"**: Triggered when a request succeeds without retry.
    - **kind="failed.with.retry"**: Triggered when a retried request fails.
    - **kind="failed.without.retry"**: Triggered when a request fails without being retried.

Read more about `resilience4j.retry.calls` on the [official site](https://resilience4j.readme.io/docs/micrometer).

Tests are implemented to ensure the correct functionality of rate limiter and retry mechanisms and the corresponding metrics.

## Testing

The Jokes Provider Microservice is thoroughly tested to ensure reliability and stability. Unit tests, integration tests, and end-to-end tests cover all critical functionalities and edge cases.

### Test Coverage
![Test Coverage](https://github.com/razvantechjourney/jokes-service/blob/master/other%20resources/tests%20coverage.PNG)

The test coverage ensures that all components and features of the microservice are rigorously tested, resulting in a robust and dependable application.

---

Thank you for exploring the Jokes Provider Microservice! I invite you to experience the resilience and performance of our application firsthand. Try it out! 😊

**Tags:** #Java #VirtualThreads #StructuredConcurrency #Records #MongoDB #Resilience #Performance

---

