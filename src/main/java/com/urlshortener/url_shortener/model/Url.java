package com.urlshortener.url_shortener.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "urls")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false, unique = true, length = 15)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Long totalClicks = 0L;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}