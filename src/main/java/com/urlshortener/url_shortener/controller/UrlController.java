package com.urlshortener.url_shortener.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.urlshortener.url_shortener.model.Url;
import com.urlshortener.url_shortener.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UrlController {

    @Autowired
    private UrlService urlService;

    // ✅ API 1 — Shorten a URL
    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody Map<String, String> request) {
        String originalUrl = request.get("originalUrl");
        String customAlias = request.get("customAlias");
        String expiryDaysStr = request.get("expiryDays");

        if (originalUrl == null || originalUrl.isBlank()) {
            return ResponseEntity.badRequest().body("URL cannot be empty");
        }

        try {
            Integer expiryDays = null;
            if (expiryDaysStr != null && !expiryDaysStr.isBlank()) {
                expiryDays = Integer.parseInt(expiryDaysStr);
            }
            Url savedUrl = urlService.shortenUrl(originalUrl, customAlias, expiryDays);
            return ResponseEntity.ok(savedUrl);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Expiry days must be a number");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ API 2 — Redirect short URL to original
    @GetMapping("/r/{shortCode}")
    public ResponseEntity<?> redirectUrl(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        return urlService.getUrlByShortCode(shortCode)
                .map(url -> {
                    String userAgent = request.getHeader("User-Agent");
                    String deviceType = userAgent != null && userAgent.toLowerCase()
                            .contains("mobile") ? "Mobile" : "Desktop";
                    String browser = detectBrowser(userAgent);

                    urlService.logClick(shortCode, deviceType, browser);

                    return ResponseEntity.status(HttpStatus.FOUND)
                            .header("Location", url.getOriginalUrl())
                            .build();
                })
                .orElse(ResponseEntity.status(HttpStatus.GONE)
                        .body("This link has expired or does not exist!"));
    }

    // ✅ API 3 — Get analytics for a short code
    @GetMapping("/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        return urlService.getUrlByShortCode(shortCode)
                .map(url -> {
                    Map<String, Object> analytics = Map.of(
                            "shortCode", url.getShortCode(),
                            "originalUrl", url.getOriginalUrl(),
                            "totalClicks", url.getTotalClicks(),
                            "clicks", urlService.getClicksByShortCode(shortCode),
                            "createdAt", url.getCreatedAt()
                    );
                    return ResponseEntity.ok(analytics);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ API 4 — Get all URLs
    @GetMapping("/urls")
    public ResponseEntity<?> getAllUrls() {
        return ResponseEntity.ok(urlService.getAllUrls());
    }

    // ✅ API 5 — Delete a URL
    @DeleteMapping("/urls/{shortCode}")
    public ResponseEntity<?> deleteUrl(@PathVariable String shortCode) {
        try {
            urlService.deleteUrl(shortCode);
            return ResponseEntity.ok("URL deleted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ API 6 — Generate QR Code for a short URL
    @GetMapping(value = "/qr/{shortCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> generateQrCode(@PathVariable String shortCode) {
        return urlService.getUrlByShortCode(shortCode)
                .map(url -> {
                    try {
                        String shortUrl = "http://localhost:8080/api/r/" + shortCode;

                        // Generate QR Code
                        QRCodeWriter qrCodeWriter = new QRCodeWriter();
                        BitMatrix bitMatrix = qrCodeWriter.encode(
                                shortUrl,
                                BarcodeFormat.QR_CODE,
                                250,
                                250
                        );

                        // Convert to PNG image bytes
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        MatrixToImageWriter.writeToStream(
                                bitMatrix, "PNG", outputStream);
                        byte[] qrBytes = outputStream.toByteArray();

                        return ResponseEntity.ok()
                                .contentType(MediaType.IMAGE_PNG)
                                .body(qrBytes);

                    } catch (WriterException | IOException e) {
                        return ResponseEntity.internalServerError()
                                .body("Failed to generate QR code".getBytes());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔧 HELPER — Detect browser from User-Agent
    private String detectBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("chrome")) return "Chrome";
        if (userAgent.contains("firefox")) return "Firefox";
        if (userAgent.contains("safari")) return "Safari";
        if (userAgent.contains("edge")) return "Edge";
        return "Other";
    }
}