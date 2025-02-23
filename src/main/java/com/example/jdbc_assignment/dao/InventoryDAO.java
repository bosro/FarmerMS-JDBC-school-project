package com.example.jdbc_assignment.dao;

import com.example.jdbc_assignment.models.Inventory;
import com.example.jdbc_assignment.utils.DatabaseConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public boolean addInventory(Inventory inventory) {
        String sql = "INSERT INTO inventory (produce_id, quantity, unit_price) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, inventory.getProduceId());
            pstmt.setBigDecimal(2, inventory.getQuantity());
            pstmt.setBigDecimal(3, inventory.getUnitPrice());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    inventory.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateInventory(Inventory inventory) {
        String sql = "UPDATE inventory SET quantity = ?, unit_price = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, inventory.getQuantity());
            pstmt.setBigDecimal(2, inventory.getUnitPrice());
            pstmt.setInt(3, inventory.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInventory(int inventoryId) {
        String sql = "DELETE FROM inventory WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, inventoryId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Inventory getInventoryByProduceId(int produceId) {
        String sql = "SELECT i.*, p.name as produce_name, p.unit_of_measure " +
                "FROM inventory i " +
                "JOIN produce p ON i.produce_id = p.id " +
                "WHERE i.produce_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, produceId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractInventoryFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Inventory> getAllInventoryByUser(int userId) {
        List<Inventory> inventoryList = new ArrayList<>();
        String sql = "SELECT i.*, p.name as produce_name, p.unit_of_measure " +
                "FROM inventory i " +
                "JOIN produce p ON i.produce_id = p.id " +
                "WHERE p.user_id = ? " +
                "ORDER BY p.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Inventory inventory = extractInventoryFromResultSet(rs);
                inventoryList.add(inventory);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inventoryList;
    }

    public boolean adjustInventoryAfterSale(int produceId, BigDecimal soldQuantity) {
        String sql = "UPDATE inventory SET quantity = quantity - ? WHERE produce_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, soldQuantity);
            pstmt.setInt(2, produceId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Inventory extractInventoryFromResultSet(ResultSet rs) throws SQLException {
        Inventory inventory = new Inventory();
        inventory.setId(rs.getInt("id"));
        inventory.setProduceId(rs.getInt("produce_id"));
        inventory.setQuantity(rs.getBigDecimal("quantity"));
        inventory.setUnitPrice(rs.getBigDecimal("unit_price"));
        inventory.setLastUpdated(rs.getString("last_updated"));
        inventory.setProduceName(rs.getString("produce_name"));
        inventory.setUnitOfMeasure(rs.getString("unit_of_measure"));
        return inventory;
    }
}