package com.example.jdbc_assignment.services;

import com.example.jdbc_assignment.dao.MarketPriceDAO;
import com.example.jdbc_assignment.models.MarketPrice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Note: This is a mock implementation since we don't have a real API to use
public class MarketPriceService {
    private static final String API_URL = "https://example.com/api/market-prices";
    private static final MarketPriceDAO marketPriceDAO = new MarketPriceDAO();
    private static ScheduledExecutorService scheduler;

    public static void startPriceUpdateService() {
        // Schedule updates every 6 hours
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(MarketPriceService::updateMarketPrices, 0, 6, TimeUnit.HOURS);
    }

    public static void stopPriceUpdateService() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    public static List<MarketPrice> fetchCurrentMarketPrices() {
        // First try to get from database
        List<MarketPrice> prices = marketPriceDAO.getLatestMarketPrices();

        // If empty or outdated, fetch from API
        if (prices.isEmpty()) {
            prices = updateMarketPrices();
        }

        return prices;
    }

    private static List<MarketPrice> updateMarketPrices() {
        // This would be a real API call in production
        // For now, we'll create mock data
        List<MarketPrice> mockPrices = createMockMarketData();

        // Save to database
        for (MarketPrice price : mockPrices) {
            marketPriceDAO.addMarketPrice(price);
        }

        return mockPrices;
    }

    private static List<MarketPrice> createMockMarketData() {
        List<MarketPrice> prices = new ArrayList<>();
        String currentDate = LocalDate.now().toString();

        // Common produce in Ghana with mock prices
        prices.add(new MarketPrice("Maize", new BigDecimal("2.50"), new BigDecimal("3.20"),
                new BigDecimal("2.85"), "Accra Central Market", currentDate));
        prices.add(new MarketPrice("Rice", new BigDecimal("5.10"), new BigDecimal("6.80"),
                new BigDecimal("5.95"), "Accra Central Market", currentDate));
        prices.add(new MarketPrice("Cassava", new BigDecimal("1.20"), new BigDecimal("2.30"),
                new BigDecimal("1.75"), "Accra Central Market", currentDate));
        prices.add(new MarketPrice("Yam", new BigDecimal("3.40"), new BigDecimal("4.90"),
                new BigDecimal("4.15"), "Accra Central Market", currentDate));
        prices.add(new MarketPrice("Plantain", new BigDecimal("2.80"), new BigDecimal("4.20"),
                new BigDecimal("3.50"), "Accra Central Market", currentDate));
        prices.add(new MarketPrice("Tomatoes", new BigDecimal("4.30"), new BigDecimal("6.50"),
                new BigDecimal("5.40"), "Accra Central Market", currentDate));
        prices.add(new MarketPrice("Onions", new BigDecimal("3.20"), new BigDecimal("5.10"),
                new BigDecimal("4.15"), "Accra Central Market", currentDate));
        prices.add(new MarketPrice("Peppers", new BigDecimal("3.80"), new BigDecimal("5.60"),
                new BigDecimal("4.70"), "Accra Central Market", currentDate));
        prices.add(new MarketPrice("Cocoa", new BigDecimal("10.20"), new BigDecimal("12.80"),
                new BigDecimal("11.50"), "Accra Central Market", currentDate));

        return prices;
    }
}