package careplus.common.model;

public class Doctor extends Employee {
    private static final long serialVersionUID = 1L;

    private String specialty;

    public Doctor() {
    }

    public Doctor(String id, String firstName, String lastName, String contactNumber,
            String password, String department, String specialty) {
        super(id, firstName, lastName, contactNumber, password, department);
        this.specialty = specialty;
    }

    @Override
    public String getRole() {
        return "Doctor";
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
