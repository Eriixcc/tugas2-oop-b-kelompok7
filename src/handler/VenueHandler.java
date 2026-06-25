package handler;

import model.Venue;
import server.Request;
import server.Response;
import service.VenueService;

import java.util.Map;

public class VenueHandler {

    private static final VenueService venueService =
            new VenueService();

    public static void getVenues(Request req, Response res)
            throws Exception {

        res.sendSuccess(
                venueService.getVenues()
        );
    }

    public static void getVenue(Request req, Response res)
            throws Exception {

        String id =
                req.getPathParam("id");

        res.sendSuccess(
                venueService.getVenue(id)
        );
    }

    public static void createVenue(Request req, Response res)
            throws Exception {

        Map<String, Object> body =
                req.getJSON();

        Venue venue = new Venue();

        venue.setName((String) body.get("name"));
        venue.setAddress((String) body.get("address"));

        Object capacityObj =
                body.get("maxCapacity");

        venue.setMaxCapacity(
                ((Number) capacityObj).intValue()
        );

        res.sendCreated(
                venueService.createVenue(venue)
        );
    }

    public static void updateVenue(Request req, Response res)
            throws Exception {

        String id =
                req.getPathParam("id");

        Map<String, Object> body =
                req.getJSON();

        Venue venue = new Venue();

        venue.setName((String) body.get("name"));
        venue.setAddress((String) body.get("address"));

        Object capacityObj =
                body.get("maxCapacity");

        venue.setMaxCapacity(
                ((Number) capacityObj).intValue()
        );

        res.sendSuccess(
                venueService.updateVenue(id, venue)
        );
    }
}