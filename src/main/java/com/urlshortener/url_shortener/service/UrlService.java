package com.urlshortener.url_shortener.service;

import com.urlshortener.url_shortener.model.ClickEvent;
import com.urlshortener.url_shortener.model.Url;
import com.urlshortener.url_shortener.repository.ClickEventRepository;
import com.urlshortener.url_shortener.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private ClickEventRepository clickEventRepository;

    // Multithreading — background thread pool for logging clicks
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    // ✅ METHOD 1 — Shorten a URL
    public Url shortenUrl(String originalUrl, String customAlias, Integer expiryDays) {

        String shortCode;

        if (customAlias != null && !customAlias.isBlank()) {

            // Validation 1 — Too long
            if (customAlias.length() > 15) {
                throw new RuntimeException(
                        "Alias too long! Maximum 15 characters allowed.");
            }

            // Validation 2 — Too short
            if (customAlias.length() < 3) {
                throw new RuntimeException(
                        "Alias too short! Minimum 3 characters required.");
            }

            // Validation 3 — Invalid characters
            if (!customAlias.matches("[a-zA-Z0-9-]+")) {
                throw new RuntimeException(
                        "Alias can only contain letters, numbers and hyphens!");
            }

            // Validation 4 — Already taken
            if (urlRepository.existsByShortCode(customAlias)) {
                throw new RuntimeException(
                        "This alias is already taken! Try another.");
            }

            shortCode = customAlias;

        } else {
            // No custom alias — generate random one
            shortCode = generateShortCode();
        }

        Url url = new Url();
        url.setOriginalUrl(originalUrl);
        url.setShortCode(shortCode);
        url.setTotalClicks(0L);

        // Set expiry date if provided
        if (expiryDays != null && expiryDays > 0) {
            url.setExpiryDate(LocalDateTime.now().plusDays(expiryDays));
        }

        return urlRepository.save(url);
    }

    // ✅ METHOD 2 — Get original URL by short code
    public Optional<Url> getUrlByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .filter(url -> {
                    // Check if link is expired
                    if (url.getExpiryDate() != null &&
                            url.getExpiryDate().isBefore(LocalDateTime.now())) {
                        return false; // expired!
                    }
                    return true; // valid
                });
    }

    // ✅ METHOD 3 — Log a click in background (Multithreading!)
    public void logClick(String shortCode, String deviceType, String browser) {
        executorService.submit(() -> {
            ClickEvent event = new ClickEvent();
            event.setShortCode(shortCode);
            event.setDeviceType(deviceType);
            event.setBrowser(browser);
            clickEventRepository.save(event);

            // Update total clicks count
            urlRepository.findByShortCode(shortCode).ifPresent(url -> {
                url.setTotalClicks(url.getTotalClicks() + 1);
                urlRepository.save(url);
            });
        });
    }

    // ✅ METHOD 4 — Get all clicks for a short code (for analytics)
    public List<ClickEvent> getClicksByShortCode(String shortCode) {
        return clickEventRepository.findByShortCode(shortCode);
    }

    // ✅ METHOD 5 — Get total clicks count
    public Long getTotalClicks(String shortCode) {
        return clickEventRepository.countByShortCode(shortCode);
    }

    // ✅ METHOD 6 — Get all URLs (for dashboard)
    public List<Url> getAllUrls() {
        return urlRepository.findAll();
    }

    // ✅ METHOD 7 — Delete a URL
    public void deleteUrl(String shortCode) {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));
        clickEventRepository.deleteAll(
                clickEventRepository.findByShortCode(shortCode));
        urlRepository.delete(url);
    }

    // 🔧 HELPER — Generate a unique 6 character short code
    private String generateShortCode() {
        String characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        String shortCode;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                sb.append(characters.charAt(random.nextInt(characters.length())));
            }
            shortCode = sb.toString();
        } while (urlRepository.existsByShortCode(shortCode));

        return shortCode;
    }
}