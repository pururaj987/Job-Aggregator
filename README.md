# 🚀 Job Aggregator API

A high-performance RESTful backend service that asynchronously scrapes job listings from multiple job boards, allowing users to save, manage, track, and export job applications.

## ✨ Features

- **🔄 Asynchronous Job Scraping**: Parallel scraping from multiple job boards using CompletableFuture
- **🌐 Real-time Data**: Scrapes actual job listings from Dice.com
- **🔍 Smart Deduplication**: Intelligent job ID extraction prevents duplicate entries
- **💾 Job Management**: Save jobs, mark as applied, track application status
- **📊 CSV Export**: Export applied jobs to CSV format for tracking
- **🐳 Dockerized**: Fully containerized application for easy deployment

## 🛠 Tech Stack

- **Backend**: Spring Boot 3.x, Java 17
- **Database**: H2 (In-memory)
- **Web Scraping**: JSoup
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## 📦 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose (for containerized deployment)

## 🚀 Quick Start

### Using Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/job-aggregator.git
cd job-aggregator

# Run with Docker Compose
docker-compose up -d

# Application will be available at:
# - API: http://localhost:8080/api
# - Swagger UI: http://localhost:8080/swagger-ui
# - H2 Console: http://localhost:8080/h2-console

## 🚀 Quick Start - Sample Flow

Follow this step-by-step guide to test all features of the Job Aggregator API:

### Step 1: Start the Application

```bash
# Using Docker (recommended)
docker-compose up -d

# OR using Maven
mvn spring-boot:run
```

Wait for the application to start completely. Verify it's running:
```bash
curl http://localhost:8080/api/actuator/health
# Should return: {"status":"UP"}
```

### Step 2: Generate Sample Jobs

First, let's populate the database with some test jobs:

```bash
# Generate 10 mock jobs for testing
curl -X POST http://localhost:8080/api/jobs/scrape-mock

# Response: "Scraping completed! Saved 10 new jobs, skipped 0 duplicates."
```

### Step 3: Trigger Real Job Scraping

Scrape actual jobs from Dice.com:

```bash
# This may take up to 30 seconds
curl -X POST http://localhost:8080/api/jobs/scrape

# Response: "Scraping completed! Saved 25 new jobs, skipped 0 duplicates."
```

### Step 4: View All Available Jobs

```bash
# Get all jobs in the database
curl http://localhost:8080/api/jobs

# Returns JSON array of jobs:
# [
#   {
#     "id": "dice-79b3eb0a-19e5-4cb6-ae05-6dfe4b6d000f",
#     "title": "Senior Java Developer",
#     "company": "Tech Corp",
#     "location": "New York, NY",
#     "jobBoard": "Dice",
#     ...
#   },
#   ...
# ]
```

### Step 5: Search for Specific Jobs

```bash
# Search for Java jobs in Remote locations
curl "http://localhost:8080/api/jobs/search?keyword=java&location=remote"

# Returns filtered list of jobs matching criteria
```

### Step 6: Save a Job

Pick a job ID from the previous response and save it:

```bash
# Save a job to your personal list (replace with actual job ID)
curl -X POST "http://localhost:8080/api/saved-jobs?jobId=dice-79b3eb0a-19e5-4cb6-ae05-6dfe4b6d000f"

# Response:
# {
#   "id": 1,
#   "job": { ... },
#   "savedAt": "2024-01-15T10:30:00",
#   "applied": false,
#   "appliedAt": null
# }
```

### Step 7: View Your Saved Jobs

```bash
# Get all your saved jobs
curl http://localhost:8080/api/saved-jobs

# Returns array of saved jobs with their status
```

### Step 8: Mark Job as Applied

```bash
# Mark saved job #1 as applied. You can get the saved jobs ids from the previous response
curl -X PUT http://localhost:8080/api/saved-jobs/1/apply

# Response shows updated job with applied=true and appliedAt timestamp
```

### Step 9: View Only Applied Jobs

```bash
# Filter to see only jobs you've applied to
curl "http://localhost:8080/api/saved-jobs?appliedOnly=true"

# Returns only jobs where applied=true
```

### Step 10: Export Applied Jobs to CSV

```bash
# Download CSV file of all applied jobs
curl -O -J http://localhost:8080/api/saved-jobs/export

# This downloads 'applied-jobs.csv' to your current directory
# Open the file to see your application history
```

## 📊 Complete Test Scenario

Here's a complete workflow combining all features:

```bash
# 1. Start fresh - generate mock data
curl -X POST http://localhost:8080/api/jobs/scrape-mock

# 2. Scrape real jobs from Dice.com
curl -X POST http://localhost:8080/api/jobs/scrape

# 3. Search for specific jobs
curl "http://localhost:8080/api/jobs/search?keyword=senior&location=remote"

# 4. Save interesting jobs (use actual IDs from step 3)
curl -X POST "http://localhost:8080/api/saved-jobs?jobId=mock-seniorjavadeveloper-techcorp-0"
curl -X POST "http://localhost:8080/api/saved-jobs?jobId=dice-abc123-def456"

# 5. View your saved jobs
curl http://localhost:8080/api/saved-jobs

# 6. After applying externally, mark as applied
curl -X PUT http://localhost:8080/api/saved-jobs/1/apply
curl -X PUT http://localhost:8080/api/saved-jobs/2/apply

# 7. Export your applications for record keeping
curl -O -J http://localhost:8080/api/saved-jobs/export
```

## 🧪 Testing with Swagger UI

For a more interactive experience, use Swagger UI:

1. Open browser and navigate to: `http://localhost:8080/swagger-ui/index.html`
2. Click on any endpoint to expand it
3. Click "Try it out"
4. Fill in parameters if needed
5. Click "Execute"
6. View the response directly in the browser

## 🔄 Duplicate Prevention Test

Test the deduplication feature:

```bash
# First scraping - saves all jobs
curl -X POST http://localhost:8080/api/jobs/scrape
# Response: "Scraping completed! Saved 25 new jobs, skipped 0 duplicates."

# Wait 1 minute, then scrape again
curl -X POST http://localhost:8080/api/jobs/scrape
# Response: "Scraping completed! Saved 0 new jobs, skipped 25 duplicates."
```

This demonstrates that the application intelligently prevents duplicate jobs from being saved.

## ✅ Expected Results

After following this flow, you should have:
- **35+ jobs** in the database (10 mock + 25+ from Dice)
- **2-3 saved jobs** in your personal list
- **2 applied jobs** marked with timestamps
- **1 CSV file** with your application history
- **Proven deduplication** preventing duplicate entries

## 🎯 Key Features Demonstrated

This flow demonstrates all core features:
- ✅ Asynchronous scraping from multiple sources
- ✅ Real-time job data from Dice.com
- ✅ Smart deduplication using unique IDs
- ✅ Personal job management (save, apply, delete)
- ✅ CSV export for record keeping
- ✅ Search and filtering capabilities

