package com.example.job_aggregator.repository;

import com.example.job_aggregator.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUserId(Long userId);
    List<SavedJob> findByUserIdAndApplied(Long userId, boolean applied);
    Optional<SavedJob> findByUserIdAndJobId(Long userId, String jobId);
}
