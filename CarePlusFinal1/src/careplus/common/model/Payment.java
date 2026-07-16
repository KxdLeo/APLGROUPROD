package careplus.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String patientId;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private BigDecimal outstandingBalance;

    public Payment() {
    }

    public Payment(int id, String patientId, BigDecimal amountPaid, LocalDate paymentDate,
            BigDecimal outstandingBalance) {
        this.id = id;
        this.patientId = patientId;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.outstandingBalance = outstandingBalance;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public BigDecimal getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal outstandingBalance) { this.outstandingBalance = outstandingBalance; }
}
