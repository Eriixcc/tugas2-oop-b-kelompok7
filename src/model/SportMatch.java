package model;

public class SportMatch extends Event{
    
    public SportMatch() {
        setType("sportmatch");
    }

    public SportMatch(String id, String name, String venueId, String organizerId, String date, double basePrice) {
        super(id, "sportmatch", name, venueId, organizerId, date, basePrice);
    }

    @Override
    public double calculateTicketPrice(String category) {
        if ("tribune".equalsIgnoreCase(category)) {
            return getBasePrice();
        }

        if ("vip".equalsIgnoreCase(category)) {
            return getBasePrice() * 2.5;
        }

        if ("vvip".equalsIgnoreCase(category)){
            return getBasePrice() * 5.0;
        }

        throw new IllegalArgumentException("Kategori sport match tidak valid: " + category);
    }

    @Override
    public String[] getAvailableCategories() {
        return new String[]{"tribune", "vip", "vvip"};
    }

}
