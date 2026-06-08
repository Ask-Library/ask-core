package pe.ask.core.model.api;

import lombok.Builder;
import lombok.Data;
import pe.ask.core.constants.Status;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper.
 *
 * @param <T> the type of the data payload
 */
@Data
@Builder
public class ApiResponse<T> {

    /**
     * Default constructor for ApiResponse.
     */
    public ApiResponse() {}

    /**
     * All args constructor for ApiResponse.
     *
     * @param status the status of the response
     * @param data the data payload
     * @param message the message
     * @param traceId the trace id
     * @param timestamp the timestamp
     */
    public ApiResponse(Status status, T data, String message, String traceId, LocalDateTime timestamp) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.traceId = traceId;
        this.timestamp = timestamp;
    }

    private Status status;
    private T data;
    private String message;
    private String traceId;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}