package com.urlshortener.url_shortener.repository;

import com.urlshortener.url_shortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    List<ClickEvent> findByShortCode(String shortCode);

    Long countByShortCode(String shortCode);
}