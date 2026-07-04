package com.rbc.holidays.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response object for file upload operation")
public class FileUploadResponse {

    @Schema(description = "Total number of records in the uploaded file", example = "10")
    private Integer totalRecords;

    @Schema(description = "Number of successfully processed records", example = "8")
    private Integer successCount;

    @Schema(description = "Number of failed records", example = "2")
    private Integer failureCount;

    @Schema(description = "List of error messages for failed records")
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Schema(description = "Overall upload status message", example = "File uploaded successfully with 8 records")
    private String message;
}
