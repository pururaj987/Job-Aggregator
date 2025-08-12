package com.example.job_aggregator.mapper;

import com.example.job_aggregator.model.SavedJob;
import com.example.job_aggregator.resource.SavedJobResource;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SavedJobMapper {

    private final JobMapper jobMapper;

    public SavedJobResource toResource(SavedJob entity) {
        if (entity == null) {
            return null;
        }

        return SavedJobResource.builder()
                .id(entity.getId())
                .job(jobMapper.toResource(entity.getJob()))
                .savedAt(entity.getSavedAt())
                .applied(entity.isApplied())
                .appliedAt(entity.getAppliedAt())
                .build();
    }

    public List<SavedJobResource> toResourceList(List<SavedJob> entities) {
        return entities.stream()
                .map(this::toResource)
                .collect(Collectors.toList());
    }
}
