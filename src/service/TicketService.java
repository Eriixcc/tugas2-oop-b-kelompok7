package service;

import exception.EventNotFoundException;
import exception.RefundNotAllowedException;
import exception.TicketSoldOutException;
import model.Event;
import model.Refundable;
import model.Ticket;
import repository.EventRepository;
import repository.TicketRepository;
import repository.UserRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TicketService {

    private final TicketRepository ticketRepository =
            new TicketRepository();

    private final EventRepository eventRepository =
            new EventRepository();

    private final UserRepository userRepository =
            new UserRepository();

    public List<Ticket> getTickets(
            String eventId,
            String userId,
            String status)
            throws SQLException {

        return ticketRepository.findAll(
                eventId,
                userId,
                status);
    }

    public Ticket getTicket(String id)
            throws SQLException {

        Ticket ticket = ticketRepository.findById(id);

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Ticket tidak ditemukan");
        }

        return ticket;
    }

    public Ticket purchase(
            String eventId,
            String userId,
            String category,
            int quantity)
            throws SQLException {

        Event event = eventRepository.findById(eventId);

        if (event == null) {
            throw new EventNotFoundException(
                    "Event tidak ditemukan");
        }

        if (userRepository.findById(userId) == null) {
            throw new IllegalArgumentException(
                    "User tidak ditemukan");
        }

        if (!eventRepository.hasEnoughCapacity(
                eventId,
                category,
                quantity)) {

            throw new TicketSoldOutException(
                    "Kapasitas tiket tidak mencukupi");
        }

        double unitPrice =
                event.calculateTicketPrice(category);

        double totalPrice =
                unitPrice * quantity;

        Ticket ticket = new Ticket();

        ticket.setEventId(eventId);
        ticket.setUserId(userId);
        ticket.setCategory(category);
        ticket.setQuantity(quantity);
        ticket.setUnitPrice(unitPrice);
        ticket.setTotalPrice(totalPrice);

        eventRepository.addFilled(
                eventId,
                category,
                quantity);

        return ticketRepository.create(ticket);
    }

    public Ticket refund(String ticketId)
            throws SQLException {

        Ticket ticket = ticketRepository.findById(ticketId);

        if (ticket == null) {
            throw new IllegalArgumentException(
                    "Ticket tidak ditemukan");
        }

        Event event =
                eventRepository.findById(
                        ticket.getEventId());

        if (event == null) {
            throw new EventNotFoundException(
                    "Event tidak ditemukan");
        }

        if (!(event instanceof Refundable)) {
            throw new RefundNotAllowedException(
                    "Event tidak mendukung refund");
        }

        Refundable refundable =
                (Refundable) event;

        long daysBeforeEvent =
                ChronoUnit.DAYS.between(
                        LocalDate.now(),
                        LocalDate.parse(event.getDate()));

        double refundPercentage =
                refundable.calculateRefund(
                        (int) daysBeforeEvent);

        if (refundPercentage <= 0) {
            throw new RefundNotAllowedException(
                    "Refund tidak diperbolehkan");
        }

        double refundAmount =
                ticket.getTotalPrice() * refundPercentage;

        eventRepository.subtractFilled(
                ticket.getEventId(),
                ticket.getCategory(),
                ticket.getQuantity());

        return ticketRepository.refund(
                ticketId,
                refundAmount);
    }
}