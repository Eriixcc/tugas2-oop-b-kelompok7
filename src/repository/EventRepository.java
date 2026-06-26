package repository;

import database.DatabaseManager;
import model.Concert;
import model.Event;
import model.Seminar;
import model.SportMatch;

import java.sql.*;
import java.util.*;

public class EventRepository {

    private String normalizeType(String type) {
        if (type == null) {
            return "";
        }

        String normalized = type.trim().toLowerCase();

        if (normalized.equals("sport_match") || normalized.equals("sport")) {
            return "sportmatch";
        }

        return normalized;
    }

    private String getTypeFromEvent(Event event) {
        String type = normalizeType(event.getType());

        if (!type.isEmpty()) {
            return type;
        }

        if (event instanceof Concert) {
            return "concert";
        }

        if (event instanceof Seminar) {
            return "seminar";
        }

        if (event instanceof SportMatch) {
            return "sportmatch";
        }

        return "";
    }

    public Event map(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String type = normalizeType(rs.getString("type"));
        String name = rs.getString("name");
        String venueId = rs.getString("venue_id");
        String organizerId = rs.getString("organizer_id");
        String date = rs.getString("date");
        double basePrice = rs.getDouble("base_price");

        if ("concert".equals(type)) {
            return new Concert(id, name, venueId, organizerId, date, basePrice);
        }

        if ("seminar".equals(type)) {
            return new Seminar(id, name, venueId, organizerId, date, basePrice);
        }

        if ("sportmatch".equals(type)) {
            return new SportMatch(id, name, venueId, organizerId, date, basePrice);
        }

        throw new SQLException("Tipe event tidak valid: " + type);
    }

