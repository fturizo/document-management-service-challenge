# Document Management Service Application

## Overview

This project is an implementation of the **Document Management Service Challenge** as described in the corresponding [challenge instructions](docs/instructions/README.md).

This project exposes a comprehensive REST API built with **Spring Boot 3.4.x** that provides the following endpoints:

- **User Registration** – Allows new users to register in the system.
- **Document Upload** – Authenticated users can upload documents with metadata (name, tags) to an S3-compatible object store (MinIO).
- **Document Download** – Retrieve download location information for a previously uploaded document.
- **Document Search** – Paginated search of documents by name and/or tags.

The application is compiled as a **GraalVM native image** for minimal memory footprint and fast startup, and is deployed via **Docker Compose** alongside PostgreSQL and MinIO.

## Tech Stack

|    Component     |                                    Technology                                     |
|------------------|-----------------------------------------------------------------------------------|
| Language         | Java 17                                                                           |
| Framework        | Spring Boot, Spring MVC, Spring Data JPA, Spring Security, Spring Bean Validation |
| Database         | PostgreSQL 15                                                                     |
| Object Storage   | MinIO (S3-compatible)                                                             |
| Native Image     | Oracle GraalVM (via Spring Boot Buildpacks)                                       |
| Build Tool       | Maven (version 3.9.11 required as a minimum)                                      |
| Containerization | Docker / Docker Compose                                                           |
| Testing          | JUnit 5, Mockito, Testcontainers, TestRestTemplate                                |

## Testing

The project includes two main test suites to ensure both service logic and API integrations work as expected.

### Unit Tests

- **Technology Stack:** JUnit 5, Mockito.
- **Focus:** Isolates service logic (`DocumentService`, `UserService`) using Mockito to mock dependencies like `UserRepository`, `StorageService`, and `MetadataService`.
- **Key Tests:**
  - User registration and password encryption.
  - Document upload validation (file types, size limits, duplicates).
  - Concurrency check: Verifies that `ConcurrentThresholdReachedException` is thrown when more than 10 parallel uploads occur.
  - Secure download URL generation and access control.

### Integration Tests

- **Technology Stack:** Testcontainers (PostgreSQL, MinIO), `TestRestTemplate`, `MinioClient`.
- **Focus:** Runs the full application context and tests the REST endpoints against real service containers.
- **Key Tests:**
  - **Registration Integration:** Validates user creation, duplicate handling, and validation errors.
  - **Document Management Integration:**
    - End-to-end upload/download/search flows.
    - Large file upload validation (500MB+).
    - Direct verification in MinIO using `MinioClient` to confirm persistence.
- **Base Class:** `BaseIntegrationTest` manages the lifecycle of the PostgreSQL and MinIO containers using the singleton container pattern.

### Running Tests

To run all tests, use the following Maven command:

```bash
./mvnw test
```

To run only a specific test suite (e.g., unit tests):

```bash
./mvnw test -Dtest=UserServiceTest,DocumentServiceTest
```

Note: Integration tests require **Docker** to be running, as they use Testcontainers to spin up PostgreSQL and MinIO instances.

## API Documentation

An OpenAPI v3 specification is available at [`docs/dms_openapi.yaml`](docs/dms_openapi.yaml).

