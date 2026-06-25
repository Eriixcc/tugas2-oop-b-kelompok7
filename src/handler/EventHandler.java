package handler;

import server.Request;
import server.Response;
import service.EventService;

public class EventHandler {

    private static final EventService eventService =
            new EventService();

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

        String id =
                req.getPathParam("id");

        res.sendSuccess(
                eventService.getEvent(id)
        );
    }

    public static void getRemainingCapacity(
            Request req,
            Response res)
            throws Exception {

        String id =
                req.getPathParam("id");

        res.sendSuccess(
                eventService.remainingCapacity(id)
        );
    }

    public static void getSalesReport(
            Request req,
            Response res)
            throws Exception {

        String id =
                req.getPathParam("id");

        res.sendSuccess(
                eventService.salesReport(id)
        );
    }
}