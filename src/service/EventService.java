package service;

import exception.EventNotFoundException;
import model.Event;
import repository.EventRepository;
import repository.UserRepository;
import repository.VenueRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class EventService {

    private final EventRepository eventRepository = new EventRepository();
    private final VenueRepository venueRepository = new VenueRepository();
    private final UserRepository userRepository = new UserRepository();

    public List<Event> getEvents(String type, String dateFrom)
            throws SQLException {

        return eventRepository.findAll(type, dateFrom);
    }

    public Event getEvent(String id) throws SQLException {

        Event event = eventRepository.findById(id);

        if (event == null) {
            throw new EventNotFoundException("Event tidak ditemukan");
        }

        return event;
    }

    public Event createEvent(
            Event event,
            Map<String, Integer> capacities)
            throws SQLException {

        if (venueRepository.findById(event.getVenueId()) == null) {
            throw new IllegalArgumentException("Venue tidak ditemukan");
        }

        if (userRepository.findById(event.getOrganizerId()) == null) {
            throw new IllegalArgumentException("Organizer tidak ditemukan");
        }

        if (eventRepository.existsSameVenueDate(
                event.getVenueId(),
                event.getDate(),
                null)) {

            throw new IllegalArgumentException(
                    "Venue sudah digunakan pada tanggal tersebut");
        }

        return eventRepository.create(event, capacities);
    }

    public Event updateEvent(
            String id,
            Event event)
            throws SQLException {

        if (eventRepository.findById(id) == null) {
            throw new EventNotFoundException("Event tidak ditemukan");
        }

        if (venueRepository.findById(event.getVenueId()) == null) {
            throw new IllegalArgumentException("Venue tidak ditemukan");
        }

        if (userRepository.findById(event.getOrganizerId()) == null) {
            throw new IllegalArgumentException("Organizer tidak ditemukan");
        }

        return eventRepository.update(id, event);
    }

    public Map<String, Integer> remainingCapacity(String eventId)
            throws SQLException {

        return eventRepository.remainingCapacity(eventId);
    }

    public Map<String, Object> salesReport(String eventId)
            throws SQLException {

        if (eventRepository.findById(eventId) == null) {
            throw new EventNotFoundException("Event tidak ditemukan");
        }

        return eventRepository.salesReport(eventId);
    }
}