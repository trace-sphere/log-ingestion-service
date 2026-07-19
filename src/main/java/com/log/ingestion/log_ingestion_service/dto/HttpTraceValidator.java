package com.log.ingestion.log_ingestion_service.dto;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(toBuilder = true)
public class HttpTraceValidator {

    @NotNull(message = "{http.method.not.null}")
    @NotBlank(message = "{http.method.not.blank}")
    private String method;

    @NotNull(message = "{http.path.not.null}")
    @NotBlank(message = "{http.path.not.blank}")
    private String path;

    @NotNull(message = "{http.status.not.null}")
    @NotBlank(message = "{http.status.not.blank}")
    @Pattern(regexp = "^[1-5][0-9]{2}$", message = "{http.status.invalid.pattern}")
    private String status;

    @Min(value = 0, message = "{http.duration.invalid}")
    @Max(value = Integer.MAX_VALUE, message = "{http.duration.invalid}")
    private Integer durationMs;

    @NotNull(message = "{http.clientIp.not.null}")
    @NotBlank(message = "{http.client.not.blank}")
    @Pattern(regexp = "\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b", message = "{http.clientIp.invalid.pattern}")
    private String clientIp;

    @NotNull(message = "{http.user.agent.not.null}")
    @NotBlank(message = "{http.user.agent.not.blank}")
    private String userAgent;
}
