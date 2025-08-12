package com.example.job_aggregator.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "jobs")
public class Job {
    @Id
    private String id;
    private String title;
    private String company;
    private String location;
    @Column(length = 5000)
    private String description;
    private String sourceUrl;
    private String jobBoard;
    private LocalDateTime scrapedAt;
    private String salary;
}
