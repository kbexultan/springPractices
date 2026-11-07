package com.example.demo.practice_5.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Practice 5: turns on Spring's annotation-driven caching.
 * With no cache library on the classpath, Spring Boot uses an in-memory ConcurrentMapCacheManager.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** All books, cached by {@code BookService.findAll()}. */
    public static final String BOOKS = "books";

    /** One book per id, cached by {@code BookService.findById(id)}. */
    public static final String BOOK_BY_ID = "bookById";
}
