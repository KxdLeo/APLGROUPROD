package careplus.common.model;

public class Patient extends Person {
    private static final long serialVersionUID = 1L;

    private String password;
    private String medicalHistory;

    public Patient() {
    }

    public Patient(String id, String firstName, String lastName, String contactNumber,
            String password, String medicalHistory) {
        super(id, firstName, lastName, contactNumber);
        this.password = password;
        this.medicalHistory = medicalHistory;
    }

    @Override
    public String getRole() {
        return "Patient";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
}
