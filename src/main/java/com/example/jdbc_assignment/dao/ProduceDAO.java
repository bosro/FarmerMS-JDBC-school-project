package com.example.jdbc_assignment.dao;

import com.example.jdbc_assignment.models.Produce;
import com.example.jdbc_assignment.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduceDAO {

    public boolean addProduce(Produce produce) {
        String sql = "INSERT INTO produce (user_id, name, category, description, unit_of_measure) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, produce.getUserId());
            pstmt.setString(2, produce.getName());
            pstmt.setString(3, produce.getCategory());
            pstmt.setString(4, produce.getDescription());
            pstmt.setString(5, produce.getUnitOfMeasure());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    produce.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduce(Produce produce) {
        String sql = "UPDATE produce SET name = ?, category = ?, description = ?, unit_of_measure = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, produce.getName());
            pstmt.setString(2, produce.getCategory());
            pstmt.setString(3, produce.getDescription());
            pstmt.setString(4, produce.getUnitOfMeasure());
            pstmt.setInt(5, produce.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduce(int produceId) {
        String sql = "DELETE FROM produce WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, produceId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Produce getProduceById(int produceId) {
        String sql = "SELECT * FROM produce WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, produceId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractProduceFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Produce> getAllProduceByUser(int userId) {
        List<Produce> produceList = new ArrayList<>();
        String sql = "SELECT * FROM produce WHERE user_id = ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Produce produce = extractProduceFromResultSet(rs);
                produceList.add(produce);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produceList;
    }

    private Produce extractProduceFromResultSet(ResultSet rs) throws SQLException {
        Produce produce = new Produce();
        produce.setId(rs.getInt("id"));
        produce.setUserId(rs.getInt("user_id"));
        produce.setName(rs.getString("name"));
        produce.setCategory(rs.getString("category"));
        produce.setDescription(rs.getString("description"));
        produce.setUnitOfMeasure(rs.getString("unit_of_measure"));
        produce.setCreatedAt(rs.getString("created_at"));
        return produce;
    }
}