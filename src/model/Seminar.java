package model;

public class Seminar extends Event implements Refundable {

    public Seminar() {
        setType("seminar");
    }

    public Seminar(String id, String name, String venueId, String organizerId, String date, double basePrice) {
        super(id, "seminar", name, venueId, organizerId, date, basePrice);
    }

    @Override
    public double calculateTicketPrice(String category) {
        if ("general".equalsIgnoreCase(category)) {
            return getBasePrice();
        }

        throw new IllegalArgumentException("Kategori seminar tidak valid: " + category);
    }

    @Override
    public String[] getAvailableCategories() {
        return new String[]{"general"};
    }

    @Override
    public double calculateRefund(int daysBeforeEvent) {
        if (daysBeforeEvent > 1) {
            return 1.0;
        }

        return 0.0;
    }

    @Override
    public boolean isRefundable() {
        return true;
    }

    @Override
    public String getRefundPolicy() {
        return "100% if >1 day, 0% if <=1 day";
    }
}