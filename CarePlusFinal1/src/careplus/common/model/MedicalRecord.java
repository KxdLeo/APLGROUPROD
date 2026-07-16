package careplus.common.model;

import java.io.Serializable;
import java.time.LocalDate;

public class MedicalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String patientId;
    private String doctorId;
    private String diagnosis;
    private String treatmentNotes;
    private String vitalSigns;
    private String nursingNotes;
    private LocalDate followUpDate;

    public MedicalRecord() {
    }

    public MedicalRecord(int id, String patientId, String doctorId, String diagnosis,
            String treatmentNotes, String vitalSigns, String nursingNotes, LocalDate followUpDate) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.diagnosis = diagnosis;
        this.treatmentNotes = treatmentNotes;
        this.vitalSigns = vitalSigns;
        this.nursingNotes = nursingNotes;
        this.followUpDate = followUpDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getTreatmentNotes() { return treatmentNotes; }
    public void setTreatmentNotes(String treatmentNotes) { this.treatmentNotes = treatmentNotes; }
    public String getVitalSigns() { return vitalSigns; }
    public void setVitalSigns(String vitalSigns) { this.vitalSigns = vitalSigns; }
    public String getNursingNotes() { return nursingNotes; }
    public void setNursingNotes(String nursingNotes) { this.nursingNotes = nursingNotes; }
    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }
}