    public List<Event> findAll(String type, String dateFrom) throws SQLException {
        List<Event> events = new ArrayList<>();

        boolean hasType = type != null && !type.trim().isEmpty();
        boolean hasDateFrom = dateFrom != null && !dateFrom.trim().isEmpty();

        StringBuilder sql = new StringBuilder("SELECT * FROM events WHERE 1 = 1");

        if (hasType) {
            sql.append(" AND LOWER(REPLACE(type, '_', '')) = ?");
        }

        if (hasDateFrom) {
            sql.append(" AND date >= ?");
        }

        sql.append(" ORDER BY date ASC");

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            int index = 1;

            if (hasType) {
                statement.setString(index++, normalizeType(type));
            }

            if (hasDateFrom) {
                statement.setString(index++, dateFrom);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(map(resultSet));
                }
            }
        }

        return events;
    }

    public Event findById(String id) throws SQLException {
        String sql = "SELECT * FROM events WHERE id = ?";

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

    public boolean existsSameVenueDate(String venueId, String date, String exceptId) throws SQLException {
        boolean hasExceptId = exceptId != null && !exceptId.trim().isEmpty();

        String sql = hasExceptId
                ? "SELECT COUNT(*) FROM events WHERE venue_id = ? AND date = ? AND id <> ?"
                : "SELECT COUNT(*) FROM events WHERE venue_id = ? AND date = ?";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, venueId);
            statement.setString(2, date);

            if (hasExceptId) {
                statement.setString(3, exceptId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private String generateId() throws SQLException {
        String sql = "SELECT id FROM events ORDER BY id DESC LIMIT 1";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                String lastId = resultSet.getString(1);
                try {
                    int num = Integer.parseInt(lastId.split("-")[1]);
                    return String.format("EVT-%03d", num + 1);
                } catch (Exception e) {
                    // fallthrough
                }
            }
        }
        return "EVT-001";
    }

    public Event create(Event event, Map<String, Integer> capacities) throws SQLException {
        if (event.getId() == null || event.getId().trim().isEmpty()) {
            event.setId(generateId());
        }

        event.setType(getTypeFromEvent(event));

        Connection connection = null;

        try {
            connection = DatabaseManager.getConnection();
            connection.setAutoCommit(false);

            String eventSql = """
                    INSERT INTO events(id, type, name, venue_id, organizer_id, date, base_price)
                    VALUES(?, ?, ?, ?, ?, ?, ?)
                    """;

            try (PreparedStatement statement = connection.prepareStatement(eventSql)) {
                statement.setString(1, event.getId());
                statement.setString(2, event.getType());
                statement.setString(3, event.getName());
                statement.setString(4, event.getVenueId());
                statement.setString(5, event.getOrganizerId());
                statement.setString(6, event.getDate());
                statement.setDouble(7, event.getBasePrice());

                statement.executeUpdate();
            }

            if (capacities != null && !capacities.isEmpty()) {
                String capacitySql = """
                        INSERT INTO capacities(event_id, category, total, filled)
                        VALUES(?, ?, ?, 0)
                        """;

                try (PreparedStatement statement = connection.prepareStatement(capacitySql)) {
                    for (Map.Entry<String, Integer> entry : capacities.entrySet()) {
                        statement.setString(1, event.getId());
                        statement.setString(2, entry.getKey().toLowerCase());
                        statement.setInt(3, entry.getValue());
                        statement.addBatch();
                    }

                    statement.executeBatch();
                }
            }

            connection.commit();
            return findById(event.getId());

        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }

            throw e;

        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public Event update(String id, Event event) throws SQLException {
        String sql = "UPDATE events SET name = ?, date = ?, base_price = ? WHERE id = ?";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, event.getName());
            statement.setString(2, event.getDate());
            statement.setDouble(3, event.getBasePrice());
            statement.setString(4, id);

            statement.executeUpdate();
        }

        return findById(id);
    }

    public Map<String, Integer> remainingCapacity(String eventId) throws SQLException {
        Map<String, Integer> capacities = new LinkedHashMap<>();

        String sql = """
                SELECT category, total - filled AS remaining
                FROM capacities
                WHERE event_id = ?
                """;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    capacities.put(
                            resultSet.getString("category"),
                            resultSet.getInt("remaining")
                    );
                }
            }
        }

        return capacities;
    }

    public int totalCapacity(Map<String, Integer> capacities) {
        int total = 0;

        if (capacities == null) {
            return total;
        }

        for (Integer capacity : capacities.values()) {
            if (capacity != null) {
                total += capacity;
            }
        }

        return total;
    }

    public boolean hasEnoughCapacity(String eventId, String category, int quantity) throws SQLException {
        String sql = """
                SELECT total - filled AS remaining
                FROM capacities
                WHERE event_id = ? AND category = ?
                """;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, eventId);
            statement.setString(2, category.toLowerCase());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("remaining") >= quantity;
            }
        }
    }

    public void addFilled(String eventId, String category, int quantity) throws SQLException {
        String sql = """
                UPDATE capacities
                SET filled = filled + ?
                WHERE event_id = ? 
                AND category = ?
                AND filled + ? <= total
                """;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, quantity);
            statement.setString(2, eventId);
            statement.setString(3, category.toLowerCase());
            statement.setInt(4, quantity);

            statement.executeUpdate();
        }
    }

    public void subtractFilled(String eventId, String category, int quantity) throws SQLException {
        String sql = """
                UPDATE capacities
                SET filled = CASE
                    WHEN filled - ? < 0 THEN 0
                    ELSE filled - ?
                END
                WHERE event_id = ? AND category = ?
                """;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, quantity);
            statement.setInt(2, quantity);
            statement.setString(3, eventId);
            statement.setString(4, category.toLowerCase());

            statement.executeUpdate();
        }
    }

    public Map<String, Object> venueMini(String venueId) throws SQLException {
        Map<String, Object> venue = new LinkedHashMap<>();

        String sql = "SELECT id, name FROM venues WHERE id = ?";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, venueId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    venue.put("id", resultSet.getString("id"));
                    venue.put("name", resultSet.getString("name"));
                }
            }
        }

        return venue;
    }

    public Map<String, Object> userMini(String userId) throws SQLException {
        Map<String, Object> user = new LinkedHashMap<>();

        String sql = "SELECT id, name FROM users WHERE id = ?";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user.put("id", resultSet.getString("id"));
                    user.put("name", resultSet.getString("name"));
                }
            }
        }

        return user;
    }

    public Map<String, Object> salesReport(String eventId) throws SQLException {
        Map<String, Object> report = new LinkedHashMap<>();

        String sql = """
                SELECT
                    COUNT(*) AS total_transactions,
                    COALESCE(SUM(CASE WHEN status = 'active' THEN quantity ELSE 0 END), 0) AS active_tickets_sold,
                    COALESCE(SUM(CASE WHEN status = 'refunded' THEN quantity ELSE 0 END), 0) AS refunded_tickets,
                    COALESCE(SUM(total_price), 0) AS gross_revenue,
                    COALESCE(SUM(refund_amount), 0) AS refund_total,
                    COALESCE(SUM(total_price - COALESCE(refund_amount, 0)), 0) AS net_revenue
                FROM tickets
                WHERE event_id = ?
                """;

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, eventId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report.put("eventId", eventId);
                    report.put("totalTransactions", resultSet.getInt("total_transactions"));
                    report.put("ticketsSold", resultSet.getInt("active_tickets_sold"));
                    report.put("activeTicketsSold", resultSet.getInt("active_tickets_sold"));
                    report.put("refundedTickets", resultSet.getInt("refunded_tickets"));
                    report.put("grossRevenue", resultSet.getDouble("gross_revenue"));
                    report.put("refundTotal", resultSet.getDouble("refund_total"));
                    report.put("revenue", resultSet.getDouble("net_revenue"));
                    report.put("netRevenue", resultSet.getDouble("net_revenue"));
                }
            }
        }

        return report;
    }
}