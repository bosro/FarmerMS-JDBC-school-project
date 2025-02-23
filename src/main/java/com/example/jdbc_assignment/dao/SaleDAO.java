package com.example.jdbc_assignment.dao;

import com.example.jdbc_assignment.models.Sale;
import com.example.jdbc_assignment.utils.DatabaseConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public boolean addSale(Sale sale) {
        String sql = "INSERT INTO sales (produce_id, user_id, quantity, unit_price, total_amount, buyer_name, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, sale.getProduceId());
            pstmt.setInt(2, sale.getUserId());
            pstmt.setBigDecimal(3, sale.getQuantity());
            pstmt.setBigDecimal(4, sale.getUnitPrice());
            pstmt.setBigDecimal(5, sale.getTotalAmount());
            pstmt.setString(6, sale.getBuyerName());
            pstmt.setString(7, sale.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    sale.setId(generatedKeys.getInt(1));
                }

                // Update inventory after sale
                InventoryDAO inventoryDAO = new InventoryDAO();
                inventoryDAO.adjustInventoryAfterSale(sale.getProduceId(), sale.getQuantity());

                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSale(Sale sale) {
        String sql = "UPDATE sales SET produce_id = ?, quantity = ?, unit_price = ?, " +
                "total_amount = ?, buyer_name = ?, notes = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sale.getProduceId());
            pstmt.setBigDecimal(2, sale.getQuantity());
            pstmt.setBigDecimal(3, sale.getUnitPrice());
            pstmt.setBigDecimal(4, sale.getTotalAmount());
            pstmt.setString(5, sale.getBuyerName());
            pstmt.setString(6, sale.getNotes());
            pstmt.setInt(7, sale.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSale(int saleId) {
        String sql = "DELETE FROM sales WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Sale getSaleById(int saleId) {
        String sql = "SELECT s.*, p.name as produce_name, p.unit_of_measure " +
                "FROM sales s " +
                "JOIN produce p ON s.produce_id = p.id " +
                "WHERE s.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractSaleFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Sale> getAllSalesByUser(int userId) {
        List<Sale> saleList = new ArrayList<>();
        String sql = "SELECT s.*, p.name as produce_name, p.unit_of_measure " +
                "FROM sales s " +
                "JOIN produce p ON s.produce_id = p.id " +
                "WHERE s.user_id = ? " +
                "ORDER BY s.sale_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Sale sale = extractSaleFromResultSet(rs);
                saleList.add(sale);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return saleList;
    }

    public BigDecimal getTotalSalesAmountByUser(int userId) {
        String sql = "SELECT SUM(total_amount) as total FROM sales WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }

    private Sale extractSaleFromResultSet(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        sale.setProduceId(rs.getInt("produce_id"));
        sale.setUserId(rs.getInt("user_id"));
        sale.setQuantity(rs.getBigDecimal("quantity"));
        sale.setUnitPrice(rs.getBigDecimal("unit_price"));
        sale.setTotalAmount(rs.getBigDecimal("total_amount"));
        sale.setSaleDate(rs.getString("sale_date"));
        sale.setBuyerName(rs.getString("buyer_name"));
        sale.setNotes(rs.getString("notes"));
        sale.setProduceName(rs.getString("produce_name"));
        sale.setUnitOfMeasure(rs.getString("unit_of_measure"));
        return sale;
    }
}