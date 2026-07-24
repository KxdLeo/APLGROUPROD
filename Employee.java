package careplus.common.model;

public abstract class Employee extends Person {
    private static final long serialVersionUID = 1L;

    private String password;
    private String department;

    protected Employee() {
    }

    protected Employee(String id, String firstName, String lastName, String contactNumber,
            String password, String department) {
        super(id, firstName, lastName, contactNumber);
        this.password = password;
        this.department = department;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
