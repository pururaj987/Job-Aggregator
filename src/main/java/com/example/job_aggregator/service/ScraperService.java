package com.example.job_aggregator.service;

import com.example.job_aggregator.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.job_aggregator.constants.ScrapingConstants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScraperService {

    // Main method that orchestrates all scrapers
    @Async
    public CompletableFuture<List<Job>> scrapeAllJobBoards() {
        log.info("Starting parallel job scraping from all boards");

        // Run all scrapers in parallel
        CompletableFuture<List<Job>> diceFuture = scrapeDice();

        // Wait for all to complete and combine results
        return CompletableFuture.allOf(diceFuture)
                .thenApply(v -> {
                    List<Job> allJobs = new ArrayList<>();
                    try {
                        allJobs.addAll(diceFuture.get());
                        log.info("Total jobs scraped: {}", allJobs.size());
                    } catch (Exception e) {
                        log.error("Error combining scraping results", e);
                    }
                    return allJobs;
                });
    }

    // Scrape Dice.com
    @Async
    public CompletableFuture<List<Job>> scrapeDice() {
        log.info("Scraping Dice.com...");
        List<Job> jobs = new ArrayList<>();

        try {

            Document doc = Jsoup.connect(DICE_SEARCH_URL)
                    .userAgent(USER_AGENT)
                    .header("Accept", HEADER_ACCEPT)
                    .header("Accept-Language", HEADER_ACCEPT_LANGUAGE)
                    .timeout(SCRAPER_TIMEOUT_MS)
                    .get();

            log.info("Connected to Dice. Page title: {}", doc.title());

            // Get all job title links
            Elements jobTitleLinks = doc.select(DICE_JOB_LINK_SELECTOR);

            log.info("Found {} job cards", jobTitleLinks.size());

            for (Element titleLink : jobTitleLinks) {
                try {
                    Job job = new Job();
                    // Get title and URL from the link
                    String title = titleLink.text();
                    String jobUrl = titleLink.attr("href");
                    String jobId = extractJobIdFromUrl(jobUrl);
                    job.setId(ID_PREFIX_DICE + jobId);

                    // Navigate to the parent card container to get other details
                    Element cardContainer = titleLink.closest("article");
                    if (cardContainer == null) {
                        cardContainer = titleLink.parent().parent().parent().parent(); // Go up until we find the card
                    }

                    // Extract company, location, and description using the class names you found
                    // Use attribute selector to match the exact class
                    String company = cardContainer.select(DICE_COMPANY_SELECTOR).text();
                    // Get only the first element with this class
                    String location = cardContainer.select(DICE_LOCATION_SELECTOR).first().text();
                    String description = cardContainer.select(DICE_DESCRIPTION_SELECTOR).text();

                    // Clean up the data
                    job.setTitle(title.isEmpty() ? "Developer Position" : title.trim());
                    job.setCompany(company.isEmpty() ? "Company Not Listed" : company.trim());
                    job.setLocation(location.isEmpty() ? "Location Not Specified" : location.trim());
                    job.setDescription(description.isEmpty() ? "See job posting for details" : description.trim());
                    job.setJobBoard(BOARD_DICE);
                    job.setScrapedAt(LocalDateTime.now());
                    job.setSourceUrl(jobUrl.startsWith("http") ? jobUrl : DICE_BASE_URL + jobUrl);

                    jobs.add(job);

                    log.debug("Added job: {} at {} in {}", title, company, location);

                } catch (Exception e) {
                    log.warn("Error parsing individual job card: {}", e.getMessage());
                }
            }

            log.info("Successfully scraped {} jobs from Dice", jobs.size());

        } catch (Exception e) {
            log.error("Error scraping Dice: {}", e.getMessage());
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(jobs);
    }

    // Mock scraper for testing (when real sites block you)
    @Async
    public CompletableFuture<List<Job>> scrapeMockJobs() {
        log.info("Generating mock jobs for testing");
        List<Job> jobs = new ArrayList<>();

        String[] titles = {"Senior Java Developer", "Python Engineer", "Full Stack Developer", "DevOps Engineer", "Data Scientist"};
        String[] companies = {"Google", "Microsoft", "Amazon", "Meta", "Apple"};
        String[] locations = {"New York", "San Francisco", "Seattle", "Austin", "Boston"};

        for (int i = 0; i < 10; i++) {
            Job job = new Job();
            job.setId(ID_PREFIX_MOCK + UUID.randomUUID());
            job.setTitle(titles[i % titles.length]);
            job.setCompany(companies[i % companies.length]);
            job.setLocation(locations[i % locations.length]);
            job.setDescription("This is a mock job description for testing purposes. Required skills include Java, Spring Boot, and REST APIs.");
            job.setSalary("$100,000 - $150,000");
            job.setSourceUrl("https://example.com/job/" + i);
            job.setJobBoard("MockBoard");
            job.setScrapedAt(LocalDateTime.now());
            jobs.add(job);
        }

        log.info("Generated {} mock jobs", jobs.size());
        return CompletableFuture.completedFuture(jobs);
    }

    // Extract UUID from Dice URL
    private String extractJobIdFromUrl(String url) {
        // Pattern to match UUID after /job-detail/
        // Example: /job-detail/79b3eb0a-19e5-4cb6-ae05-6dfe4b6d000f
        Pattern pattern = Pattern.compile(DICE_JOB_ID_PATTERN);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);  // Returns: 79b3eb0a-19e5-4cb6-ae05-6dfe4b6d000f
        }

        // Fallback: try to find any UUID pattern in the URL
        Pattern uuidPattern = Pattern.compile(DICE_UUID_PATTERN);
        Matcher uuidMatcher = uuidPattern.matcher(url);

        if (uuidMatcher.find()) {
            return uuidMatcher.group(1);
        }

        return null;
    }


}
