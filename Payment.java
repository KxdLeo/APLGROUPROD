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
    private String description;
    private String status;

    public Payment() {
    }

    public Payment(int id, String patientId, BigDecimal amountPaid, LocalDate paymentDate,
            BigDecimal outstandingBalance) {
        this(id, patientId, amountPaid, paymentDate, outstandingBalance, "General Payment", "Unpaid");
    }

    public Payment(int id, String patientId, BigDecimal amountPaid, LocalDate paymentDate,
            BigDecimal outstandingBalance, String description, String status) {
        this.id = id;
        this.patientId = patientId;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.outstandingBalance = outstandingBalance;
        this.description = description;
        this.status = status;
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
