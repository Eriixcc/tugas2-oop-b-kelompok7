package model;

public class Concert extends Event implements Refundable {

    public Concert() {
        setType("concert");
    }

    public Concert(String id, String name, String venueId, String organizerId, String date, double basePrice) {
        super(id, "concert", name, venueId, organizerId, date, basePrice);
    }

    @Override
    public double calculateTicketPrice(String category) {
        if ("vip".equalsIgnoreCase(category)) {
            return getBasePrice() * 3.0;
        }

        if ("regular".equalsIgnoreCase(category)) {
            return getBasePrice();
        }

        if ("festival".equalsIgnoreCase(category)) {
            return getBasePrice() * 0.7;
        }

        throw new IllegalArgumentException("Kategori concert tidak valid: " + category);
    }

    @Override
    public String[] getAvailableCategories() {
        return new String[]{"vip", "regular", "festival"};
    }

    @Override
    public double calculateRefund(int daysBeforeEvent) {
        if (daysBeforeEvent > 14) {
            return 1.0;
        }

        if (daysBeforeEvent >= 7) {
            return 0.5;
        }

        return 0.0;
    }

    @Override
    public boolean isRefundable() {
        return true;
    }

    @Override
    public String getRefundPolicy() {
        return "100% if >14 days, 50% if 7-14 days, 0% if <7 days";
    }
}