package com.urlshortener.url_shortener.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "click_events")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String shortCode;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    @Column
    private String deviceType;

    @Column
    private String browser;

    @Column
    private String country;

    @PrePersist
    public void prePersist() {
        this.clickedAt = LocalDateTime.now();
    }
}