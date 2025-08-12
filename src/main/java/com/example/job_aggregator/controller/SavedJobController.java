package com.example.job_aggregator.controller;

import com.example.job_aggregator.resource.SavedJobResource;
import com.example.job_aggregator.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/saved-jobs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Jobs", description = "Saved Job Endpoints")
public class SavedJobController {

    private final JobService jobService;

    // GET /api/saved-jobs
    @GetMapping
    public List<SavedJobResource> getSavedJobs(
            @RequestParam(defaultValue = "false") boolean appliedOnly) {
        log.info("Fetching saved jobs, appliedOnly: {}", appliedOnly);
        return jobService.getSavedJobs(appliedOnly);
    }

    // POST /api/saved-jobs
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // 201 Created
    public SavedJobResource saveJob(@RequestParam String jobId) {
        log.info("Saving job with ID: {}", jobId);
        return jobService.saveJob(jobId);
    }

    // PUT /api/saved-jobs/{id}/apply
    @PutMapping("/{id}/apply")
    public SavedJobResource markAsApplied(@PathVariable Long id) {
        log.info("Marking job {} as applied", id);
        return jobService.markAsApplied(id);
    }

    // DELETE /api/saved-jobs/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)  // 204 No Content
    public void deleteSavedJob(@PathVariable Long id) {
        log.info("Deleting saved job: {}", id);
        jobService.deleteSavedJob(id);
    }

    // GET /api/saved-jobs/export
    // Only using ResponseEntity here because we need custom headers for file download
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAppliedJobs() {
        log.info("Exporting applied jobs to CSV");
        return jobService.exportAppliedJobsCsv();
    }
}
