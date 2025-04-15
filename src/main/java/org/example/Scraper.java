package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Scraper {
    public void scrapeNews(String keyword, int maxPages, JTextArea resultArea) {
        String driverPath = "D:/UNSIKA/FASILKOM/SEM 6/Karya Tulis Ilmiah/model-scraper/src/main/resources/webdriver/chromedriver.exe";

        File driverFile = new File(driverPath);
        if (!driverFile.exists()) {
            resultArea.append("❌ chromedriver.exe tidak ditemukan di path: " + driverPath + "\n");
            return;
        }

        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        List<String[]> newsList = new ArrayList<>();

        try {
            for (int page = 0; page < maxPages; page++) {
                // ❗ Cek interupsi sebelum scraping tiap halaman
                if (Thread.currentThread().isInterrupted()) {
                    resultArea.append("\n⛔ Proses scraping dihentikan.\n");
                    break;
                }

                int start = page * 10;
                String searchUrl = "https://www.google.com/search?q=" + keyword + "&tbm=nws&start=" + start;

                driver.get(searchUrl);
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class='SoaBEf']")));

                List<WebElement> newsElements = driver.findElements(By.xpath("//div[@class='SoaBEf']"));
                for (WebElement news : newsElements) {
                    if (Thread.currentThread().isInterrupted()) {
                        resultArea.append("\n⛔ Proses scraping dihentikan di tengah halaman.\n");
                        driver.quit();
                        return;
                    }

                    try {
                        String title = news.findElement(By.xpath(".//div[@role='heading']")).getText();
                        String link = news.findElement(By.tagName("a")).getAttribute("href");
                        String source = news.findElement(By.xpath(".//div[contains(@class, 'MgUUmf')]//span")).getText();
                        String date = ETL.convertRelativeDate(news.findElement(By.xpath(".//div[contains(@class, 'OSrXXb')]//span")).getText());
                        String extractedKeywords = ETL.extractKeywords(title, keyword);

                        newsList.add(new String[]{title, source, date, link, extractedKeywords});
                        resultArea.append("✅ " + title + "\n");
                    } catch (NoSuchElementException ex) {
                        resultArea.append("⛔ Elemen tidak ditemukan, melewati...\n");
                    }
                }
            }

            if (!Thread.currentThread().isInterrupted()) {
                String fileName = ETL.getNextFileName("HasilScrap");
                ETL.saveToExcel(newsList, fileName);
                resultArea.append("\n💾 Data berhasil disimpan di " + fileName + "\n");
            }

        } catch (Exception e) {
            resultArea.append("❌ Terjadi kesalahan: " + e.getMessage() + "\n");
        } finally {
            driver.quit();
        }
    }
}
