package repository;

import database.DatabaseManager;
import model.Ticket;

import java.sql.*;
import java.util.*;

public class TicketRepository {

    private Ticket map(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getString("id"),
                rs.getString("event_id"),
                rs.getString("user_id"),
                rs.getString("category"),
                rs.getInt("quantity"),
                rs.getDouble("unit_price"),
                rs.getDouble("total_price"),
                rs.getString("purchase_date"),
                rs.getString("status"),
                rs.getDouble("refund_amount")
        );
    }

    private String lower(String value) {
        if (value == null) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    public List<Ticket> findAll(String eventId, String userId, String status) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();

        boolean hasEventId = eventId != null && !eventId.trim().isEmpty();
        boolean hasUserId = userId != null && !userId.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty();

        StringBuilder sql = new StringBuilder("SELECT * FROM tickets WHERE 1 = 1");

        if (hasEventId) {
            sql.append(" AND event_id = ?");
        }

        if (hasUserId) {
            sql.append(" AND user_id = ?");
        }

        if (hasStatus) {
            sql.append(" AND LOWER(status) = ?");
        }

        sql.append(" ORDER BY purchase_date DESC");

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            int index = 1;

            if (hasEventId) {
                statement.setString(index++, eventId);
            }

            if (hasUserId) {
                statement.setString(index++, userId);
            }

            if (hasStatus) {
                statement.setString(index++, lower(status));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tickets.add(map(resultSet));
                }
            }
        }

        return tickets;
    }

    public Ticket findById(String id) throws SQLException {
        String sql = "SELECT * FROM tickets WHERE id = ?";

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
        String sql = "SELECT id FROM tickets ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String lastId = resultSet.getString(1);
                try {
                    int num = Integer.parseInt(lastId.split("-")[1]);
                    return String.format("TKT-%03d", num + 1);
                } catch (Exception e) {
                    // fallthrough
                }
            }
        }
        return "TKT-001";
    }

    public Ticket create(Ticket ticket) throws SQLException {
        if (ticket.getId() == null || ticket.getId().trim().isEmpty()) {
            ticket.setId(generateId());
        }

        String sql = "INSERT INTO tickets("
                    + "id,"
                    + "event_id,"
                    + "user_id,"
                    + "category,"
                    + "quantity,"
                    + "unit_price,"
                    + "total_price,"
                    + "purchase_date,"
                    + "status,"
                    + "refund_amount"
                + ") "

                + "VALUES(?, ?, ?, ?, ?, ?, ?, datetime('now'), 'active', 0)"
                ;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, ticket.getId());
            statement.setString(2, ticket.getEventId());
            statement.setString(3, ticket.getUserId());
            statement.setString(4, lower(ticket.getCategory()));
            statement.setInt(5, ticket.getQuantity());
            statement.setDouble(6, ticket.getUnitPrice());
            statement.setDouble(7, ticket.getTotalPrice());

            statement.executeUpdate();
        }

        return findById(ticket.getId());
    }

    public Ticket refund(String id, double refundAmount) throws SQLException {
        String sql = "UPDATE tickets "
                + "SET status = 'refunded', "
                + "refund_amount = ? "
                + "WHERE id = ? "
                + "AND status <> 'refunded' "
                ;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setDouble(1, refundAmount);
            statement.setString(2, id);

            statement.executeUpdate();
        }

        return findById(id);
    }
}
