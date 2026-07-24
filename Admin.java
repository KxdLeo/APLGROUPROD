package careplus.common.model;

public class Admin extends Employee {
    private static final long serialVersionUID = 1L;

    public Admin() {
    }

    public Admin(String id, String firstName, String lastName, String contactNumber,
            String password, String department) {
        super(id, firstName, lastName, contactNumber, password, department);
    }

    @Override
    public String getRole() {
        return "Admin";
    }
}
