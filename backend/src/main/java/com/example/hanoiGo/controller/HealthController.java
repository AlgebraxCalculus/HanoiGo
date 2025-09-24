package com.example.hanoiGo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/db")
    public String checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return "✅ Database connection successful! " +
                   "Connected to: " + connection.getMetaData().getURL();
        } catch (Exception e) {
            return "❌ Database connection failed: " + e.getMessage();
        }
    }

    @GetMapping("/")
    public String health() {
        return "🚀 HanoiGo Backend is running!";
    }
}