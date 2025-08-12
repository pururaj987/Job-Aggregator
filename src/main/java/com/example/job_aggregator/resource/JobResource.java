package com.example.job_aggregator.resource;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobResource {
    private String id;
    private String title;
    private String company;
    private String location;
    private String description;
    private String sourceUrl;
    private String jobBoard;
    private String salary;
    private LocalDateTime scrapedAt;
}
