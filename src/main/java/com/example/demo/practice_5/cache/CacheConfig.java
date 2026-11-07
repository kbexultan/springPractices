package com.example.demo.practice_5.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String BOOKS = "books";

    public static final String BOOK_BY_ID = "bookById";
}
