package com.example.job_aggregator.mapper;

import com.example.job_aggregator.model.Job;
import com.example.job_aggregator.resource.JobResource;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JobMapper {

    public JobResource toResource(Job entity) {
        if (entity == null) {
            return null;
        }

        return JobResource.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .company(entity.getCompany())
                .location(entity.getLocation())
                .description(entity.getDescription())
                .sourceUrl(entity.getSourceUrl())
                .jobBoard(entity.getJobBoard())
                .salary(entity.getSalary())
                .scrapedAt(entity.getScrapedAt())
                .build();
    }

    public List<JobResource> toResourceList(List<Job> entities) {
        return entities.stream()
                .map(this::toResource)
                .collect(Collectors.toList());
    }
}
