package neu.csye6225.spring2020.cloud.model;

public enum PaymentStatusType {

    paid ("paid"),
    due ("due"),
    past_due ("past_due"),
    no_payment_required ("no_payment_required");

    private final String paymentStatusType;

    PaymentStatusType(String paymentStatusType) {
        this.paymentStatusType = paymentStatusType;
    }

}
