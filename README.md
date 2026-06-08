<div align="center">

# Ask Core

<img src="https://img.shields.io/badge/Java-21-F89820?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java Support" />
<img src="https://img.shields.io/badge/Spring_Boot-3.4.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
<img src="https://img.shields.io/badge/Reactor-3.x-007ACC?style=for-the-badge&logo=project-reactor&logoColor=white" alt="Project Reactor" />
<img src="https://img.shields.io/jitpack/v/github/Ask-Library/ask-core?style=for-the-badge&label=JitPack&color=663399" alt="JitPack Version" />
<img src="https://img.shields.io/badge/License-Apache_2.0-1F6FEB?style=for-the-badge" alt="License" />

**Ask Core** is a foundational library designed specifically to accelerate the development of reactive microservices. By centralizing standard models, mapping interfaces, and utility functions, this library ensures consistency across distributed systems built with **Spring WebFlux** and **Project Reactor**.

This library abstracts away boilerplate code, allowing developers to focus purely on business logic while maintaining robust and predictable application architecture.

</div>

---

## Key Features

### 1. Standardized API Models (`pe.ask.core.model.api`)
Provides standard wrappers for API responses to ensure front-end clients and inter-service communications always receive predictable payload structures.
*   **`ApiResponse`**: A unified wrapper containing payload data, status codes, and optional metadata or error details.

### 2. Reactive Pagination Support (`pe.ask.core.model.pagination` & `utils`)
Built-in classes and utilities to seamlessly handle pagination in asynchronous data flows without blocking.
*   **`PageRequest`**: Standardized object for incoming pagination requests (e.g., `page`, `size`, `sortBy`).
*   **`PageResponse`**: Consistent structure for returning paginated data alongside metadata (e.g., `totalElements`, `totalPages`).
*   **`ReactivePaginationUtil`**: Helper functions to convert reactive streams (`Flux`) into paginated responses efficiently.

### 3. Generic Mapping Interfaces (`pe.ask.core.mapper`)
Eliminates boilerplate when converting between Data Transfer Objects (DTOs) and Domain Entities.
*   **`DtoMapper` / `EntityMapper`**: Base interfaces defining standard contract methods (`toDto`, `toEntity`).
*   **`GenericDtoMapper` / `GenericBeanMapper`**: Extensible implementations designed to work harmoniously with reactive operators (`map`, `flatMap`).

### 4. Robust Error Handling (`pe.ask.core.exception`)
Standardized exception definitions to streamline error reporting.
*   **`ErrorCatalog`**: A centralized catalog or enum structure to manage standard system and business error codes.
*   **`MapFailedException`**: Specific exceptions for handling failures during DTO/Entity transformations.

### 5. Shared Constants (`pe.ask.core.constants`)
*   **`Status`**: Common status definitions (e.g., ACTIVE, INACTIVE, DELETED) used across multiple services.

---

## Prerequisites

*   **Java:** 21 or higher
*   **Frameworks:** Spring Boot 3.x (WebFlux) & Project Reactor
*   **Build Tool:** Gradle or Maven

---

## Installation

This library is hosted on [JitPack](https://jitpack.io/#Ask-Library/ask-core), making it easy to include in your projects without needing a dedicated private Maven repository.

### Gradle (Kotlin DSL)

**1. Add the JitPack repository:**
In your `settings.gradle.kts` or `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

**2. Add the dependency:**

```kotlin
dependencies {
    implementation("com.github.Ask-Library:ask-core:1.0.1")
}
```

### Maven

**1. Add the JitPack repository:**
In your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

**2. Add the dependency:**

```xml
<dependency>
    <groupId>com.github.Ask-Library</groupId>
    <artifactId>ask-core</artifactId>
    <version>{current-version}</version>
</dependency>
```

---

## Quick Usage Examples

### 1. Returning a Standard API Response
Wrap your reactive streams in a standardized `ApiResponse`:

```java
import pe.ask.core.model.api.ApiResponse;
import reactor.core.publisher.Mono;

public Mono<ApiResponse<UserDto>> getUser(String id) {
    return userRepository.findById(id)
        .map(user -> ApiResponse.success(user))
        .defaultIfEmpty(ApiResponse.notFound("User not found"));
}
```

### 2. Handling Reactive Pagination
Easily process and return paginated data from a repository:

```java
import pe.ask.core.model.pagination.PageRequest;
import pe.ask.core.model.pagination.PageResponse;
import pe.ask.core.utils.ReactivePaginationUtil;

public Mono<PageResponse<ProductDto>> getProducts(PageRequest request) {
    return ReactivePaginationUtil.paginate(
        productRepository.findAllByCriteria(request), 
        productRepository.countAll(),
        request
    );
}
```

---

## Contributing

We welcome contributions! If you have suggestions for new utilities, mappers, or find a bug, please open an issue or submit a pull request.

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'Add some amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

---

## License

This project is open-sourced under the terms of the **[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)**.

## Author

**Allan Sagastegui**
*   Email: [allxn.sxh@gmail.com](mailto:allxn.sxh@gmail.com)
*   GitHub: [@AllanSagastegui](https://github.com/AllanSagastegui)
* Instagram: [@_ask.dev](https://www.instagram.com/_ask.dev/)