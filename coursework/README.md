# Restaurant Management System

> ### Type of service
>A Restaurant Management System is a software application designed to help restaurant owners and managers efficiently
> run their business. It typically includes features such as equipment taking, table management, inventory management,
> employee management.
>
>### What is the purpose of service?
>The purpose of a Restaurant Management System is to streamline the operations of a restaurant, increase efficiency,
> reduce costs, and improve customer service. By automating various tasks and providing real-time data, the system can
> help restaurant owners and managers make informed decisions and optimize their business processes.
>
>### What problem is resolved by this service?
>Without a Restaurant Management System, restaurants may face various operational challenges such as inaccurate
> equipment
> taking, inefficient table management, inadequate inventory tracking, and ineffective employee scheduling. These issues
> can lead to higher costs, lower customer satisfaction, and reduced profitability. The Restaurant Management System
> helps
> to address these problems by providing a comprehensive solution for managing the restaurant's operations.

## Tech, Framework and other Dependencies

* Java version: 17
* Gradle version: 7.4
* SpringBoot version: 3.0.4
* Other core frameworks versions
    * Spring version: 6.0.2
    * OpenAPI version: 2.0.2
    * Jupiter API version: 5.8.1
    * Mockito JUnit Jupiter version: 4.8.0
    * Mockito Inline version: 3.11.2
    * Mockito Core version: 4.8.0
    * AssertJ Core version: 3.23.1
    * TestContainer version: 1.17.6
    * Guava version: 31.1-jre
    * Commons Lang3 version: 3.12.0
    * MapStruct version: 1.5.3.Final
    * Lombok version: 1.18.24
    * Faker version: 1.0.2
    * Jackson version: 2.14.2
    * HikariCP version: 5.0.1
    * Postgres version: 42.5.0
    * Flyway Core version: 9.15.1
    * JWT version: 0.11.2

## Building

<p>
<details>
<summary>Instructions for build, run, deploy and test project</summary>

### Using Docker

```shell
docker compose up -d
```

### Using Gradle

build the project

```shell
./gradlew build
```

run the project

```shell
./gradlew :coursework:infrastructure:web-app:bootRun
```

run tests.

```shell
./gradlew test
```

</details>
<p>

### Logging agreements

> Logging is implemented using the <b>Logback</b> framework.