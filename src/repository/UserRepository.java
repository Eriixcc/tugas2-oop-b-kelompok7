package repository;

import database.DatabaseManager;
import model.User;

import java.sql.*;
import java.util.*;

public class UserRepository {

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("role")
        );
    }

    private String normalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "buyer";
        }

        return role.trim().toLowerCase();
    }

    public List<User> findAll(String role) throws SQLException {
        List<User> users = new ArrayList<>();

        boolean hasRole = role != null && !role.trim().isEmpty();

        String sql = hasRole
                ? "SELECT * FROM users WHERE LOWER(role) = ? ORDER BY name ASC"
                : "SELECT * FROM users ORDER BY name ASC";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            if (hasRole) {
                statement.setString(1, normalizeRole(role));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(map(resultSet));
                }
            }
        }

        return users;
    }

    public User findById(String id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return map(resultSet);
                }
            }
        }

        return null;
    }

    private String generateId() throws SQLException {
        String sql = "SELECT id FROM users ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String lastId = resultSet.getString(1);
                try {
                    int num = Integer.parseInt(lastId.split("-")[1]);
                    return String.format("USR-%03d", num + 1);
                } catch (Exception e) {
                    // fallthrough
                }
            }
        }
        return "USR-001";
    }

    public User create(User user) throws SQLException {
        if (user.getId() == null || user.getId().trim().isEmpty()) {
            user.setId(generateId());
        }

        user.setRole(normalizeRole(user.getRole()));

        String sql = "INSERT INTO users(id, name, email, phone, role) VALUES(?, ?, ?, ?, ?)";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.getId());
            statement.setString(2, user.getName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getRole());

            statement.executeUpdate();
        }

        return findById(user.getId());
    }

    public User update(String id, User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, role = ? WHERE id = ?";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPhone());
            statement.setString(4, normalizeRole(user.getRole()));
            statement.setString(5, id);

            statement.executeUpdate();
        }

        return findById(id);
    }

    public Map<String, Object> buyerSummary(String userId) throws SQLException {
        Map<String, Object> summary = new LinkedHashMap<>();

        String sql = """
                SELECT 
                    COALESCE(SUM(quantity), 0) AS total_quantity,
                    COALESCE(SUM(total_price - COALESCE(refund_amount, 0)), 0) AS total_spending
                FROM tickets
                WHERE user_id = ?
                """;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    summary.put("totalTicketsPurchased", resultSet.getInt("total_quantity"));
                    summary.put("totalSpending", resultSet.getDouble("total_spending"));
                }
            }
        }

        return summary;
    }

    public Map<String, Object> organizerSummary(String userId) throws SQLException {
        Map<String, Object> summary = new LinkedHashMap<>();

        String sql = """
                SELECT 
                    COUNT(DISTINCT e.id) AS total_events,
                    COALESCE(SUM(t.total_price - COALESCE(t.refund_amount, 0)), 0) AS total_revenue
                FROM events e
                LEFT JOIN tickets t ON e.id = t.event_id
                WHERE e.organizer_id = ?
                """;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    summary.put("totalEventsCreated", resultSet.getInt("total_events"));
                    summary.put("totalRevenue", resultSet.getDouble("total_revenue"));
                }
            }
        }

        return summary;
    }
}