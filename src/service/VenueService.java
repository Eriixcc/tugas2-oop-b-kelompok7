package service;

import model.Venue;
import repository.VenueRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class VenueService {

    private final VenueRepository venueRepository = new VenueRepository();

    public List<Venue> getVenues() throws SQLException {
        return venueRepository.findAll();
    }

    public Venue getVenue(String id) throws SQLException {

        Venue venue = venueRepository.findById(id);

        if (venue == null) {
            throw new IllegalArgumentException("Venue tidak ditemukan");
        }

        return venue;
    }

    public Venue createVenue(Venue venue) throws SQLException {

        if (venue.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("Kapasitas harus lebih dari 0");
        }

        return venueRepository.create(venue);
    }

    public Venue updateVenue(String id, Venue venue) throws SQLException {

        if (venueRepository.findById(id) == null) {
            throw new IllegalArgumentException("Venue tidak ditemukan");
        }

        if (venue.getMaxCapacity() <= 0) {
            throw new IllegalArgumentException("Kapasitas harus lebih dari 0");
        }

        return venueRepository.update(id, venue);
    }

    public List<Map<String, Object>> eventsAtVenue(String venueId)
            throws SQLException {

        return venueRepository.eventsAtVenue(venueId);
    }
}