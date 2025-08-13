package com.example.job_aggregator.constants;

public class ScrapingConstants {

    public static final String BOARD_DICE = "Dice";
    // URL Constants
    public static final String DICE_BASE_URL = "https://www.dice.com";
    public static final String DICE_SEARCH_URL = "https://www.dice.com/jobs?q=Java+Developer&location=Remote";

    // Timeout
    public static final int SCRAPER_TIMEOUT_MS = 30000;
    public static final int SCRAPING_WAIT_TIMEOUT_SECONDS = 10;

    // CSS Selectors for Dice
    public static final String DICE_JOB_LINK_SELECTOR = "[data-testid='job-search-job-detail-link']";
    public static final String DICE_COMPANY_SELECTOR = "[class='mb-0 line-clamp-2 text-sm sm:line-clamp-1']";
    public static final String DICE_LOCATION_SELECTOR = ".text-sm.font-normal.text-zinc-600";
    public static final String DICE_DESCRIPTION_SELECTOR = ".line-clamp-2.h-10.shrink.grow.basis-0.text-sm.font-normal.text-zinc-900";

    // Regex Patterns
    public static final String DICE_UUID_PATTERN = "/job-detail/([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})";
    public static final String DICE_JOB_ID_PATTERN = "/job-detail/([a-f0-9-]{36})";

    // User Agent
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    // HTTP Headers
    public static final String HEADER_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    public static final String HEADER_ACCEPT_LANGUAGE = "en-US,en;q=0.9";

    // ID Prefixes
    public static final String ID_PREFIX_DICE = "dice-";
    public static final String ID_PREFIX_MOCK = "mock-";


    // DEFAULT USER SETTINGS
    public static final String DEFAULT_USER = "default_user";
    public static final String DEFAULT_USER_PASSWORD = "password";
}
