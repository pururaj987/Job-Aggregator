package com.example.job_aggregator.resource;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobResource {
    private Long id;
    private JobResource job;
    private LocalDateTime savedAt;
    private boolean applied;
    private LocalDateTime appliedAt;
}