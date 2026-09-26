package co.panha.hibernate.tourmanagement.features.payment;

/**
 * វិធីទូទាត់។
 *
 * <p>បែងចែកជា ២ ក្រុមដែលមានឥរិយាបថខុសគ្នា (មើល {@code PaymentServiceImpl.pay})៖
 * ក្រុមអនឡាញបញ្ជាក់ភ្លាម ចំណែក {@code CASH} និង {@code BANK_TRANSFER} រង់ចាំ ADMIN ពិនិត្យ។
 */
public enum PaymentMethod {
    CASH,
    BANK_TRANSFER,
    ABA_PAY,
    WING,
    CREDIT_CARD,
    KHQR
}
