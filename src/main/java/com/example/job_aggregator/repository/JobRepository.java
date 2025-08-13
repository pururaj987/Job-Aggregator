package com.example.job_aggregator.repository;

import com.example.job_aggregator.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, String> {
    List<Job> findByTitleContainingIgnoreCaseOrCompanyContainingIgnoreCase(String title, String company);

    @Query("SELECT j FROM Job j WHERE " +
            "(?1 IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', ?1, '%'))) AND " +
            "(?2 IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', ?2, '%'))) AND " +
            "(?3 IS NULL OR LOWER(j.company) LIKE LOWER(CONCAT('%', ?3, '%')))")
    List<Job> searchJobs(String keyword, String location, String company);

    @Query("SELECT j.id FROM Job j WHERE j.id IN :ids")
    List<String> findAllIdsByIdIn(@Param("ids") Collection<String> ids);
}
