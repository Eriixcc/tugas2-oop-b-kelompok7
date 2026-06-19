package repository;

import database.DatabaseManager;
import model.Venue;

import java.sql.*;
import java.util.*;

public class VenueRepository {

    private Venue map(ResultSet rs) throws SQLException {
        return new Venue(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getInt("max_capacity")
        );
    }

    public List<Venue> findAll() throws SQLException {
        List<Venue> venues = new ArrayList<>();

        String sql = "SELECT * FROM venues ORDER BY name ASC";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                venues.add(map(resultSet));
            }
        }

        return venues;
    }

    public Venue findById(String id) throws SQLException {
        String sql = "SELECT * FROM venues WHERE id = ?";

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

    public Venue create(Venue venue) throws SQLException {
        if (venue.getId() == null || venue.getId().trim().isEmpty()) {
            venue.setId(RepoUtil.generateId("VNU"));
        }

        String sql = "INSERT INTO venues(id, name, address, max_capacity) VALUES(?, ?, ?, ?)";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, venue.getId());
            statement.setString(2, venue.getName());
            statement.setString(3, venue.getAddress());
            statement.setInt(4, venue.getMaxCapacity());

            statement.executeUpdate();
        }

        return findById(venue.getId());
    }

    public Venue update(String id, Venue venue) throws SQLException {
        String sql = "UPDATE venues SET name = ?, address = ?, max_capacity = ? WHERE id = ?";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, venue.getName());
            statement.setString(2, venue.getAddress());
            statement.setInt(3, venue.getMaxCapacity());
            statement.setString(4, id);

            statement.executeUpdate();
        }

        return findById(id);
    }

    public List<Map<String, Object>> eventsAtVenue(String venueId) throws SQLException {
        List<Map<String, Object>> events = new ArrayList<>();

        String sql = "SELECT id, name, type, date FROM events WHERE venue_id = ? ORDER BY date ASC";

        try (
                Connection connection = DatabaseManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, venueId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> event = new LinkedHashMap<>();
                    event.put("id", resultSet.getString("id"));
                    event.put("name", resultSet.getString("name"));
                    event.put("type", resultSet.getString("type"));
                    event.put("date", resultSet.getString("date"));

                    events.add(event);
                }
            }
        }

        return events;
    }
}