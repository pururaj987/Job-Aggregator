package com.example.job_aggregator.service;

import com.example.job_aggregator.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ScraperService {

    // Main method that orchestrates all scrapers
    @Async
    public CompletableFuture<List<Job>> scrapeAllJobBoards() {
        log.info("Starting parallel job scraping from all boards");

        // Run all scrapers in parallel
        CompletableFuture<List<Job>> simplyHiredFuture = scrapeDice();

        // Wait for all to complete and combine results
        return CompletableFuture.allOf(simplyHiredFuture)
                .thenApply(v -> {
                    List<Job> allJobs = new ArrayList<>();
                    try {
                        allJobs.addAll(simplyHiredFuture.get());
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
            String url = "https://www.dice.com/jobs?q=Java+Developer&location=Remote";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(30000)
                    .get();

            log.info("Connected to Dice. Page title: {}", doc.title());

            // Get all job title links
            Elements jobTitleLinks = doc.select("[data-testid='job-search-job-detail-link']");

            log.info("Found {} job cards", jobTitleLinks.size());

            for (Element titleLink : jobTitleLinks) {
                try {
                    Job job = new Job();
                    job.setId("dice-" + UUID.randomUUID());

                    // Get title and URL from the link
                    String title = titleLink.text();
                    String jobUrl = titleLink.attr("href");

                    // Navigate to the parent card container to get other details
                    Element cardContainer = titleLink.closest("article");
                    if (cardContainer == null) {
                        cardContainer = titleLink.parent().parent().parent(); // Go up until we find the card
                    }

                    // Extract company, location, and description using the class names you found
                    String company = cardContainer.select(".mb-0.line-clamp-2.text-sm").text();
                    String location = cardContainer.select(".text-sm.font-normal.text-zinc-600").text();
                    String description = cardContainer.select(".line-clamp-2.h-10.shrink.grow.basis-0.text-sm.font-normal.text-zinc-900").text();

                    // Clean up the data
                    job.setTitle(title.isEmpty() ? "Developer Position" : title.trim());
                    job.setCompany(company.isEmpty() ? "Company Not Listed" : company.trim());
                    job.setLocation(location.isEmpty() ? "Location Not Specified" : location.trim());
                    job.setDescription(description.isEmpty() ? "See job posting for details" : description.trim());
                    job.setJobBoard("Dice");
                    job.setScrapedAt(LocalDateTime.now());
                    job.setSourceUrl(jobUrl.startsWith("http") ? jobUrl : "https://www.dice.com" + jobUrl);

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
    // Scrape WeWork Remotely
    @Async
    public CompletableFuture<List<Job>> scrapeWeWorkRemotely() {
        log.info("Scraping WeWorkRemotely...");
        List<Job> jobs = new ArrayList<>();

        try {
            String url = "https://weworkremotely.com/remote-jobs/search?term=developer";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .timeout(30000)
                    .get();

            // WeWorkRemotely structure
            Elements jobCards = doc.select(".jobs article, li.feature, section.jobs ul li");

            log.info("Found {} job listings", jobCards.size());

            for (Element card : jobCards) {
                try {
                    Job job = new Job();
                    job.setId("wwr-" + UUID.randomUUID().toString());

                    String title = card.select(".title, h2, h3").text();
                    String company = card.select(".company, .company-name").text();
                    String location = "Remote";

                    job.setTitle(title.isEmpty() ? "Remote Position" : title);
                    job.setCompany(company.isEmpty() ? "Company" : company);
                    job.setLocation(location);
                    job.setDescription(card.text().substring(0, Math.min(card.text().length(), 200)));
                    job.setJobBoard("WeWorkRemotely");
                    job.setScrapedAt(LocalDateTime.now());
                    job.setSourceUrl(url);

                    if (!title.isEmpty()) {
                        jobs.add(job);
                    }
                } catch (Exception e) {
                    log.debug("Error parsing job: {}", e.getMessage());
                }
            }

            log.info("Scraped {} jobs from WeWorkRemotely", jobs.size());

        } catch (Exception e) {
            log.error("Error scraping WeWorkRemotely: {}", e.getMessage());
        }

        return CompletableFuture.completedFuture(jobs);
    }

    // Scrape Simply Hired
    @Async
    public CompletableFuture<List<Job>> scrapeSimplyHired() {
        log.info("Scraping SimplyHired...");
        List<Job> jobs = new ArrayList<>();

        try {
            String url = "https://www.simplyhired.com/search?q=software+developer&l=";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .timeout(30000)
                    .get();

            // SimplyHired uses different class names - let's try multiple selectors
            Elements jobCards = doc.select(".SerpJob, .jobposting, article[data-testid='job-card'], .job-listing, [data-id]");

            log.info("Found {} potential job cards", jobCards.size());

            if (jobCards.isEmpty()) {
                log.warn("No job cards found. Page title: {}", doc.title());
                // Debug: log what we're seeing
                Elements allArticles = doc.select("article");
                log.info("Found {} article elements", allArticles.size());
            }

            for (Element card : jobCards) {
                try {
                    Job job = new Job();
                    job.setId("simplyhired-" + UUID.randomUUID().toString());

                    // Try multiple selectors for title
                    String title = card.select(".jobposting-title, h2[data-testid='job-title'], h3[data-testid='job-title'], .jobTitle").text();
                    if (title.isEmpty()) {
                        title = card.select("a[data-testid='job-link']").text();
                    }

                    // Company and location
                    String company = card.select(".JobPosting-labelWithIcon, [data-testid='company-name'], .companyName").text();
                    String location = card.select(".JobPosting-labelWithIcon--location, [data-testid='job-location'], .location").text();
                    String snippet = card.select(".jobposting-snippet, .snippet, .summary").text();

                    job.setTitle(title.isEmpty() ? "Unknown Title" : title);
                    job.setCompany(company.isEmpty() ? "Unknown Company" : company);
                    job.setLocation(location.isEmpty() ? "Unknown Location" : location);
                    job.setDescription(snippet.isEmpty() ? "No description available" : snippet);
                    job.setJobBoard("SimplyHired");
                    job.setScrapedAt(LocalDateTime.now());
                    job.setSourceUrl(url);

                    if (!job.getTitle().equals("Unknown Title")) {
                        jobs.add(job);
                        log.debug("Added job: {} at {}", job.getTitle(), job.getCompany());
                    }
                } catch (Exception e) {
                    log.warn("Error parsing job card: {}", e.getMessage());
                }
            }

            log.info("Successfully scraped {} jobs from SimplyHired", jobs.size());

        } catch (HttpStatusException e) {
            log.error("HTTP error scraping SimplyHired. Status: {}, URL: {}", e.getStatusCode(), e.getUrl());
        } catch (Exception e) {
            log.error("Error scraping SimplyHired: {}", e.getMessage());
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(jobs);
    }

    // Scrape Indeed
    @Async
    public CompletableFuture<List<Job>> scrapeIndeed() {
        log.info("Scraping Indeed...");
        List<Job> jobs = new ArrayList<>();

        try {
            String url = "https://www.indeed.com/jobs?q=software+developer&l=";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements jobCards = doc.select(".job_seen_beacon"); // Indeed's job card class

            for (Element card : jobCards) {
                try {
                    Job job = new Job();
                    job.setId("indeed-" + UUID.randomUUID().toString());
                    job.setTitle(card.select(".jobTitle span[title]").text());
                    job.setCompany(card.select(".companyName").text());
                    job.setLocation(card.select(".companyLocation").text());
                    job.setDescription(card.select(".job-snippet").text());
                    job.setSalary(card.select(".salary-snippet").text());

                    String jobLink = card.select(".jobTitle a").attr("href");
                    job.setSourceUrl("https://www.indeed.com" + jobLink);

                    job.setJobBoard("Indeed");
                    job.setScrapedAt(LocalDateTime.now());

                    if (!job.getTitle().isEmpty()) {
                        jobs.add(job);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing Indeed job card: {}", e.getMessage());
                }
            }

            log.info("Scraped {} jobs from Indeed", jobs.size());

        } catch (Exception e) {
            log.error("Error scraping Indeed", e);
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
            job.setId("mock-" + UUID.randomUUID().toString());
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


}
