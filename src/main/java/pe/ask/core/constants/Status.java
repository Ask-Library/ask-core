package pe.ask.core.constants;

/**
 * Standard status catalog for API responses.
 * <p>
 * Helps clients (Frontends, Mobile, other microservices) to quickly identify
 * the outcome of the operation without relying solely on HTTP status codes.
 * </p>
 *
 * @author Allan Sagastegui
 */
public enum Status {
    /**
     * The operation completed successfully (HTTP Equivalent: 200, 201, 204).
     */
    SUCCESS,

    /**
     * Batch operation where some elements were processed and others failed.
     */
    PARTIAL_SUCCESS,

    /**
     * The server is emitting data chunks continuously (WebFlux Streaming).
     */
    STREAMING,

    /**
     * The request has an invalid format or field validations failed (HTTP Equivalent: 400).
     */
    BAD_REQUEST,

    /**
     * The client has not provided valid credentials (HTTP Equivalent: 401).
     */
    UNAUTHORIZED,

    /**
     * The client is authenticated, but does not have permissions for this resource (HTTP Equivalent: 403).
     */
    FORBIDDEN,

    /**
     * The requested resource does not exist in the database (HTTP Equivalent: 404).
     */
    NOT_FOUND,

    /**
     * Business rule violation, database unique keys, or optimistic locking error (HTTP Equivalent: 409).
     */
    CONFLICT,

    /**
     * The operation took too long to respond (HTTP Equivalent: 408, 504).
     */
    TIMEOUT,

    /**
     * Unhandled exception or internal server error (HTTP Equivalent: 500).
     */
    ERROR
}
