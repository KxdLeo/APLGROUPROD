package careplus.common.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Appointment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String patientId;
    private String doctorId;
    private LocalDateTime appointmentDate;
    private String status;
    private String appointmentType;

    public Appointment() {
    }

    public Appointment(int id, String patientId, String doctorId, LocalDateTime appointmentDate, String status) {
        this(id, patientId, doctorId, appointmentDate, status, "General Check Up");
    }

    public Appointment(int id, String patientId, String doctorId, LocalDateTime appointmentDate,
            String status, String appointmentType) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.appointmentType = appointmentType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }
}
