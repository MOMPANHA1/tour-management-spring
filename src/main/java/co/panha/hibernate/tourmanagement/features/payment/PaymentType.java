package co.panha.hibernate.tourmanagement.features.payment;

/**
 * ប្រភេទចលនាទឹកប្រាក់។
 *
 * <p>{@code REFUND} ជាទិសផ្ទុយ — វា<b>ដក</b>ចេញពី {@code booking.paidAmount}
 * ចំណែក ៣ ប្រភេទទៀតបូកចូល។ ដូច្នេះរាល់ការបូកសរុបត្រូវញែក {@code REFUND} ចេញជានិច្ច។
 */
public enum PaymentType {
    DEPOSIT,
    FULL_PAYMENT,
    BALANCE,
    REFUND
}
