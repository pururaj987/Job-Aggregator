package com.example.job_aggregator.controller;

import com.example.job_aggregator.resource.JobResource;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Jobs", description = "Job management endpoints")
public class JobController {

    private final JobService jobService;

    // GET /api/jobs
    @Operation(
            summary = "Get all jobs",
            description = "Retrieves all jobs currently stored in the database. Returns an empty array if no jobs are found."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved all jobs",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobResource.class)
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public List<JobResource> getAllJobs() {
        log.info("Fetching all jobs");
        return jobService.getAllJobs();
    }

    // GET /api/jobs/search
    @Operation(
            summary = "Search jobs with filters",
            description = "Search for jobs using optional filters. All parameters are optional and can be combined. " +
                    "Search is case-insensitive and uses partial matching."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved filtered jobs",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = JobResource.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public List<JobResource> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company) {
        log.info("Searching jobs - keyword: {}, location: {}, company: {}", keyword, location, company);
        return jobService.searchJobs(keyword, location, company);
    }

    // POST /api/jobs/scrape
    @Operation(
            summary = "Scrape real jobs from Dice.com",
            description = "Triggers asynchronous job scraping from Dice.com. This operation may take up to 30 seconds. " +
                    "The scraper will automatically prevent duplicate jobs from being saved. " +
                    "Returns a summary of jobs scraped and duplicates skipped."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Scraping completed successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Scraping completed! Saved 25 new jobs, skipped 5 duplicates.")
                    )
            ),
            @ApiResponse(
                    responseCode = "408",
                    description = "Scraping timeout",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Scraping is taking longer than expected (>30s). Jobs will be saved once complete.")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Scraping failed")
    })
    @PostMapping("/scrape")
    public String scrapeJobs() {
        log.info("Triggering job scraping");
        return jobService.scrapeJobs();
    }

    // POST /api/jobs/scrape-mock
    @Operation(
            summary = "Generate mock jobs for testing",
            description = "Creates 10 sample jobs with realistic data for testing purposes. " +
                    "Useful for testing the application without relying on external websites. " +
                    "Mock jobs have consistent IDs to prevent duplicates on multiple runs."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Mock jobs generated successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Scraping completed! Saved 10 new jobs, skipped 0 duplicates.")
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Failed to generate mock jobs")
    })
    @PostMapping("/scrape-mock")
    public String scrapeMockJobs() {
        log.info("Generating mock jobs");
        return jobService.scrapeMockJobs();
    }

}