You can import this file into tools like [Swagger Editor](https://editor.swagger.io/) or [Bruno](https://www.usebruno.com/) to explore the API interactively.

## Bruno Collections

A Bruno v3 collection with sample requests is also provided in the [`bruno/`](bruno/) directory. You can use this collection simply by using the **Open Collection** menu option and then selecting the `bruno/Document Management Service` directory.

To run the sample requests in the provided collection, enable the `Local Test` environment first and assign a strong password to the `password` environment variable (since it is a secret, it will be blank by default).

For the sample requests that upload document files, you'll have to use local files in your machine which can be updated under the `Body` --> `Value` column in the corresponding parameter table.

## Prerequisites

Before setting up the project, make sure you have the following installed:

- **Java 17** (GraalVM distribution recommended but not needed for local testing)
- **Maven 3.9+** (or use the included Maven Wrapper `./mvnw`)
- **Docker** and **Docker Compose**

## Setup & Deployment

### 1. Build the GraalVM Native Image

The application is packaged as a native Docker image using the Spring Boot Buildpacks support. Run the following command from the project root:

```bash
./mvnw -Pnative spring-boot:build-image
```

Note: This process may take several minutes as it performs Ahead-of-Time (AOT) compilation via GraalVM using Spring Boot integration with the GraalVM Native image Maven plugin. You may also use `-Dskip-tests` to avoid running the test suites and increase the build time.

Ensure Docker is running, as the build uses Docker to create the native image.
This will produce a Docker image named `clara/document-management-service-challenge:0.0.1-SNAPSHOT`.

### 2. Configure Environment Variables

The Docker Compose file expects several environment variables. The [`docker/.env`](docker/.env) environment file is provided with default values that will be used by each of the Docker containers provisioned during the Docker compose deployment.

Feel free to adjust the variables in this file to suit your environment.

### 3. Deploy with Docker Compose

Navigate to the docker/ directory and start all services:

```bash
cd docker/
docker compose up -d
```

This command will start the following services:

|      Service      |     Port(s)     |                         Description                          |
|-------------------|-----------------|--------------------------------------------------------------|
| PostgreSQL        | `5432`          | The relational database                                      |
| MinIO             | `9000` / `9001` | Object storage (API / Web Console)                           |
| MinIO Init Script |                 | One-shot container to setup the MinIO bucket and access keys |
| DMS Application   | `8080`          | The Document Management Service API                          |

Note: The `dms` service may start too quickly, even before the PostgreSQL database is fully ready to accept connections, and it may fail its Spring Boot initialization. In such a case, start the service again:

```bash
cd docker/
docker compose start dms
```

### 4. Verify the Deployment

Once all containers are up, you can verify the application is running by creating a new user registration:

```bash
curl --request POST \
  --url http://localhost:8080/registration \
  --header 'content-type: application/json' \
  --data '{
      "username": "malfa",
      "password": "challenge_20XX",
      "fullName": "Michael Alfa"
    }'
```

You can also access the MinIO Web Console at http://localhost:9001 using the root credentials configured in the `.env` file.

### 5. Troubleshooting

On Windows OS environments, the `minio-init` container may fail to run the bucket and key initialization shell script due to the script being check out by Git using Windows-style line breaks (`CRLF`).

To fix this issue, you may use a utility to fix the line breaks to work on Linux environments such as `dos2unix` and then starting the `minio-init` container again.

## Challenge Solutions and Deviations

To fully solve the requirements stipulated in the challenge, the following considerations in the Document Management Service application have been implemented:

1. The most important consideration is that to satisfy the minimal memory requirement for the application container (`50 MB`) and allow the service to run as a fully provisioned Spring Boot application, the application's deployment was implemented using a GraalVM JDK 17 native image via Spring Boot's AOT compilation integration. This allows the native image to run successfully under the memory requirements (using `85%` of available memory for the JVM Heap) while also maintaining and also immediate startup time due to the AOT compilation information present. The tradeoff is that the application's build time is now increased, and additional libraries required for future features may require additional AOT optimizations (reflection hints, proxy provider hints, etc.)
2. The Minio Docker image was downgraded to an older release (`2025-04-22T22-12-26Z`) due to the MinIO Community releases adopting breaking changes that removed many features from the Web console (like adding access keys). Although these features are not critical to test and implement the MinIO integrations, it is a good idea to use a more complete Web console.
3. The image name of the PostgreSQL container was updated to `bitnamilegacy/postgresql:15.4.0` due to this version of the Bitnami image being moved to a "legacy" status since August 2025.
4. Finally, all document management endpoints are fully protected via standard Spring Security configurations using Basic Authentication. Users will have to register themselves by providing username/password credentials via the `POST /registration` endpoint (see the OpenAPI specification document linked above) first before calling any document management endpoint. This change was done in principle, since it is an insecure development practice to allow API clients to freely input which users are uploading/downloading/searching documents. Instead, users are limited to accessing their uploaded documents in a confidential and secure manner.

