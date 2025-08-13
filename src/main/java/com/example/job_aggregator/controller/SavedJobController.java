package com.example.job_aggregator.controller;

import com.example.job_aggregator.resource.SavedJobResource;
import com.example.job_aggregator.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Saved Jobs", description = "Saved Job Endpoints")
public class SavedJobController {

    private final JobService jobService;

    // GET /api/saved-jobs
    @Operation(
            summary = "Get user's saved jobs",
            description = "Retrieves all jobs saved by the user. Can be filtered to show only applied jobs. " +
                    "Returns jobs in reverse chronological order (most recently saved first)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved saved jobs",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SavedJobResource.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public List<SavedJobResource> getSavedJobs(
            @RequestParam(defaultValue = "false") boolean appliedOnly) {
        log.info("Fetching saved jobs, appliedOnly: {}", appliedOnly);
        return jobService.getSavedJobs(appliedOnly);
    }

    // POST /api/saved-jobs
    @Operation(
            summary = "Save a job to user's list",
            description = "Saves a job to the user's personal job list for tracking. " +
                    "If the job is already saved, returns the existing saved job without creating a duplicate. " +
                    "The job must exist in the database (from scraping) before it can be saved."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Job saved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SavedJobResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Job not found",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Job not found with ID: dice-123")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // 201 Created
    public SavedJobResource saveJob(@RequestParam String jobId) {
        log.info("Saving job with ID: {}", jobId);
        return jobService.saveJob(jobId);
    }

    // PUT /api/saved-jobs/{id}/apply
    @Operation(
            summary = "Mark saved job as applied",
            description = "Updates the status of a saved job to indicate the user has applied for it. " +
                    "Sets the applied flag to true and records the current timestamp as the application date. " +
                    "This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Job marked as applied successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SavedJobResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Saved job not found",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Saved job not found with ID: 1")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/apply")
    public SavedJobResource markAsApplied(@PathVariable Long id) {
        log.info("Marking job {} as applied", id);
        return jobService.markAsApplied(id);
    }

    // DELETE /api/saved-jobs/{id}
    @Operation(
            summary = "Delete a saved job",
            description = "Removes a job from the user's saved list. " +
                    "This does not delete the job from the main job database, only from the user's saved list. " +
                    "This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Job deleted successfully (no content returned)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Saved job not found",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Saved job not found with ID: 1")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)  // 204 No Content
    public void deleteSavedJob(@PathVariable Long id) {
        log.info("Deleting saved job: {}", id);
        jobService.deleteSavedJob(id);
    }

    // GET /api/saved-jobs/export
    @Operation(
            summary = "Export applied jobs as CSV",
            description = "Generates and downloads a CSV file containing all jobs marked as applied. " +
                    "The CSV includes job title, company, location, job board, salary, application date, and source URL. " +
                    "File is named 'applied-jobs.csv' and downloads immediately."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "CSV file generated successfully",
                    content = @Content(
                            mediaType = "text/csv",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Failed to generate CSV")
    })
    @GetMapping("/export")
    public ResponseEntity<Resource> exportAppliedJobs() {
        log.info("Exporting applied jobs to CSV");
        return jobService.exportAppliedJobsCsv();
    }
}
