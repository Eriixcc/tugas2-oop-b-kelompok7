package handler;

import model.Ticket;
import server.Request;
import server.Response;
import service.TicketService;

import java.util.Map;

public class TicketHandler {

    private static final TicketService ticketService =
            new TicketService();

    public static void getTickets(Request req, Response res)
            throws Exception {

        res.sendSuccess(
                ticketService.getTickets(
                        req.getQueryParam("eventId"),
                        req.getQueryParam("userId"),
                        req.getQueryParam("status")
                )
        );
    }

    public static void getTicket(Request req, Response res)
            throws Exception {

        String id =
                req.getPathParam("id");

        res.sendSuccess(
                ticketService.getTicket(id)
        );
    }

    public static void purchaseTicket(
            Request req,
            Response res)
            throws Exception {

        Map<String, Object> body =
                req.getJSON();

        String eventId =
                (String) body.get("eventId");

        String userId =
                (String) body.get("userId");

        String category =
                (String) body.get("category");

        int quantity =
                ((Number) body.get("quantity"))
                        .intValue();

        Ticket ticket =
                ticketService.purchase(
                        eventId,
                        userId,
                        category,
                        quantity
                );

        res.sendCreated(ticket);
    }

    public static void refundTicket(
            Request req,
            Response res)
            throws Exception {

        String id =
                req.getPathParam("id");

        res.sendSuccess(
                ticketService.refund(id)
        );
    }
}