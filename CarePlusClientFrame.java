package careplus.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import careplus.client.CarePlusClient;
import careplus.common.model.Appointment;
import careplus.common.model.ChatMessage;
import careplus.common.model.Complaint;
import careplus.common.model.Doctor;
import careplus.common.model.Employee;
import careplus.common.model.MedicalRecord;
import careplus.common.model.Nurse;
import careplus.common.model.Patient;
import careplus.common.model.Payment;
import careplus.common.model.Receptionist;
import careplus.common.net.CarePlusRequest;
import careplus.common.net.CarePlusResponse;

public class CarePlusClientFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private static final Color NAVY = new Color(10, 22, 40);
    private static final Color TEAL = new Color(0, 150, 150);
    private static final Color TEAL_DARK = new Color(0, 115, 120);
    private static final Color BG = new Color(235, 245, 248);
    private static final Color CARD = new Color(248, 251, 253);
    private static final Color BORDER = new Color(207, 224, 233);
    private static final Color TEXT = new Color(20, 30, 50);
    private static final Color MUTED = new Color(98, 115, 132);

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private static final Font MENU_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 12);

    private final CarePlusClient client;
    private final String loginMode;
    private final JDesktopPane desktop = new JDesktopPane();
    private Object currentUser;
    private int nextFrameOffset;

    public CarePlusClientFrame(CarePlusClient client, String loginMode) {
        this.client = client;
        this.loginMode = loginMode;
        configureLookAndFeel();
        setTitle("CarePlus Hospital Patient Management System");
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(buildMenuBar());
        desktop.setBackground(BG);

        add(buildShell(), BorderLayout.CENTER);
        openLoginFrame();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(NAVY);
        menuBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JMenu session = new JMenu("Session");
        styleMenu(session);
        session.setMnemonic('S');
        JMenuItem login = new JMenuItem("Login");
        login.setAccelerator(KeyStroke.getKeyStroke("control L"));
        login.addActionListener(e -> resetToLogin());
        session.add(login);
        if (currentUser != null) {
            JMenuItem home = new JMenuItem("Home");
            home.setAccelerator(KeyStroke.getKeyStroke("control shift H"));
            home.addActionListener(e -> openHomeFrame());
            session.add(home);
        }
        menuBar.add(session);

        if (currentUser instanceof Patient) {
            JMenu patient = new JMenu("Patient");
            styleMenu(patient);
            patient.setMnemonic('P');
            addMenuItem(patient, "Submit Complaint", "control M", e -> openSubmitComplaintFrame());
            addMenuItem(patient, "Appointments", "control A", e -> openAppointmentsFrame());
            addMenuItem(patient, "My Complaints", "control C", e -> openComplaintsFrame(false));
            addMenuItem(patient, "Payments", "control P", e -> openPaymentsFrame());
            menuBar.add(patient);
        }

        if (currentUser instanceof Employee) {
            JMenu staff = new JMenu("Staff");
            styleMenu(staff);
            staff.setMnemonic('T');
            addMenuItem(staff, "Dashboard", "control D", e -> openDashboardFrame());
            addMenuItem(staff, "Register Patient", "control N", e -> openRegisterPatientFrame());
            addMenuItem(staff, "Schedule Appointment", "control shift A", e -> openScheduleAppointmentFrame());
            if (isAdmin()) {
                addMenuItem(staff, "Add Staff", "control shift D", e -> openAddStaffFrame(null));
            }
            addMenuItem(staff, "Patients", "control shift P", e -> openPatientsFrame());
            addMenuItem(staff, "Complaints", "control shift C", e -> openComplaintsFrame(true));
            addMenuItem(staff, "Medical Records", "control R", e -> openMedicalRecordFrame());
            addMenuItem(staff, "Payments", "control shift M", e -> openPaymentsFrame());
            addMenuItem(staff, "Employees", "control E", e -> openEmployeesFrame());
            menuBar.add(staff);
        }

        if (currentUser != null) {
            JMenu communication = new JMenu("Communication");
            styleMenu(communication);
            communication.setMnemonic('C');
            addMenuItem(communication, "Live Chat", "control H", e -> openChatFrame());
            menuBar.add(communication);
        }
        return menuBar;
    }

    private void addMenuItem(JMenu menu, String text, String shortcut, java.awt.event.ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(BODY_FONT);
        item.setToolTipText(text);
        item.setAccelerator(KeyStroke.getKeyStroke(shortcut));
        item.addActionListener(listener);
        menu.add(item);
    }

    private void openLoginFrame() {
        JInternalFrame frame = basicFrame("Login", 420, 260);
        JPanel panel = formPanel();
        JTextField idField = textField(16);
        JTextField passwordField = textField(16);
        JComboBox<String> roleBox = new JComboBox<String>(new String[] {"Patient", "Employee"});
        styleCombo(roleBox);
        roleBox.setSelectedItem("employee".equalsIgnoreCase(loginMode) ? "Employee" : "Patient");
        addField(panel, 0, "Login Type", roleBox);
        addField(panel, 1, "ID", idField);
        addField(panel, 2, "Password", passwordField);
        JButton loginButton = primaryButton("Login");
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
            refreshChrome();
            frame.dispose();
            show(response.getMessage());
            openHomeFrame();
        });
        addButton(panel, 3, loginButton);
        frame.add(panel);
        showFrame(frame);
    }

    private void openSubmitComplaintFrame() {
        Patient patient = requirePatient();
        if (patient == null) return;
        JInternalFrame frame = basicFrame("Submit Medical Request", 640, 390);
        JPanel panel = formPanel();
        JComboBox<String> category = new JComboBox<String>(new String[] {
                "General Health Issue", "Medication Concern", "Appointment Issue"});
        styleCombo(category);
        JTextArea description = new JTextArea(6, 30);
        styleTextArea(description);
        addField(panel, 0, "Category", category);
        addField(panel, 1, "Description", new JScrollPane(description));
        JButton submit = primaryButton("Submit");
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
        if (currentUser == null) {
            show("Login first.");
            return;
        }
        String patientId = currentUser instanceof Patient ? ((Patient) currentUser).getId() : "";
        String doctorId = currentUser instanceof Employee && "Doctor".equals(((Employee) currentUser).getRole())
                ? ((Employee) currentUser).getId() : "";
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_APPOINTMENTS")
                .with("patientId", patientId).with("doctorId", doctorId));
        showTable("Appointments", asList(response.getData()), new String[] {
                "ID", "Patient", "Doctor", "Date", "Type", "Status"}, (row, col) -> {
            Appointment a = (Appointment) row;
            switch (col) {
                case 0: return a.getId();
                case 1: return a.getPatientId();
                case 2: return a.getDoctorId();
                case 3: return a.getAppointmentDate();
                case 4: return a.getAppointmentType();
                default: return a.getStatus();
            }
        });
    }

    private void openScheduleAppointmentFrame() {
        if (requireEmployee() == null) {
            return;
        }

        CarePlusResponse patientsResponse = client.send(new CarePlusRequest("LIST_PATIENTS"));
        CarePlusResponse employeesResponse = client.send(new CarePlusRequest("LIST_EMPLOYEES"));

        JInternalFrame frame = basicFrame("Schedule Patient Appointment", 940, 520);
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = formPanel();
        JComboBox<String> patientBox = new JComboBox<String>(patientChoices(asList(patientsResponse.getData())));
        JComboBox<String> doctorBox = new JComboBox<String>(employeeChoices(asList(employeesResponse.getData()), "Doctor"));
        JComboBox<String> dateBox = new JComboBox<String>(appointmentDateChoices());
        JComboBox<String> timeBox = new JComboBox<String>(appointmentTimeChoices());
        JComboBox<String> typeBox = new JComboBox<String>(new String[] {"General Check Up", "Lab Tests"});
        JComboBox<String> statusBox = new JComboBox<String>(new String[] {"Scheduled", "Pending", "Completed", "Cancelled"});
        styleCombo(patientBox);
        styleCombo(doctorBox);
        styleCombo(dateBox);
        styleCombo(timeBox);
        styleCombo(typeBox);
        styleCombo(statusBox);

        addField(form, 0, "Patient", patientBox);
        addField(form, 1, "Doctor", doctorBox);
        addField(form, 2, "Open Date", dateBox);
        addField(form, 3, "Open Time", timeBox);
        addField(form, 4, "Appointment Type", typeBox);
        addField(form, 5, "Status", statusBox);

        JButton save = primaryButton("Save Appointment");
        addButton(form, 6, save);
        root.add(form, BorderLayout.WEST);

        DefaultTableModel slotModel = new DefaultTableModel(new Object[] {"Doctor", "Open Date", "Open Time"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable slots = new JTable(slotModel);
        styleTable(slots);
        JPanel slotPanel = new JPanel(new BorderLayout(0, 8));
        slotPanel.setBackground(CARD);
        JLabel slotTitle = new JLabel("Open Appointment Dates and Times");
        slotTitle.setFont(LABEL_FONT);
        slotTitle.setForeground(TEXT);
        slotPanel.add(slotTitle, BorderLayout.NORTH);
        slotPanel.add(new JScrollPane(slots), BorderLayout.CENTER);
        root.add(slotPanel, BorderLayout.CENTER);

        Runnable refreshSlots = () -> loadOpenAppointmentSlots(slotModel, parseChoiceId(doctorBox.getSelectedItem()));
        doctorBox.addActionListener(e -> refreshSlots.run());
        slots.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || slots.getSelectedRow() < 0) {
                return;
            }
            int row = slots.convertRowIndexToModel(slots.getSelectedRow());
            dateBox.setSelectedItem(slotModel.getValueAt(row, 1));
            timeBox.setSelectedItem(slotModel.getValueAt(row, 2));
        });
        refreshSlots.run();

        save.addActionListener(e -> {
            try {
                String patientId = parseChoiceId(patientBox.getSelectedItem());
                String doctorId = parseChoiceId(doctorBox.getSelectedItem());
                LocalDateTime appointmentDate = LocalDateTime.parse(dateBox.getSelectedItem() + "T" + timeBox.getSelectedItem());
                Appointment appointment = new Appointment(0, patientId, doctorId,
                        appointmentDate, String.valueOf(statusBox.getSelectedItem()),
                        String.valueOf(typeBox.getSelectedItem()));
                CarePlusResponse response = client.send(new CarePlusRequest("SAVE_APPOINTMENT")
                        .with("appointment", appointment));
                show(response.getMessage());
                if (response.isSuccess()) {
                    refreshSlots.run();
                }
            } catch (RuntimeException ex) {
                show("Use a valid appointment date and time.");
            }
        });

        frame.add(root, BorderLayout.CENTER);
        showFrame(frame);
    }

    private void openComplaintsFrame(boolean staffMode) {
        if (staffMode && requireEmployee() == null) {
            return;
        }
        if (!staffMode && requirePatient() == null) {
            return;
        }
        String patientId = !staffMode && currentUser instanceof Patient ? ((Patient) currentUser).getId() : "";
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_COMPLAINTS")
                .with("patientId", patientId).with("category", ""));
        List<?> rows = asList(response.getData());
        DefaultTableModel model = new DefaultTableModel(new Object[] {
                "ID", "Patient", "Category", "Description", "Status", "Assigned", "Response"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return staffMode && column == 4;
            }
        };
        for (Object item : rows) {
            Complaint c = (Complaint) item;
            model.addRow(new Object[] {Integer.valueOf(c.getId()), c.getPatientId(), c.getCategory(),
                    c.getDescription(), c.getStatus(), c.getAssignedEmployeeId(), c.getResponse()});
        }
        JTable table = new JTable(model);
        styleTable(table);
        if (staffMode) {
            JComboBox<String> statusEditor = new JComboBox<String>(new String[] {"Unresolved", "Resolved"});
            styleCombo(statusEditor);
            table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(statusEditor));
        }
        JInternalFrame frame = basicFrame(staffMode ? "Manage Patient Complaints" : "My Medical Requests", 900, 420);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        if (staffMode) {
            JPanel controls = new JPanel();
            controls.setBackground(CARD);
            controls.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            CarePlusResponse employees = client.send(new CarePlusRequest("LIST_EMPLOYEES"));
            JComboBox<String> assigned = new JComboBox<String>(employeeChoices(asList(employees.getData()), ""));
            styleCombo(assigned);
            JTextField responseText = textField(35);
            JButton respond = primaryButton("Respond / Assign");
            respond.setMnemonic('R');
            table.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                    return;
                }
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                selectChoiceById(assigned, model.getValueAt(row, 5));
                responseText.setText(nullSafe(model.getValueAt(row, 6)));
            });
            respond.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    show("Select a complaint first.");
                    return;
                }
                int modelRow = table.convertRowIndexToModel(row);
                Integer complaintId = (Integer) model.getValueAt(modelRow, 0);
                Employee employee = requireEmployee();
                if (employee == null) return;
                CarePlusResponse update = client.send(new CarePlusRequest("RESPOND_COMPLAINT")
                        .with("complaintId", complaintId)
                        .with("employeeId", employee.getId())
                        .with("assignedEmployeeId", parseChoiceId(assigned.getSelectedItem()))
                        .with("response", responseText.getText().trim())
                        .with("status", String.valueOf(model.getValueAt(modelRow, 4))));
                show(update.getMessage());
                frame.dispose();
                openComplaintsFrame(true);
            });
            controls.add(new JLabel("Assign Staff"));
            controls.add(assigned);
            controls.add(new JLabel("Response"));
            controls.add(responseText);
            controls.add(respond);
            frame.add(controls, BorderLayout.SOUTH);
        }
        showFrame(frame);
    }

    private void openPaymentsFrame() {
        if (currentUser == null) {
            show("Login first.");
            return;
        }
        boolean staffMode = currentUser instanceof Employee;
        String patientId = currentUser instanceof Patient ? ((Patient) currentUser).getId() : "";
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_PAYMENTS").with("patientId", patientId));
        List<?> rows = asList(response.getData());
        Map<Integer, BigDecimal> paymentTotals = new HashMap<Integer, BigDecimal>();
        DefaultTableModel model = new DefaultTableModel(new Object[] {
                "ID", "Patient", "Description", "Amount Paid", "Payment Date", "Outstanding Balance", "Status"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return staffMode && (column == 3 || column == 6);
            }
        };
        for (Object item : rows) {
            Payment payment = (Payment) item;
            BigDecimal total = moneyOrZero(payment.getAmountPaid()).add(moneyOrZero(payment.getOutstandingBalance()));
            paymentTotals.put(Integer.valueOf(payment.getId()), total);
            model.addRow(new Object[] {Integer.valueOf(payment.getId()), payment.getPatientId(),
                    payment.getDescription(), payment.getAmountPaid(), payment.getPaymentDate(),
                    payment.getOutstandingBalance(), payment.getStatus()});
        }

        JTable table = new JTable(model);
        styleTable(table);
        if (staffMode) {
            JComboBox<String> statusEditor = new JComboBox<String>(new String[] {"Unpaid", "Paid"});
            styleCombo(statusEditor);
            table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(statusEditor));
            final boolean[] recalculating = new boolean[] {false};
            model.addTableModelListener(e -> {
                if (recalculating[0] || e.getType() != TableModelEvent.UPDATE || e.getColumn() != 3) {
                    return;
                }
                int row = e.getFirstRow();
                Integer paymentId = (Integer) model.getValueAt(row, 0);
                BigDecimal total = paymentTotals.get(paymentId);
                BigDecimal amountPaid = parseMoney(model.getValueAt(row, 3));
                BigDecimal outstanding = total.subtract(amountPaid);
                if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
                    outstanding = BigDecimal.ZERO;
                }
                recalculating[0] = true;
                model.setValueAt(outstanding, row, 5);
                model.setValueAt(outstanding.compareTo(BigDecimal.ZERO) == 0 ? "Paid" : "Unpaid", row, 6);
                recalculating[0] = false;
            });
        }

        JInternalFrame frame = basicFrame(staffMode ? "Manage Payments" : "Payment History", 900, 420);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        if (staffMode) {
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            controls.setBackground(CARD);
            JButton update = primaryButton("Update Payment");
            update.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    show("Select a payment first.");
                    return;
                }
                int modelRow = table.convertRowIndexToModel(row);
                CarePlusResponse updated = client.send(new CarePlusRequest("UPDATE_PAYMENT_STATUS")
                        .with("paymentId", (Integer) model.getValueAt(modelRow, 0))
                        .with("employeeId", currentUserId())
                        .with("amountPaid", parseMoney(model.getValueAt(modelRow, 3)))
                        .with("status", String.valueOf(model.getValueAt(modelRow, 6))));
                show(updated.getMessage());
                frame.dispose();
                openPaymentsFrame();
            });
            controls.add(update);
            frame.add(controls, BorderLayout.SOUTH);
        }
        showFrame(frame);
    }

    private void openDashboardFrame() {
        if (requireEmployee() == null) {
            return;
        }
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
        if (requireEmployee() == null) {
            return;
        }
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_PATIENTS"));
        List<?> rows = asList(response.getData());
        SimpleTableModel model = new SimpleTableModel(new String[] {
                "ID", "Name", "Contact", "Medical History"}, (row, col) -> {
            Patient p = (Patient) row;
            switch (col) {
                case 0: return p.getId();
                case 1: return p.getFullName();
                case 2: return p.getContactNumber();
                default: return p.getMedicalHistory();
            }
        });
        model.setRows(rows);
        JTable table = new JTable(model);
        styleTable(table);
        JInternalFrame frame = basicFrame("Patients", 840, 380);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton addPatient = primaryButton("Register Patient");
        addPatient.addActionListener(e -> openRegisterPatientFrame());
        JButton editPatient = primaryButton("Edit Patient");
        editPatient.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                show("Select a patient first.");
                return;
            }
            Patient patient = (Patient) model.getRow(table.convertRowIndexToModel(row));
            openEditPatientFrame(patient);
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        footer.setBackground(CARD);
        footer.add(editPatient);
        footer.add(addPatient);
        frame.add(footer, BorderLayout.SOUTH);
        showFrame(frame);
    }

    private void openRegisterPatientFrame() {
        if (requireEmployee() == null) {
            return;
        }

        JInternalFrame frame = basicFrame("Register Patient", 660, 430);
        JPanel panel = formPanel();
        JTextField patientId = textField(14);
        JTextField firstName = textField(18);
        JTextField lastName = textField(18);
        JTextField contact = textField(18);
        JTextField password = textField(18);
        JTextArea history = new JTextArea(5, 32);
        styleTextArea(history);

        addField(panel, 0, "Patient ID", patientId);
        addField(panel, 1, "First Name", firstName);
        addField(panel, 2, "Last Name", lastName);
        addField(panel, 3, "Contact Number", contact);
        addField(panel, 4, "Password", password);
        addField(panel, 5, "Medical History", new JScrollPane(history));

        JButton save = primaryButton("Create Patient Login");
        save.addActionListener(e -> {
            Patient patient = new Patient(patientId.getText().trim(), firstName.getText().trim(),
                    lastName.getText().trim(), contact.getText().trim(), password.getText().trim(),
                    history.getText().trim());
            CarePlusResponse response = client.send(new CarePlusRequest("SAVE_PATIENT").with("patient", patient));
            show(response.getMessage());
            if (response.isSuccess()) {
                frame.dispose();
                openPatientsFrame();
            }
        });
        addButton(panel, 6, save);

        frame.add(panel, BorderLayout.CENTER);
        showFrame(frame);
    }

    private void openEditPatientFrame(Patient patient) {
        if (requireEmployee() == null) {
            return;
        }

        JInternalFrame frame = basicFrame("Edit Patient", 660, 430);
        JPanel panel = formPanel();
        JTextField patientId = textField(14);
        patientId.setText(patient.getId());
        patientId.setEditable(false);
        JTextField firstName = textField(18);
        firstName.setText(patient.getFirstName());
        JTextField lastName = textField(18);
        lastName.setText(patient.getLastName());
        JTextField contact = textField(18);
        contact.setText(patient.getContactNumber());
        JTextField password = textField(18);
        password.setText(patient.getPassword());
        JTextArea history = new JTextArea(5, 32);
        history.setText(patient.getMedicalHistory());
        styleTextArea(history);

        addField(panel, 0, "Patient ID", patientId);
        addField(panel, 1, "First Name", firstName);
        addField(panel, 2, "Last Name", lastName);
        addField(panel, 3, "Contact Number", contact);
        addField(panel, 4, "Password", password);
        addField(panel, 5, "Medical History", new JScrollPane(history));

        JButton save = primaryButton("Update Patient");
        save.addActionListener(e -> {
            Patient updatedPatient = new Patient(patientId.getText().trim(), firstName.getText().trim(),
                    lastName.getText().trim(), contact.getText().trim(), password.getText().trim(),
                    history.getText().trim());
            CarePlusResponse response = client.send(new CarePlusRequest("UPDATE_PATIENT")
                    .with("employeeId", currentUserId())
                    .with("patient", updatedPatient));
            show(response.getMessage());
            if (response.isSuccess()) {
                frame.dispose();
                openPatientsFrame();
            }
        });
        addButton(panel, 6, save);

        frame.add(panel, BorderLayout.CENTER);
        showFrame(frame);
    }

    private void openAddStaffFrame(String defaultRole) {
        if (!isAdmin()) {
            show("Login as an admin first.");
            return;
        }

        JInternalFrame frame = basicFrame("Add Staff Member", 660, 460);
        JPanel panel = formPanel();
        JTextField staffId = textField(14);
        JComboBox<String> role = new JComboBox<String>(new String[] {"Doctor", "Nurse", "Receptionist"});
        JTextField firstName = textField(18);
        JTextField lastName = textField(18);
        JTextField contact = textField(18);
        JTextField password = textField(18);
        JTextField department = textField(18);
        JTextField specialty = textField(18);
        styleCombo(role);
        if (defaultRole != null) {
            role.setSelectedItem(defaultRole);
        }

        addField(panel, 0, "Staff Role", role);
        addField(panel, 1, "Staff ID", staffId);
        addField(panel, 2, "First Name", firstName);
        addField(panel, 3, "Last Name", lastName);
        addField(panel, 4, "Contact Number", contact);
        addField(panel, 5, "Password", password);
        addField(panel, 6, "Department", department);
        addField(panel, 7, "Specialty", specialty);
        role.addActionListener(e -> specialty.setEnabled("Doctor".equals(role.getSelectedItem())));
        specialty.setEnabled("Doctor".equals(role.getSelectedItem()));

        JButton save = primaryButton("Create Staff Login");
        save.addActionListener(e -> {
            Employee employee = buildStaffMember(String.valueOf(role.getSelectedItem()), staffId.getText().trim(),
                    firstName.getText().trim(), lastName.getText().trim(), contact.getText().trim(),
                    password.getText().trim(), department.getText().trim(), specialty.getText().trim());
            CarePlusResponse response = client.send(new CarePlusRequest("SAVE_EMPLOYEE")
                    .with("adminId", currentUserId())
                    .with("employee", employee));
            show(response.getMessage());
            if (response.isSuccess()) {
                frame.dispose();
                openEmployeesFrame();
            }
        });
        addButton(panel, 8, save);

        frame.add(panel, BorderLayout.CENTER);
        showFrame(frame);
    }

    private void openEmployeesFrame() {
        if (requireEmployee() == null) {
            return;
        }
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_EMPLOYEES"));
        List<?> rows = asList(response.getData());
        SimpleTableModel model = new SimpleTableModel(new String[] {
                "ID", "Name", "Role", "Department", "Specialty", "Contact"}, (row, col) -> {
            Employee e = (Employee) row;
            switch (col) {
                case 0: return e.getId();
                case 1: return e.getFullName();
                case 2: return e.getRole();
                case 3: return e.getDepartment();
                case 4: return e instanceof Doctor ? ((Doctor) e).getSpecialty() : "";
                default: return e.getContactNumber();
            }
        });
        model.setRows(rows);
        JTable table = new JTable(model);
        styleTable(table);
        JInternalFrame frame = basicFrame("Doctors / Nurses / Receptionists", 900, 400);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        if (isAdmin()) {
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            footer.setBackground(CARD);
            JButton editStaff = primaryButton("Edit Staff");
            editStaff.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    show("Select a staff member first.");
                    return;
                }
                Employee employee = (Employee) model.getRow(table.convertRowIndexToModel(row));
                if (!canAdminEditStaff(employee)) {
                    show("Admin can edit doctors, nurses, and receptionists.");
                    return;
                }
                openEditStaffFrame(employee);
            });
            JButton addStaff = primaryButton("Add Staff");
            addStaff.addActionListener(e -> openAddStaffFrame(null));
            footer.add(editStaff);
            footer.add(addStaff);
            frame.add(footer, BorderLayout.SOUTH);
        }
        showFrame(frame);
    }

    private void openEditStaffFrame(Employee employee) {
        if (!isAdmin()) {
            show("Login as an admin first.");
            return;
        }

        JInternalFrame frame = basicFrame("Edit Staff Member", 660, 460);
        JPanel panel = formPanel();
        JTextField staffId = textField(14);
        staffId.setText(employee.getId());
        staffId.setEditable(false);
        JComboBox<String> role = new JComboBox<String>(new String[] {"Doctor", "Nurse", "Receptionist"});
        role.setSelectedItem(employee.getRole());
        role.setEnabled(false);
        styleCombo(role);
        JTextField firstName = textField(18);
        firstName.setText(employee.getFirstName());
        JTextField lastName = textField(18);
        lastName.setText(employee.getLastName());
        JTextField contact = textField(18);
        contact.setText(employee.getContactNumber());
        JTextField password = textField(18);
        password.setText(employee.getPassword());
        JTextField department = textField(18);
        department.setText(employee.getDepartment());
        JTextField specialty = textField(18);
        specialty.setText(employee instanceof Doctor ? ((Doctor) employee).getSpecialty() : "");
        specialty.setEnabled(employee instanceof Doctor);

        addField(panel, 0, "Staff Role", role);
        addField(panel, 1, "Staff ID", staffId);
        addField(panel, 2, "First Name", firstName);
        addField(panel, 3, "Last Name", lastName);
        addField(panel, 4, "Contact Number", contact);
        addField(panel, 5, "Password", password);
        addField(panel, 6, "Department", department);
        addField(panel, 7, "Specialty", specialty);

        JButton save = primaryButton("Update Staff");
        save.addActionListener(e -> {
            Employee updatedEmployee = buildStaffMember(String.valueOf(role.getSelectedItem()), staffId.getText().trim(),
                    firstName.getText().trim(),
                    lastName.getText().trim(), contact.getText().trim(), password.getText().trim(),
                    department.getText().trim(), specialty.getText().trim());
            CarePlusResponse response = client.send(new CarePlusRequest("UPDATE_EMPLOYEE")
                    .with("adminId", currentUserId())
                    .with("employee", updatedEmployee));
            show(response.getMessage());
            if (response.isSuccess()) {
                frame.dispose();
                openEmployeesFrame();
            }
        });
        addButton(panel, 8, save);

        frame.add(panel, BorderLayout.CENTER);
        showFrame(frame);
    }

    private void openMedicalRecordFrame() {
        Employee employee = requireEmployee();
        if (employee == null) return;
        JInternalFrame frame = basicFrame("Doctor / Nurse Case Handling", 660, 460);
        JPanel panel = formPanel();
        JTextField patientId = new JTextField(12);
        styleTextField(patientId);
        JTextField diagnosis = textField(28);
        JTextField treatment = textField(28);
        JTextField vitals = textField(28);
        JTextField nursing = textField(28);
        JTextField followUp = textField(12);
        addField(panel, 0, "Patient ID", patientId);
        addField(panel, 1, "Diagnosis", diagnosis);
        addField(panel, 2, "Treatment Notes", treatment);
        addField(panel, 3, "Vital Signs", vitals);
        addField(panel, 4, "Nursing Notes", nursing);
        addField(panel, 5, "Follow-up Date yyyy-mm-dd", followUp);
        JButton save = primaryButton("Save Record");
        save.addActionListener(e -> {
            LocalDate followDate = followUp.getText().trim().length() == 0
                    ? null : LocalDate.parse(followUp.getText().trim());
            MedicalRecord record = new MedicalRecord(0, patientId.getText().trim(), employee.getId(),
                    diagnosis.getText().trim(), treatment.getText().trim(), vitals.getText().trim(),
                    nursing.getText().trim(), followDate);
            CarePlusResponse response = client.send(new CarePlusRequest("SAVE_MEDICAL_RECORD").with("record", record));
            show(response.getMessage());
        });
        JButton view = primaryButton("View Records");
        view.addActionListener(e -> openMedicalRecordsList(patientId.getText().trim(), employee.getId()));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(CARD);
        buttons.add(save);
        buttons.add(view);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(buttons, gbc);
        frame.add(panel);
        showFrame(frame);
    }

    private void openMedicalRecordsList(String patientId, String employeeId) {
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_MEDICAL_RECORDS")
                .with("patientId", patientId == null ? "" : patientId)
                .with("employeeId", employeeId == null ? "" : employeeId));
        showTable("Saved Medical Records", asList(response.getData()), new String[] {
                "ID", "Patient", "Doctor/Nurse", "Diagnosis", "Treatment", "Vitals", "Nursing Notes", "Follow-up"}, (row, col) -> {
            MedicalRecord record = (MedicalRecord) row;
            switch (col) {
                case 0: return record.getId();
                case 1: return record.getPatientId();
                case 2: return record.getDoctorId();
                case 3: return record.getDiagnosis();
                case 4: return record.getTreatmentNotes();
                case 5: return record.getVitalSigns();
                case 6: return record.getNursingNotes();
                default: return record.getFollowUpDate();
            }
        });
    }

    private void openChatFrame() {
        if (currentUser == null) {
            show("Login first.");
            return;
        }
        JInternalFrame frame = basicFrame("Live Chat", 820, 520);
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        DefaultTableModel chatModel = new DefaultTableModel(new Object[] {"Sender", "To", "Message", "Sent At"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(chatModel);
        styleTable(table);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel panel = formPanel();
        JComboBox<String> receiver = new JComboBox<String>(new String[] {"Receptionist", "Doctor", "Nurse", "Admin", "Patient"});
        styleCombo(receiver);
        JTextArea message = new JTextArea(5, 30);
        styleTextArea(message);
        addField(panel, 0, "Send To", receiver);
        addField(panel, 1, "Message", new JScrollPane(message));
        JButton refresh = primaryButton("Refresh");
        refresh.addActionListener(e -> loadChatMessages(chatModel));
        JButton send = primaryButton("Send");
        send.addActionListener(e -> {
            CarePlusResponse response = client.send(new CarePlusRequest("SEND_CHAT")
                    .with("senderId", currentUserId())
                    .with("receiverRole", receiver.getSelectedItem())
                    .with("message", message.getText()));
            show(response.getMessage());
            if (response.isSuccess()) {
                message.setText("");
                loadChatMessages(chatModel);
            }
        });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            int row = table.convertRowIndexToModel(table.getSelectedRow());
            String sender = String.valueOf(chatModel.getValueAt(row, 0));
            if (currentUserId().equals(sender)) {
                receiver.setSelectedItem(chatModel.getValueAt(row, 1));
            } else {
                receiver.setSelectedItem(roleForSenderId(sender));
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setBackground(CARD);
        buttons.add(send);
        buttons.add(refresh);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(buttons, gbc);

        root.add(panel, BorderLayout.SOUTH);
        frame.add(root, BorderLayout.CENTER);
        loadChatMessages(chatModel);
        showFrame(frame);
    }

    private void openHomeFrame() {
        if (currentUser == null) {
            openLoginFrame();
            return;
        }

        JInternalFrame frame = basicFrame("Home", 760, 460);
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel(currentUser instanceof Patient ? "Patient Dashboard" : "Staff Dashboard");
        title.setFont(TITLE_FONT);
        title.setForeground(TEXT);
        root.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(2, currentUser instanceof Patient ? 2 : 3, 14, 14));
        cards.setBackground(BG);

        if (currentUser instanceof Patient) {
            cards.add(actionCard("Submit Medical Request", "Register a complaint or request follow-up help.",
                    "Open", e -> openSubmitComplaintFrame()));
            cards.add(actionCard("Appointments", "View upcoming appointment date, doctor, and status.",
                    "View", e -> openAppointmentsFrame()));
            cards.add(actionCard("Previous Complaints", "Check request responses and response dates.",
                    "View", e -> openComplaintsFrame(false)));
            cards.add(actionCard("Payments", "Review payments and outstanding balances.",
                    "View", e -> openPaymentsFrame()));
        } else {
            cards.add(actionCard("Complaint Dashboard", "View request totals grouped by complaint category.",
                    "Open", e -> openDashboardFrame()));
            cards.add(actionCard("Register Patient", "Create a patient account with a login password.",
                    "Add", e -> openRegisterPatientFrame()));
            cards.add(actionCard("Schedule Appointment", "Book a patient with open doctor dates and times.",
                    "Book", e -> openScheduleAppointmentFrame()));
            if (isAdmin()) {
                cards.add(actionCard("Add Staff", "Create doctor, nurse, and receptionist login accounts.",
                        "Add", e -> openAddStaffFrame(null)));
            }
            cards.add(actionCard("Manage Complaints", "Respond to patients and assign doctors or nurses.",
                    "Open", e -> openComplaintsFrame(true)));
            cards.add(actionCard("Medical Records", "Add diagnosis, treatment notes, vital signs, and follow-up.",
                    "Open", e -> openMedicalRecordFrame()));
            cards.add(actionCard("Payments", "Review patient balances and mark charges paid or unpaid.",
                    "Open", e -> openPaymentsFrame()));
        }

        root.add(cards, BorderLayout.CENTER);

        JButton chat = primaryButton("Open Live Chat");
        chat.addActionListener(e -> openChatFrame());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setBackground(BG);
        actions.add(chat);
        root.add(actions, BorderLayout.SOUTH);

        frame.add(root, BorderLayout.CENTER);
        showFrame(frame);
    }

    private void showTable(String title, List<?> rows, String[] columns, SimpleTableModel.RowMapper mapper) {
        showFrame(tableFrame(title, rows, columns, mapper));
    }

    private JInternalFrame tableFrame(String title, List<?> rows, String[] columns, SimpleTableModel.RowMapper mapper) {
        SimpleTableModel model = new SimpleTableModel(columns, mapper);
        model.setRows(rows);
        JInternalFrame frame = basicFrame(title, 840, 380);
        JTable table = new JTable(model);
        styleTable(table);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        return frame;
    }

    private JInternalFrame basicFrame(String title, int width, int height) {
        JInternalFrame frame = new JInternalFrame(title, true, true, true, true);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BG);
        frame.setFrameIcon(null);
        frame.setSize(width, height);
        return frame;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        return panel;
    }

    private void addField(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(LABEL_FONT);
        labelComponent.setForeground(TEXT);
        panel.add(labelComponent, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        styleComponent(component);
        panel.add(component, gbc);
    }

    private void addButton(JPanel panel, int row, JButton button) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        styleButton(button, TEAL);
        panel.add(button, gbc);
    }

    private void showFrame(JInternalFrame frame) {
        frame.setLocation(24 + nextFrameOffset, 24 + nextFrameOffset);
        nextFrameOffset = (nextFrameOffset + 28) % 140;
        desktop.add(frame);
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (java.beans.PropertyVetoException ignored) {
        }
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("InternalFrame.activeTitleBackground", NAVY);
        UIManager.put("InternalFrame.activeTitleForeground", Color.WHITE);
        UIManager.put("InternalFrame.inactiveTitleBackground", new Color(80, 100, 120));
        UIManager.put("InternalFrame.inactiveTitleForeground", Color.WHITE);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("CarePlus Hospital Patient Management System");
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel user = new JLabel(currentUserLabel());
        user.setFont(BODY_FONT);
        user.setForeground(new Color(205, 235, 238));
        header.add(user, BorderLayout.EAST);
        return header;
    }

    private JPanel buildShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(BG);
        shell.add(buildHeaderPanel(), BorderLayout.NORTH);
        if (currentUser != null) {
            shell.add(buildSidebarPanel(), BorderLayout.WEST);
        }
        shell.add(desktop, BorderLayout.CENTER);
        return shell;
    }

    private JPanel buildSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new javax.swing.BoxLayout(sidebar, javax.swing.BoxLayout.Y_AXIS));
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));

        JLabel role = new JLabel(currentUser instanceof Patient ? "PATIENT" : ((Employee) currentUser).getRole().toUpperCase());
        role.setFont(LABEL_FONT);
        role.setForeground(new Color(170, 220, 225));
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(role);
        sidebar.add(javax.swing.Box.createVerticalStrut(12));

        sidebar.add(navButton("Home", e -> openHomeFrame()));
        sidebar.add(javax.swing.Box.createVerticalStrut(8));

        if (currentUser instanceof Patient) {
            sidebar.add(navButton("Submit Complaint", e -> openSubmitComplaintFrame()));
            sidebar.add(navButton("Appointments", e -> openAppointmentsFrame()));
            sidebar.add(navButton("My Complaints", e -> openComplaintsFrame(false)));
            sidebar.add(navButton("Payments", e -> openPaymentsFrame()));
        } else {
            sidebar.add(navButton("Dashboard", e -> openDashboardFrame()));
            sidebar.add(navButton("Register Patient", e -> openRegisterPatientFrame()));
            sidebar.add(navButton("Schedule Appointment", e -> openScheduleAppointmentFrame()));
            if (isAdmin()) {
                sidebar.add(navButton("Add Staff", e -> openAddStaffFrame(null)));
            }
            sidebar.add(navButton("Patients", e -> openPatientsFrame()));
            sidebar.add(navButton("Complaints", e -> openComplaintsFrame(true)));
            sidebar.add(navButton("Medical Records", e -> openMedicalRecordFrame()));
            sidebar.add(navButton("Payments", e -> openPaymentsFrame()));
            sidebar.add(navButton("Employees", e -> openEmployeesFrame()));
        }

        sidebar.add(javax.swing.Box.createVerticalGlue());
        sidebar.add(navButton("Live Chat", e -> openChatFrame()));
        sidebar.add(javax.swing.Box.createVerticalStrut(8));
        sidebar.add(navButton("Switch Login", e -> resetToLogin()));
        return sidebar;
    }

    private String currentUserLabel() {
        if (currentUser instanceof Patient) {
            Patient patient = (Patient) currentUser;
            return "Patient: " + patient.getFullName() + " (" + patient.getId() + ")";
        }
        if (currentUser instanceof Employee) {
            Employee employee = (Employee) currentUser;
            return employee.getRole() + ": " + employee.getFullName() + " (" + employee.getId() + ")";
        }
        return "Not logged in";
    }

    private void refreshChrome() {
        setJMenuBar(buildMenuBar());
        getContentPane().removeAll();
        add(buildShell(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel actionCard(String title, String description, String buttonText,
            java.awt.event.ActionListener listener) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));

        JPanel copy = new JPanel(new BorderLayout(0, 6));
        copy.setBackground(CARD);
        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setForeground(TEXT);
        JLabel body = new JLabel("<html><body style='width:240px'>" + description + "</body></html>");
        body.setFont(BODY_FONT);
        body.setForeground(MUTED);
        copy.add(heading, BorderLayout.NORTH);
        copy.add(body, BorderLayout.CENTER);

        JButton button = primaryButton(buttonText);
        button.addActionListener(listener);
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonRow.setBackground(CARD);
        buttonRow.add(button);

        card.add(copy, BorderLayout.CENTER);
        card.add(buttonRow, BorderLayout.SOUTH);
        return card;
    }

    private JButton navButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(MENU_FONT);
        button.setForeground(TEAL);
        button.setBackground(NAVY);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        button.setHorizontalAlignment(JButton.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addActionListener(listener);
        return button;
    }

    private void resetToLogin() {
        currentUser = null;
        desktop.removeAll();
        refreshChrome();
        openLoginFrame();
    }

    private void styleMenu(JMenu menu) {
        menu.setFont(MENU_FONT);
        menu.setForeground(Color.WHITE);
        menu.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    private JTextField textField(int columns) {
        JTextField field = new JTextField(columns);
        styleTextField(field);
        return field;
    }

    private void styleTextField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setForeground(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 32));
    }

    private void styleTextArea(JTextArea area) {
        area.setFont(BODY_FONT);
        area.setForeground(TEXT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(BODY_FONT);
        combo.setBackground(Color.WHITE);
        combo.setForeground(TEXT);
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, 32));
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, TEAL);
        return button;
    }

    private void styleButton(JButton button, Color background) {
        button.setFont(LABEL_FONT);
        button.setForeground(NAVY);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setOpaque(true);
    }

    private void styleComponent(Component component) {
        if (component instanceof JTextField) {
            styleTextField((JTextField) component);
        } else if (component instanceof JTextArea) {
            styleTextArea((JTextArea) component);
        } else if (component instanceof JComboBox<?>) {
            @SuppressWarnings("unchecked")
            JComboBox<String> combo = (JComboBox<String>) component;
            styleCombo(combo);
        } else if (component instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) component;
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER));
            scrollPane.getViewport().setBackground(Color.WHITE);
        } else if (component instanceof JComponent) {
            ((JComponent) component).setBorder(BorderFactory.createLineBorder(BORDER));
        }
    }

    private void styleTable(JTable table) {
        table.setFont(BODY_FONT);
        table.setForeground(TEXT);
        table.setRowHeight(28);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(204, 240, 240));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(LABEL_FONT);
        header.setBackground(TEAL_DARK);
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 32));
    }

    private String[] patientChoices(List<?> rows) {
        List<String> choices = new ArrayList<String>();
        for (Object item : rows) {
            Patient patient = (Patient) item;
            choices.add(patient.getId() + " - " + patient.getFullName());
        }
        return choices.toArray(new String[choices.size()]);
    }

    private String[] employeeChoices(List<?> rows, String roleFilter) {
        List<String> choices = new ArrayList<String>();
        for (Object item : rows) {
            Employee employee = (Employee) item;
            if (roleFilter == null || roleFilter.length() == 0 || roleFilter.equals(employee.getRole())) {
                choices.add(employee.getId() + " - " + employee.getFullName() + " (" + employee.getRole() + ")");
            }
        }
        return choices.toArray(new String[choices.size()]);
    }

    private String[] appointmentDateChoices() {
        String[] dates = new String[14];
        LocalDate start = LocalDate.now().plusDays(1);
        for (int i = 0; i < dates.length; i++) {
            dates[i] = start.plusDays(i).toString();
        }
        return dates;
    }

    private String[] appointmentTimeChoices() {
        return new String[] {"08:00", "09:00", "10:00", "11:00", "13:30", "14:30", "15:30", "16:30"};
    }

    private void loadOpenAppointmentSlots(DefaultTableModel model, String doctorId) {
        model.setRowCount(0);
        if (doctorId == null || doctorId.length() == 0) {
            return;
        }
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_APPOINTMENTS")
                .with("patientId", "").with("doctorId", doctorId));
        Set<String> bookedSlots = new HashSet<String>();
        for (Object item : asList(response.getData())) {
            Appointment appointment = (Appointment) item;
            if (appointment.getAppointmentDate() != null) {
                bookedSlots.add(appointment.getAppointmentDate().toLocalDate().toString()
                        + " " + appointmentSlotTime(appointment.getAppointmentDate()));
            }
        }
        for (String date : appointmentDateChoices()) {
            for (String time : appointmentTimeChoices()) {
                if (!bookedSlots.contains(date + " " + time)) {
                    model.addRow(new Object[] {doctorId, date, time});
                }
            }
        }
    }

    private void loadChatMessages(DefaultTableModel model) {
        model.setRowCount(0);
        CarePlusResponse response = client.send(new CarePlusRequest("LIST_CHAT")
                .with("userId", currentUserId())
                .with("receiverRole", currentUserRole()));
        for (Object item : asList(response.getData())) {
            ChatMessage message = (ChatMessage) item;
            model.addRow(new Object[] {message.getSenderId(), message.getReceiverRole(),
                    message.getMessage(), message.getSentAt()});
        }
    }

    private String appointmentSlotTime(LocalDateTime appointmentDate) {
        return String.format("%02d:%02d", Integer.valueOf(appointmentDate.getHour()),
                Integer.valueOf(appointmentDate.getMinute()));
    }

    private BigDecimal parseMoney(Object value) {
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (RuntimeException ex) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal moneyOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String parseChoiceId(Object selected) {
        if (selected == null) {
            return "";
        }
        String value = String.valueOf(selected);
        int marker = value.indexOf(" - ");
        return marker < 0 ? value.trim() : value.substring(0, marker).trim();
    }

    private void selectChoiceById(JComboBox<String> combo, Object idValue) {
        String id = nullSafe(idValue);
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (parseChoiceId(combo.getItemAt(i)).equals(id)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private String nullSafe(Object value) {
        return value == null ? "" : String.valueOf(value);
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

    private boolean isAdmin() {
        return currentUser instanceof Employee && "Admin".equals(((Employee) currentUser).getRole());
    }

    private boolean canAdminEditStaff(Employee employee) {
        return employee instanceof Doctor || employee instanceof Nurse || employee instanceof Receptionist;
    }

    private Employee buildStaffMember(String role, String id, String firstName, String lastName,
            String contactNumber, String password, String department, String specialty) {
        if ("Doctor".equals(role)) {
            return new Doctor(id, firstName, lastName, contactNumber, password, department, specialty);
        }
        if ("Nurse".equals(role)) {
            return new Nurse(id, firstName, lastName, contactNumber, password, department);
        }
        return new Receptionist(id, firstName, lastName, contactNumber, password, department);
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

    private String currentUserRole() {
        if (currentUser instanceof Patient) {
            return "Patient";
        }
        if (currentUser instanceof Employee) {
            return ((Employee) currentUser).getRole();
        }
        return "";
    }

    private String roleForSenderId(String senderId) {
        if (senderId == null || senderId.length() == 0) {
            return "Receptionist";
        }
        char prefix = Character.toUpperCase(senderId.charAt(0));
        if (prefix == 'P') {
            return "Patient";
        }
        if (prefix == 'D') {
            return "Doctor";
        }
        if (prefix == 'N') {
            return "Nurse";
        }
        if (prefix == 'R') {
            return "Receptionist";
        }
        if (prefix == 'A') {
            return "Admin";
        }
        return "Receptionist";
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
