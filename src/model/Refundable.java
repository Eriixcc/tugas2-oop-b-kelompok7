package model;

public interface Refundable {
    double calculateRefund(int daysBeforeEvent);
    boolean isRefundable();
    String getRefundPolicy();
}

