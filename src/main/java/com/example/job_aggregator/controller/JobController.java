package com.example.job_aggregator.controller;

import com.example.job_aggregator.resource.JobResource;
import com.example.job_aggregator.service.JobService;
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
    @GetMapping
    public List<JobResource> getAllJobs() {
        log.info("Fetching all jobs");
        return jobService.getAllJobs();
    }

    // GET /api/jobs/search
    @GetMapping("/search")
    public List<JobResource> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company) {
        log.info("Searching jobs - keyword: {}, location: {}, company: {}", keyword, location, company);
        return jobService.searchJobs(keyword, location, company);
    }

    // POST /api/jobs/scrape
    @PostMapping("/scrape")
    public String scrapeJobs() {
        log.info("Triggering job scraping");
        return jobService.scrapeJobs();
    }

    // POST /api/jobs/scrape-mock
    @PostMapping("/scrape-mock")
    public String scrapeMockJobs() {
        log.info("Generating mock jobs");
        return jobService.scrapeMockJobs();
    }

}
