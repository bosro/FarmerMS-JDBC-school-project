package com.example.jdbc_assignment.dao;

import com.example.jdbc_assignment.models.MarketPrice;
import com.example.jdbc_assignment.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MarketPriceDAO {

    public boolean addMarketPrice(MarketPrice marketPrice) {
        String sql = "INSERT INTO market_prices (produce_name, min_price, max_price, average_price, market_name, date_recorded) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, marketPrice.getProduceName());
            pstmt.setBigDecimal(2, marketPrice.getMinPrice());
            pstmt.setBigDecimal(3, marketPrice.getMaxPrice());
            pstmt.setBigDecimal(4, marketPrice.getAveragePrice());
            pstmt.setString(5, marketPrice.getMarketName());
            pstmt.setString(6, marketPrice.getDateRecorded());
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    marketPrice.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMarketPrice(MarketPrice marketPrice) {
        String sql = "UPDATE market_prices SET min_price = ?, max_price = ?, average_price = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, marketPrice.getMinPrice());
            pstmt.setBigDecimal(2, marketPrice.getMaxPrice());
            pstmt.setBigDecimal(3, marketPrice.getAveragePrice());
            pstmt.setInt(4, marketPrice.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<MarketPrice> getLatestMarketPrices() {
        List<MarketPrice> priceList = new ArrayList<>();
        String sql = "SELECT * FROM market_prices WHERE date_recorded = " +
                "(SELECT MAX(date_recorded) FROM market_prices) " +
                "ORDER BY produce_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MarketPrice price = extractMarketPriceFromResultSet(rs);
                priceList.add(price);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return priceList;
    }

    public MarketPrice getLatestPriceForProduce(String produceName) {
        String sql = "SELECT * FROM market_prices " +
                "WHERE produce_name = ? " +
                "ORDER BY date_recorded DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, produceName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractMarketPriceFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private MarketPrice extractMarketPriceFromResultSet(ResultSet rs) throws SQLException {
        MarketPrice price = new MarketPrice();
        price.setId(rs.getInt("id"));
        price.setProduceName(rs.getString("produce_name"));
        price.setMinPrice(rs.getBigDecimal("min_price"));
        price.setMaxPrice(rs.getBigDecimal("max_price"));
        price.setAveragePrice(rs.getBigDecimal("average_price"));
        price.setMarketName(rs.getString("market_name"));
        price.setDateRecorded(rs.getString("date_recorded"));
        price.setLastUpdated(rs.getString("last_updated"));
        return price;
    }
}