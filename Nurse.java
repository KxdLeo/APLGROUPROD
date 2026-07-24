package careplus.common.model;

public class Nurse extends Employee {
    private static final long serialVersionUID = 1L;

    public Nurse() {
    }

    public Nurse(String id, String firstName, String lastName, String contactNumber,
            String password, String department) {
        super(id, firstName, lastName, contactNumber, password, department);
    }

    @Override
    public String getRole() {
        return "Nurse";
    }
}
