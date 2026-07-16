package careplus.client.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import careplus.client.CarePlusClient;
import careplus.common.model.Appointment;
import careplus.common.model.ChatMessage;
import careplus.common.model.Complaint;
import careplus.common.model.Employee;
import careplus.common.model.MedicalRecord;
import careplus.common.model.Patient;
import careplus.common.model.Payment;
import careplus.common.net.CarePlusRequest;
import careplus.common.net.CarePlusResponse;

public class CarePlusClientFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final CarePlusClient client;
    private final String loginMode;
    private final JDesktopPane desktop = new JDesktopPane();
    private Object currentUser;

    public CarePlusClientFrame(CarePlusClient client, String loginMode) {
        this.client = client;
        this.loginMode = loginMode;
        setTitle("CarePlus Hospital Patient Management System");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(buildMenuBar());
        add(desktop, BorderLayout.CENTER);
        openLoginFrame();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu session = new JMenu("Session");
        session.setMnemonic('S');
        JMenuItem login = new JMenuItem("Login");
        login.setAccelerator(KeyStroke.getKeyStroke("control L"));
        login.addActionListener(e -> openLoginFrame());
        session.add(login);

        JMenu patient = new JMenu("Patient");
        patient.setMnemonic('P');
        addMenuItem(patient, "Submit Complaint", "control M", e -> openSubmitComplaintFrame());
        addMenuItem(patient, "Appointments", "control A", e -> openAppointmentsFrame());
        addMenuItem(patient, "My Complaints", "control C", e -> openComplaintsFrame(false));
        addMenuItem(patient, "Payments", "control P", e -> openPaymentsFrame());

        JMenu staff = new JMenu("Staff");
        staff.setMnemonic('T');
        addMenuItem(staff, "Dashboard", "control D", e -> openDashboardFrame());
        addMenuItem(staff, "Patients", "control shift P", e -> openPatientsFrame());
        addMenuItem(staff, "Complaints", "control shift C", e -> openComplaintsFrame(true));
        addMenuItem(staff, "Medical Records", "control R", e -> openMedicalRecordFrame());
        addMenuItem(staff, "Employees", "control E", e -> openEmployeesFrame());

        JMenu communication = new JMenu("Communication");
        communication.setMnemonic('C');
        addMenuItem(communication, "Live Chat", "control H", e -> openChatFrame());

        menuBar.add(session);
        menuBar.add(patient);
        menuBar.add(staff);
        menuBar.add(communication);
        return menuBar;
    }

    private void addMenuItem(JMenu menu, String text, String shortcut, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setToolTipText(text);
        item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
        item.addActionListener(listener);
        menu.add(item);
    }

    private void openLoginFrame() {
        JInternalFrame frame = basicFrame("Login", 360, 230);
        JPanel panel = formPanel();
        JTextField idField = new JTextField(16);
        JTextField passwordField = new JTextField(16);
        JComboBox<String> roleBox = new JComboBox<String>(new String[] {"Patient", "Employee"});
        roleBox.setSelectedItem("employee".equalsIgnoreCase(loginMode) ? "Employee" : "Patient");
        addField(panel, 0, "Login Type", roleBox);
        addField(panel, 1, "ID", idField);
        addField(panel, 2, "Password", passwordField);
        JButton loginButton = new JButton("Login");
        loginButton.setMnemonic('L');
        loginButton.addActionListener(e -> {
            String action = "Patient".equals(roleBox.getSelectedItem()) ? "LOGIN_PATIENT" : "LOGIN_EMPLOYEE";
            CarePlusResponse response = client.send(new CarePlusRequest(action)
                    .with("id", idField.getText().trim())
                    .with("password", passwordField.getText().trim()));
            if (!response.isSuccess()) {
                show(response.getMessage());
                return;
            }
            currentUser = response.getData();
            frame.dispose();
            show(response.getMessage());
            if (currentUser instanceof Patient) {
                openSubmitComplaintFrame();
            } else {
                openDashboardFrame();
            }
        });
        addButton(panel, 3, loginButton);
        frame.add(panel);
        showFrame(frame);
    }

    private void openSubmitComplaintFrame() {
        Patient patient = requirePatient();
        if (patient == null) return;
        JInternalFrame frame = basicFrame("Submit Medical Request", 520, 340);
        JPanel panel = formPanel();
        JComboBox<String> category = new JComboBox<String>(new String[] {
                "General Health Issue", "Medication Concern", "Appointment Issue"});
        JTextArea description = new JTextArea(6, 30);
        addField(panel, 0, "Category", category);
        addField(panel, 1, "Description", new JScrollPane(description));
        JButton submit = new JButton("Submit");
        submit.setToolTipText("Submit complaint to server");
        submit.addActionListener(e -> {
            CarePlusResponse response = client.send(new CarePlusRequest("SUBMIT_COMPLAINT")
                    .with("patientId", patient.getId())
                    .with("category", category.getSelectedItem())
                    .with("description", description.getText()));
            show(response.getMessage());
        });
        addButton(panel, 2, submit);
        frame.add(panel);
        showFrame(frame);
    }

    private void openAppointmentsFrame() {
        String patientId = currentUser instanceof Patient ? ((Patient) currentUser).getId() : "";
        String doctorId = currentUser instanceof Employee && "Doctor".equals(((Employee) currentUser).getRole())
                ? ((Employee) currentUser).getId() : "";
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_APPOINTMENTS")
                .with("patientId", patientId).with("doctorId", doctorId));
        showTable("Appointments", asList(response.getData()), new String[] {
                "ID", "Patient", "Doctor", "Date", "Status"}, (row, col) -> {
            Appointment a = (Appointment) row;
            switch (col) {
                case 0: return a.getId();
                case 1: return a.getPatientId();
                case 2: return a.getDoctorId();
                case 3: return a.getAppointmentDate();
                default: return a.getStatus();
            }
        });
    }

    private void openComplaintsFrame(boolean staffMode) {
        String patientId = !staffMode && currentUser instanceof Patient ? ((Patient) currentUser).getId() : "";
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_COMPLAINTS")
                .with("patientId", patientId).with("category", ""));
        List<?> rows = asList(response.getData());
        SimpleTableModel model = new SimpleTableModel(new String[] {
                "ID", "Patient", "Category", "Description", "Status", "Assigned", "Response"}, (row, col) -> {
            Complaint c = (Complaint) row;
            switch (col) {
                case 0: return c.getId();
                case 1: return c.getPatientId();
                case 2: return c.getCategory();
                case 3: return c.getDescription();
                case 4: return c.getStatus();
                case 5: return c.getAssignedEmployeeId();
                default: return c.getResponse();
            }
        });
        model.setRows(rows);
        JTable table = new JTable(model);
        JInternalFrame frame = basicFrame(staffMode ? "Manage Patient Complaints" : "My Medical Requests", 900, 420);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        if (staffMode) {
            JPanel controls = new JPanel();
            JTextField assigned = new JTextField(8);
            JTextField responseText = new JTextField(35);
            JButton respond = new JButton("Respond / Assign");
            respond.setMnemonic('R');
            respond.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    show("Select a complaint first.");
                    return;
                }
                Complaint complaint = (Complaint) model.getRow(table.convertRowIndexToModel(row));
                Employee employee = requireEmployee();
                if (employee == null) return;
                CarePlusResponse update = client.send(new CarePlusRequest("RESPOND_COMPLAINT")
                        .with("complaintId", Integer.valueOf(complaint.getId()))
                        .with("employeeId", employee.getId())
                        .with("assignedEmployeeId", assigned.getText().trim())
                        .with("response", responseText.getText().trim()));
                show(update.getMessage());
                frame.dispose();
                openComplaintsFrame(true);
            });
            controls.add(new JLabel("Assign Staff ID"));
            controls.add(assigned);
            controls.add(new JLabel("Response"));
            controls.add(responseText);
            controls.add(respond);
            frame.add(controls, BorderLayout.SOUTH);
        }
        showFrame(frame);
    }

    private void openPaymentsFrame() {
        Patient patient = requirePatient();
        if (patient == null) return;
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_PAYMENTS").with("patientId", patient.getId()));
        showTable("Payment History", asList(response.getData()), new String[] {
                "ID", "Amount Paid", "Payment Date", "Outstanding Balance"}, (row, col) -> {
            Payment p = (Payment) row;
            switch (col) {
                case 0: return p.getId();
                case 1: return p.getAmountPaid();
                case 2: return p.getPaymentDate();
                default: return p.getOutstandingBalance();
            }
        });
    }

    private void openDashboardFrame() {
        requireEmployee();
        CarePlusResponse response = client.send(new CarePlusRequest("DASHBOARD_SUMMARY"));
        Map<?, ?> summary = (Map<?, ?>) response.getData();
        List<Object[]> rows = new ArrayList<Object[]>();
        if (summary != null) {
            for (Map.Entry<?, ?> entry : summary.entrySet()) {
                rows.add(new Object[] {entry.getKey(), entry.getValue()});
            }
        }
        showTable("Receptionist Dashboard - Requests By Category", rows, new String[] {
                "Category", "Total Requests"}, (row, col) -> ((Object[]) row)[col]);
    }

    private void openPatientsFrame() {
        requireEmployee();
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_PATIENTS"));
        showTable("Patients", asList(response.getData()), new String[] {
                "ID", "Name", "Contact", "Medical History"}, (row, col) -> {
            Patient p = (Patient) row;
            switch (col) {
                case 0: return p.getId();
                case 1: return p.getFullName();
                case 2: return p.getContactNumber();
                default: return p.getMedicalHistory();
            }
        });
    }

    private void openEmployeesFrame() {
        requireEmployee();
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_EMPLOYEES"));
        showTable("Doctors / Nurses / Receptionists", asList(response.getData()), new String[] {
                "ID", "Name", "Role", "Department", "Contact"}, (row, col) -> {
            Employee e = (Employee) row;
            switch (col) {
                case 0: return e.getId();
                case 1: return e.getFullName();
                case 2: return e.getRole();
                case 3: return e.getDepartment();
                default: return e.getContactNumber();
            }
        });
    }

    private void openMedicalRecordFrame() {
        Employee employee = requireEmployee();
        if (employee == null) return;
        JInternalFrame frame = basicFrame("Doctor / Nurse Case Handling", 580, 420);
        JPanel panel = formPanel();
        JTextField patientId = new JTextField(12);
        JTextField diagnosis = new JTextField(28);
        JTextField treatment = new JTextField(28);
        JTextField vitals = new JTextField(28);
        JTextField nursing = new JTextField(28);
        JTextField followUp = new JTextField(12);
        addField(panel, 0, "Patient ID", patientId);
        addField(panel, 1, "Diagnosis", diagnosis);
        addField(panel, 2, "Treatment Notes", treatment);
        addField(panel, 3, "Vital Signs", vitals);
        addField(panel, 4, "Nursing Notes", nursing);
        addField(panel, 5, "Follow-up Date yyyy-mm-dd", followUp);
        JButton save = new JButton("Save Record");
        save.addActionListener(e -> {
            LocalDate followDate = followUp.getText().trim().length() == 0
                    ? null : LocalDate.parse(followUp.getText().trim());
            MedicalRecord record = new MedicalRecord(0, patientId.getText().trim(), employee.getId(),
                    diagnosis.getText().trim(), treatment.getText().trim(), vitals.getText().trim(),
                    nursing.getText().trim(), followDate);
            CarePlusResponse response = client.send(new CarePlusRequest("SAVE_MEDICAL_RECORD").with("record", record));
            show(response.getMessage());
        });
        addButton(panel, 6, save);
        frame.add(panel);
        showFrame(frame);
    }

    private void openChatFrame() {
        if (currentUser == null) {
            show("Login first.");
            return;
        }
        JInternalFrame frame = basicFrame("Live Chat", 520, 300);
        JPanel panel = formPanel();
        JComboBox<String> receiver = new JComboBox<String>(new String[] {"Receptionist", "Doctor", "Nurse", "Patient"});
        JTextArea message = new JTextArea(5, 30);
        addField(panel, 0, "Send To", receiver);
        addField(panel, 1, "Message", new JScrollPane(message));
        JButton send = new JButton("Send");
        send.addActionListener(e -> {
            CarePlusResponse response = client.send(new CarePlusRequest("SEND_CHAT")
                    .with("senderId", currentUserId())
                    .with("receiverRole", receiver.getSelectedItem())
                    .with("message", message.getText()));
            show(response.getMessage());
        });
        addButton(panel, 2, send);
        frame.add(panel);
        showFrame(frame);
    }

    private void showTable(String title, List<?> rows, String[] columns, SimpleTableModel.RowMapper mapper) {
        SimpleTableModel model = new SimpleTableModel(columns, mapper);
        model.setRows(rows);
        JInternalFrame frame = basicFrame(title, 840, 380);
        frame.add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        showFrame(frame);
    }

    private JInternalFrame basicFrame(String title, int width, int height) {
        JInternalFrame frame = new JInternalFrame(title, true, true, true, true);
        frame.setLayout(new BorderLayout());
        frame.setSize(width, height);
        return frame;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private void addField(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    private void addButton(JPanel panel, int row, JButton button) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(button, gbc);
    }

    private void showFrame(JInternalFrame frame) {
        desktop.add(frame);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
    }

    private Patient requirePatient() {
        if (currentUser instanceof Patient) {
            return (Patient) currentUser;
        }
        show("Login as a patient first.");
        return null;
    }

    private Employee requireEmployee() {
        if (currentUser instanceof Employee) {
            return (Employee) currentUser;
        }
        show("Login as hospital staff first.");
        return null;
    }

    private String currentUserId() {
        if (currentUser instanceof Patient) {
            return ((Patient) currentUser).getId();
        }
        if (currentUser instanceof Employee) {
            return ((Employee) currentUser).getId();
        }
        return "";
    }

    private List<?> asList(Object data) {
        if (data instanceof List<?>) {
            return (List<?>) data;
        }
        return new ArrayList<Object>();
    }

    private void show(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
