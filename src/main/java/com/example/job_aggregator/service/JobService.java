package com.example.job_aggregator.service;

import com.example.job_aggregator.exception.ResourceNotFoundException;
import com.example.job_aggregator.mapper.JobMapper;
import com.example.job_aggregator.mapper.SavedJobMapper;
import com.example.job_aggregator.model.Job;
import com.example.job_aggregator.model.SavedJob;
import com.example.job_aggregator.model.User;
import com.example.job_aggregator.repository.JobRepository;
import com.example.job_aggregator.repository.SavedJobRepository;
import com.example.job_aggregator.repository.UserRepository;
import com.example.job_aggregator.resource.JobResource;
import com.example.job_aggregator.resource.SavedJobResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import com.opencsv.CSVWriter;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.job_aggregator.constants.ScrapingConstants.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;
    private final SavedJobMapper savedJobMapper;
    private final ScraperService scraperService;  // Injected ScraperService

    // Search jobs with filters
    public List<JobResource> searchJobs(String keyword, String location, String company) {
        log.info("Searching jobs with keyword: {}, location: {}, company: {}", keyword, location, company);
        List<Job> jobs = jobRepository.searchJobs(keyword, location, company);
        return jobMapper.toResourceList(jobs);
    }

    // Get all jobs from database
    public List<JobResource> getAllJobs() {
        log.info("Fetching all jobs from database");
        List<Job> jobs = jobRepository.findAll();
        return jobMapper.toResourceList(jobs);
    }

    // Save a job for user
    public SavedJobResource saveJob(String jobId) {
        log.info("Saving job with ID: {}", jobId);

        // Find by username instead of hardcoded ID
        User user = userRepository.findByUsername("default_user")
                .orElseGet(() -> createDefaultUser());

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        // Check if already saved
        SavedJob existingSave = savedJobRepository.findByUserIdAndJobId(user.getId(), jobId)
                .orElse(null);

        if (existingSave != null) {
            log.info("Job already saved for user");
            return savedJobMapper.toResource(existingSave);
        }

        SavedJob savedJob = new SavedJob();
        savedJob.setUser(user);
        savedJob.setJob(job);
        savedJob.setSavedAt(LocalDateTime.now());
        savedJob.setApplied(false);

        savedJob = savedJobRepository.save(savedJob);
        log.info("Job saved successfully");

        return savedJobMapper.toResource(savedJob);
    }

    // Get user's saved jobs
    public List<SavedJobResource> getSavedJobs(boolean appliedOnly) {
        log.info("Fetching saved jobs, appliedOnly: {}", appliedOnly);

        // Using default user for now
        Long userId = 1L;

        List<SavedJob> savedJobs;
        if (appliedOnly) {
            savedJobs = savedJobRepository.findByUserIdAndApplied(userId, true);
        } else {
            savedJobs = savedJobRepository.findByUserId(userId);
        }

        return savedJobMapper.toResourceList(savedJobs);
    }

    // Mark job as applied
    public SavedJobResource markAsApplied(Long savedJobId) {
        log.info("Marking saved job {} as applied", savedJobId);

        SavedJob savedJob = savedJobRepository.findById(savedJobId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved job not found with ID: " + savedJobId));

        savedJob.setApplied(true);
        savedJob.setAppliedAt(LocalDateTime.now());
        savedJob = savedJobRepository.save(savedJob);

        log.info("Job marked as applied");
        return savedJobMapper.toResource(savedJob);
    }

    // Delete a saved job
    public void deleteSavedJob(Long savedJobId) {
        log.info("Deleting saved job with ID: {}", savedJobId);

        SavedJob savedJob = savedJobRepository.findById(savedJobId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved job not found with ID: " + savedJobId));

        savedJobRepository.delete(savedJob);
        log.info("Saved job deleted successfully");
    }

    // Export applied jobs as CSV
    public ResponseEntity<Resource> exportAppliedJobsCsv() {
        log.info("Exporting applied jobs to CSV");

        Long userId = 1L; // Default user
        List<SavedJob> appliedJobs = savedJobRepository.findByUserIdAndApplied(userId, true);

        try {
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = getCsvWriter(writer, appliedJobs);
            csvWriter.close();

            ByteArrayResource resource = new ByteArrayResource(writer.toString().getBytes());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=applied-jobs.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error exporting CSV", e);
            throw new RuntimeException("Failed to export CSV: " + e.getMessage());
        }
    }

    private static CSVWriter getCsvWriter(StringWriter writer, List<SavedJob> appliedJobs) {
        CSVWriter csvWriter = new CSVWriter(writer);

        // Write header
        String[] header = {"Job Title", "Company", "Location", "Job Board", "Salary", "Applied Date", "Source URL"};
        csvWriter.writeNext(header);

        // Write data
        for (SavedJob savedJob : appliedJobs) {
            Job job = savedJob.getJob();
            String[] row = {
                    job.getTitle(),
                    job.getCompany(),
                    job.getLocation(),
                    job.getJobBoard(),
                    job.getSalary() != null ? job.getSalary() : "Not specified",
                    savedJob.getAppliedAt() != null ? savedJob.getAppliedAt().toString() : "",
                    job.getSourceUrl()
            };
            csvWriter.writeNext(row);
        }
        return csvWriter;
    }

    // Trigger job scraping - delegates to ScraperService
    @Transactional
    public String scrapeJobs() {
        log.info("Triggering job scraping process. Will take 10 seconds");

        CompletableFuture<List<Job>> future = scraperService.scrapeAllJobBoards();

        try {
            List<Job> scrapedJobs = future.get(SCRAPING_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (scrapedJobs.isEmpty()) {
                return "Scraping completed but no jobs found.";
            }

            // Extract all IDs from scraped jobs
            Set<String> scrapedIds = scrapedJobs.stream()
                    .map(Job::getId)
                    .collect(Collectors.toSet());

            // Find which IDs already exist in DB (single query)
            Set<String> existingIds = new HashSet<>(jobRepository.findAllIdsByIdIn(scrapedIds));

            // Filter to get only new jobs
            List<Job> newJobs = scrapedJobs.stream()
                    .filter(job -> !existingIds.contains(job.getId()))
                    .collect(Collectors.toList());

            // Save new jobs
            if (!newJobs.isEmpty()) {
                jobRepository.saveAll(newJobs);
            }

            int savedCount = newJobs.size();
            int duplicateCount = scrapedJobs.size() - savedCount;

            log.info("Saved {} new jobs, skipped {} duplicates", savedCount, duplicateCount);

            return String.format("Scraping completed! Saved %d new jobs, skipped %d duplicates.",
                    savedCount, duplicateCount);

        } catch (Exception e) {
            log.error("Error during scraping", e);
            return "Scraping failed: " + e.getMessage();
        }
    }

    // Trigger mock job scraping for testing
    @Transactional
    public String scrapeMockJobs() {
        log.info("Triggering mock job scraping for testing");

        CompletableFuture<List<Job>> future = scraperService.scrapeMockJobs();

        future.thenAccept(jobs -> {
            log.info("Received {} mock jobs, saving to database", jobs.size());
            jobRepository.saveAll(jobs);
            log.info("Mock jobs saved successfully");
        }).exceptionally(ex -> {
            log.error("Error saving mock jobs", ex);
            return null;
        });

        return "Mock job scraping initiated";
    }

    // Helper method to create default user
    private User createDefaultUser() {
        log.info("Creating or finding default user");

        // First check if ANY user exists
        Optional<User> existingUser = userRepository.findByUsername(DEFAULT_USER);
        if (existingUser.isPresent()) {
            log.info("Default user already exists");
            return existingUser.get();
        }

        // Only create if doesn't exist
        User user = new User();
        user.setUsername(DEFAULT_USER);
        user.setPassword(DEFAULT_USER_PASSWORD);
        User savedUser = userRepository.save(user);
        log.info("Created default user with ID: {}", savedUser.getId());
        return savedUser;
    }

}