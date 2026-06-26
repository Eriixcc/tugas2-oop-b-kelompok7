package handler;

import model.*;
import server.Request;
import server.Response;
import service.EventService;
import java.util.*;

public class EventHandler {

    private static final EventService eventService =
            new EventService();

    private static Map<String, Object> formatEvent(Event event) throws Exception {
        service.VenueService venueService = new service.VenueService();
        service.UserService userService = new service.UserService();
        
        Venue venue = venueService.getVenue(event.getVenueId());
        User organizer = userService.getUser(event.getOrganizerId());
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", event.getId());
        data.put("type", event.getType());
        data.put("name", event.getName());
        
        Map<String, Object> venueData = new LinkedHashMap<>();
        venueData.put("id", venue.getId());
        venueData.put("name", venue.getName());
        data.put("venue", venueData);
        
        Map<String, Object> organizerData = new LinkedHashMap<>();
        organizerData.put("id", organizer.getId());
        organizerData.put("name", organizer.getName());
        data.put("organizer", organizerData);
        
        data.put("date", event.getDate());
        data.put("basePrice", event.getBasePrice());
        
        Map<String, Object> priceList = new LinkedHashMap<>();
        for (String category : event.getAvailableCategories()) {
            priceList.put(category, event.calculateTicketPrice(category));
        }
        data.put("priceList", priceList);
        
        data.put("remainingCapacity", eventService.remainingCapacity(event.getId()));
        
        if (event instanceof Refundable) {
            data.put("refundable", true);
            data.put("refundPolicy", ((Refundable) event).getRefundPolicy());
        } else {
            data.put("refundable", false);
        }
        
        return data;
    }

    public static void getEvents(Request req, Response res)
            throws Exception {

        res.sendSuccess(
                eventService.getEvents(
                        req.getQueryParam("type"),
                        req.getQueryParam("dateFrom")
                )
        );
    }

    public static void getEvent(Request req, Response res)
            throws Exception {

        String id = req.getPathParam("id");
        Event event = eventService.getEvent(id);
        res.sendSuccess(formatEvent(event));
    }

    public static void createEvent(Request req, Response res)
            throws Exception {
        Map<String, Object> body = req.getJSON();
        if (body == null) {
            res.sendError(400, "Request body wajib berupa JSON");
            return;
        }

        String type = (String) body.get("type");
        if (type == null || type.trim().isEmpty()) {
            res.sendError(400, "Field 'type' wajib diisi");
            return;
        }

        String name = (String) body.get("name");
        String venueId = (String) body.get("venueId");
        String organizerId = (String) body.get("organizerId");
        String date = (String) body.get("date");
        Number basePriceNum = (Number) body.get("basePrice");
        
        if (name == null || name.trim().isEmpty()) {
            res.sendError(400, "Field 'name' wajib diisi");
            return;
        }
        if (venueId == null || venueId.trim().isEmpty()) {
            res.sendError(400, "Field 'venueId' wajib diisi");
            return;
        }
        if (organizerId == null || organizerId.trim().isEmpty()) {
            res.sendError(400, "Field 'organizerId' wajib diisi");
            return;
        }
        if (date == null || date.trim().isEmpty()) {
            res.sendError(400, "Field 'date' wajib diisi");
            return;
        }
        if (basePriceNum == null) {
            res.sendError(400, "Field 'basePrice' wajib diisi");
            return;
        }

        double basePrice = basePriceNum.doubleValue();

        Event event;
        String normalizedType = type.trim().toLowerCase();
        if (normalizedType.equals("concert")) {
            event = new Concert(null, name, venueId, organizerId, date, basePrice);
        } else if (normalizedType.equals("seminar")) {
            event = new Seminar(null, name, venueId, organizerId, date, basePrice);
        } else if (normalizedType.equals("sport_match") || normalizedType.equals("sportmatch") || normalizedType.equals("sport")) {
            event = new SportMatch(null, name, venueId, organizerId, date, basePrice);
        } else {
            res.sendError(400, "Tipe event tidak valid: " + type);
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> capRaw = (Map<String, Object>) body.get("capacity");
        Map<String, Integer> capacities = new HashMap<>();
        if (capRaw != null) {
            for (Map.Entry<String, Object> entry : capRaw.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    capacities.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                }
            }
        }

        Event createdEvent = eventService.createEvent(event, capacities);
        res.sendCreated(formatEvent(createdEvent));
    }

    public static void updateEvent(Request req, Response res)
            throws Exception {
        String id = req.getPathParam("id");
        Map<String, Object> body = req.getJSON();
        if (body == null) {
            res.sendError(400, "Request body wajib berupa JSON");
            return;
        }

        Event existing = eventService.getEvent(id);
        String name = (String) body.get("name");
        String date = (String) body.get("date");
        Number basePriceNum = (Number) body.get("basePrice");

        if (name != null) {
            existing.setName(name);
        }
        if (date != null) {
            existing.setDate(date);
        }
        if (basePriceNum != null) {
            existing.setBasePrice(basePriceNum.doubleValue());
        }

        Event updated = eventService.updateEvent(id, existing);
        res.sendSuccess(formatEvent(updated));
    }

    public static void priceSummary(Request req, Response res)
            throws Exception {
        List<Event> events = eventService.getEvents(null, null);
        List<Map<String, Object>> summary = new ArrayList<>();
        
        for (Event event : events) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", event.getId());
            item.put("name", event.getName());
            item.put("type", event.getType());
            
            Map<String, Object> prices = new LinkedHashMap<>();
            for (String category : event.getAvailableCategories()) {
                prices.put(category, event.calculateTicketPrice(category));
            }
            item.put("prices", prices);
            summary.add(item);
        }
        
        res.sendSuccess(summary);
    }

    public static void getRemainingCapacity(
            Request req,
            Response res)
            throws Exception {

        String id = req.getPathParam("id");
        res.sendSuccess(
                eventService.remainingCapacity(id)
        );
    }

    public static void getSalesReport(
            Request req,
            Response res)
            throws Exception {

        String eventId = req.getPathParam("id");
        if (eventId == null || eventId.trim().isEmpty()) {
            eventId = req.getQueryParam("eventId");
        }
        
        if (eventId == null || eventId.trim().isEmpty()) {
            res.sendError(400, "Event ID (path atau query param) wajib diisi");
            return;
        }

        res.sendSuccess(
                eventService.salesReport(eventId)
        );
    }
}