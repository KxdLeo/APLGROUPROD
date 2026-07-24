package careplus.common.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Complaint implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String patientId;
    private String category;
    private String description;
    private LocalDate dateSubmitted;
    private String status;
    private String assignedEmployeeId;
    private String response;
    private LocalDate responseDate;
    private String respondedBy;

    public Complaint() {
    }

    public Complaint(int id, String patientId, String category, String description, LocalDate dateSubmitted) {
        this.id = id;
        this.patientId = patientId;
        this.category = category;
        this.description = description;
        this.dateSubmitted = dateSubmitted;
        this.status = "Unresolved";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDateSubmitted() { return dateSubmitted; }
    public void setDateSubmitted(LocalDate dateSubmitted) { this.dateSubmitted = dateSubmitted; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedEmployeeId() { return assignedEmployeeId; }
    public void setAssignedEmployeeId(String assignedEmployeeId) { this.assignedEmployeeId = assignedEmployeeId; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public LocalDate getResponseDate() { return responseDate; }
    public void setResponseDate(LocalDate responseDate) { this.responseDate = responseDate; }
    public String getRespondedBy() { return respondedBy; }
    public void setRespondedBy(String respondedBy) { this.respondedBy = respondedBy; }
}
