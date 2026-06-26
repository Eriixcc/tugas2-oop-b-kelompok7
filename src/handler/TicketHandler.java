package handler;

import model.Ticket;
import server.Request;
import server.Response;
import service.TicketService;
import java.util.*;

public class TicketHandler {

    private static final TicketService ticketService =
            new TicketService();

    private static Map<String, Object> formatTicket(Ticket ticket) throws Exception {
        service.EventService eventService = new service.EventService();
        service.UserService userService = new service.UserService();
        
        model.Event event = eventService.getEvent(ticket.getEventId());
        model.User user = userService.getUser(ticket.getUserId());
        
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", ticket.getId());
        data.put("event", event.getName());
        data.put("eventType", event.getType());
        
        Map<String, Object> buyer = new LinkedHashMap<>();
        buyer.put("id", user.getId());
        buyer.put("name", user.getName());
        data.put("buyer", buyer);
        
        data.put("category", ticket.getCategory());
        data.put("quantity", ticket.getQuantity());
        data.put("unitPrice", ticket.getUnitPrice());
        data.put("totalPrice", ticket.getTotalPrice());
        data.put("purchaseDate", ticket.getPurchaseDate());
        data.put("status", ticket.getStatus());
        
        return data;
    }

    public static void getTickets(Request req, Response res)
            throws Exception {

        List<Ticket> tickets = ticketService.getTickets(
                req.getQueryParam("eventId"),
                req.getQueryParam("userId"),
                req.getQueryParam("status")
        );

        List<Map<String, Object>> formatted = new ArrayList<>();
        for (Ticket ticket : tickets) {
            formatted.add(formatTicket(ticket));
        }

        res.sendSuccess(formatted);
    }

    public static void getTicket(Request req, Response res)
            throws Exception {

        String id = req.getPathParam("id");
        Ticket ticket = ticketService.getTicket(id);
        res.sendSuccess(formatTicket(ticket));
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

        res.sendCreated(formatTicket(ticket));
    }

    public static void refundTicket(
            Request req,
            Response res)
            throws Exception {

        String id = req.getPathParam("id");
        Ticket ticket = ticketService.refund(id);

        service.EventService eventService = new service.EventService();
        model.Event event = eventService.getEvent(ticket.getEventId());

        double refundPercentage = 0.0;
        if (event instanceof model.Refundable) {
            long daysBeforeEvent = java.time.temporal.ChronoUnit.DAYS.between(
                    java.time.LocalDate.now(),
                    java.time.LocalDate.parse(event.getDate()));
            refundPercentage = ((model.Refundable) event).calculateRefund((int) daysBeforeEvent);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", ticket.getId());
        data.put("event", event.getName());
        data.put("totalPaid", ticket.getTotalPrice());
        data.put("refundPercentage", (int) (refundPercentage * 100));
        data.put("refundAmount", ticket.getRefundAmount());
        data.put("status", ticket.getStatus());

        res.sendSuccess(data);
    }
}