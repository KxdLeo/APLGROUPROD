package careplus.server.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import careplus.common.model.Appointment;
import careplus.common.model.ChatMessage;
import careplus.common.model.Complaint;
import careplus.common.model.Doctor;
import careplus.common.model.Employee;
import careplus.common.model.Admin;
import careplus.common.model.MedicalRecord;
import careplus.common.model.Nurse;
import careplus.common.model.Patient;
import careplus.common.model.Payment;
import careplus.common.model.Receptionist;

public class JdbcHospitalRepository implements HospitalRepository {
    private static final Logger logger = LogManager.getLogger(JdbcHospitalRepository.class);

    private final MysqlDataSource dataSource;

    public JdbcHospitalRepository(String serverName, String databaseName, String user, String password) {
        dataSource = new MysqlDataSource();
        dataSource.setServerName(serverName);
        dataSource.setDatabaseName(databaseName);
        dataSource.setUser(user);
        dataSource.setPassword(password);
    }

    @Override
    public Patient findPatient(String patientId) {
        String sql = "select patient_id, first_name, last_name, contact_number, password, medical_history from patients where patient_id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? readPatient(resultSet) : null;
            }
        } catch (SQLException ex) {
            logger.error("Could not load patient {}", patientId, ex);
            throw new RepositoryException("Could not load patient.", ex);
        }
    }

    @Override
    public Employee findEmployee(String staffId) {
        String sql = "select staff_id, first_name, last_name, contact_number, password, department, role, specialty from employees where staff_id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, staffId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? readEmployee(resultSet) : null;
            }
        } catch (SQLException ex) {
            logger.error("Could not load employee {}", staffId, ex);
            throw new RepositoryException("Could not load employee.", ex);
        }
    }

    @Override
    public List<Patient> findAllPatients() {
        List<Patient> patients = new ArrayList<Patient>();
        String sql = "select patient_id, first_name, last_name, contact_number, password, medical_history from patients";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                patients.add(readPatient(resultSet));
            }
            return patients;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load patients.", ex);
        }
    }

    @Override
    public Patient savePatient(Patient patient) {
        String sql = "insert into patients(patient_id, first_name, last_name, contact_number, password, medical_history) "
                + "values (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patient.getId());
            statement.setString(2, patient.getFirstName());
            statement.setString(3, patient.getLastName());
            statement.setString(4, patient.getContactNumber());
            statement.setString(5, patient.getPassword());
            statement.setString(6, patient.getMedicalHistory());
            statement.executeUpdate();
            logger.info("Inserted patient {}", patient.getId());
            return patient;
        } catch (SQLException ex) {
            logger.error("Could not save patient {}", patient.getId(), ex);
            throw new RepositoryException("Could not save patient.", ex);
        }
    }

    @Override
    public Patient updatePatient(Patient patient) {
        String sql = "update patients set first_name = ?, last_name = ?, contact_number = ?, password = ?, medical_history = ? "
                + "where patient_id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patient.getFirstName());
            statement.setString(2, patient.getLastName());
            statement.setString(3, patient.getContactNumber());
            statement.setString(4, patient.getPassword());
            statement.setString(5, patient.getMedicalHistory());
            statement.setString(6, patient.getId());
            statement.executeUpdate();
            logger.info("Updated patient {}", patient.getId());
            return patient;
        } catch (SQLException ex) {
            logger.error("Could not update patient {}", patient.getId(), ex);
            throw new RepositoryException("Could not update patient.", ex);
        }
    }

    @Override
    public List<Employee> findAllEmployees() {
        List<Employee> employees = new ArrayList<Employee>();
        String sql = "select staff_id, first_name, last_name, contact_number, password, department, role, specialty from employees";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                employees.add(readEmployee(resultSet));
            }
            return employees;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load employees.", ex);
        }
    }

    @Override
    public Employee saveEmployee(Employee employee) {
        String sql = "insert into employees(staff_id, first_name, last_name, contact_number, password, department, role, specialty) "
                + "values (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getId());
            statement.setString(2, employee.getFirstName());
            statement.setString(3, employee.getLastName());
            statement.setString(4, employee.getContactNumber());
            statement.setString(5, employee.getPassword());
            statement.setString(6, employee.getDepartment());
            statement.setString(7, employee.getRole());
            statement.setString(8, employee instanceof Doctor ? ((Doctor) employee).getSpecialty() : null);
            statement.executeUpdate();
            logger.info("Inserted employee {}", employee.getId());
            return employee;
        } catch (SQLException ex) {
            logger.error("Could not save employee {}", employee.getId(), ex);
            throw new RepositoryException("Could not save employee.", ex);
        }
    }

    @Override
    public Employee updateEmployee(Employee employee) {
        String sql = "update employees set first_name = ?, last_name = ?, contact_number = ?, password = ?, "
                + "department = ?, role = ?, specialty = ? where staff_id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getFirstName());
            statement.setString(2, employee.getLastName());
            statement.setString(3, employee.getContactNumber());
            statement.setString(4, employee.getPassword());
            statement.setString(5, employee.getDepartment());
            statement.setString(6, employee.getRole());
            statement.setString(7, employee instanceof Doctor ? ((Doctor) employee).getSpecialty() : null);
            statement.setString(8, employee.getId());
            statement.executeUpdate();
            logger.info("Updated employee {}", employee.getId());
            return employee;
        } catch (SQLException ex) {
            logger.error("Could not update employee {}", employee.getId(), ex);
            throw new RepositoryException("Could not update employee.", ex);
        }
    }

    @Override
    public Complaint saveComplaint(Complaint complaint) {
        String sql = "insert into complaints(patient_id, category, description, date_submitted, status) values (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, complaint.getPatientId());
            statement.setString(2, complaint.getCategory());
            statement.setString(3, complaint.getDescription());
            statement.setDate(4, Date.valueOf(complaint.getDateSubmitted()));
            statement.setString(5, complaint.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    complaint.setId(keys.getInt(1));
                }
            }
            return complaint;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not save complaint.", ex);
        }
    }

    @Override
    public void updateComplaint(Complaint complaint) {
        String sql = "update complaints set status = ?, assigned_employee_id = ?, response = ?, response_date = ?, responded_by = ? where complaint_id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, complaint.getStatus());
            statement.setString(2, complaint.getAssignedEmployeeId());
            statement.setString(3, complaint.getResponse());
            statement.setDate(4, complaint.getResponseDate() == null ? null : Date.valueOf(complaint.getResponseDate()));
            statement.setString(5, complaint.getRespondedBy());
            statement.setInt(6, complaint.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Could not update complaint.", ex);
        }
    }

    @Override
    public List<Complaint> findComplaints(String patientId, String category) {
        List<Complaint> complaints = new ArrayList<Complaint>();
        String sql = "select complaint_id, patient_id, category, description, date_submitted, status, assigned_employee_id, response, response_date, responded_by from complaints where (? = '' or patient_id = ?) and (? = '' or category = ?) order by complaint_id";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String safePatient = patientId == null ? "" : patientId;
            String safeCategory = category == null ? "" : category;
            statement.setString(1, safePatient);
            statement.setString(2, safePatient);
            statement.setString(3, safeCategory);
            statement.setString(4, safeCategory);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    complaints.add(readComplaint(resultSet));
                }
            }
            return complaints;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load complaints.", ex);
        }
    }

    @Override
    public List<Appointment> findAppointments(String patientId, String doctorId) {
        List<Appointment> appointments = new ArrayList<Appointment>();
        String sql = "select appointment_id, patient_id, doctor_id, appointment_date, status, appointment_type from appointments where (? = '' or patient_id = ?) and (? = '' or doctor_id = ?) order by appointment_date";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String safePatient = patientId == null ? "" : patientId;
            String safeDoctor = doctorId == null ? "" : doctorId;
            statement.setString(1, safePatient);
            statement.setString(2, safePatient);
            statement.setString(3, safeDoctor);
            statement.setString(4, safeDoctor);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    appointments.add(new Appointment(resultSet.getInt("appointment_id"),
                            resultSet.getString("patient_id"),
                            resultSet.getString("doctor_id"),
                            resultSet.getTimestamp("appointment_date").toLocalDateTime(),
                            resultSet.getString("status"),
                            resultSet.getString("appointment_type")));
                }
            }
            return appointments;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load appointments.", ex);
        }
    }

    @Override
    public Appointment saveAppointment(Appointment appointment) {
        String sql = "insert into appointments(patient_id, doctor_id, appointment_date, status, appointment_type) values (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, appointment.getPatientId());
            statement.setString(2, appointment.getDoctorId());
            statement.setTimestamp(3, Timestamp.valueOf(appointment.getAppointmentDate()));
            statement.setString(4, appointment.getStatus());
            statement.setString(5, appointment.getAppointmentType());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    appointment.setId(keys.getInt(1));
                }
            }
            return appointment;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not save appointment.", ex);
        }
    }

    @Override
    public List<Payment> findPayments(String patientId) {
        List<Payment> payments = new ArrayList<Payment>();
        String sql = "select payment_id, patient_id, amount_paid, payment_date, outstanding_balance, description, status from payments where (? = '' or patient_id = ?) order by payment_date desc";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String safePatient = patientId == null ? "" : patientId;
            statement.setString(1, safePatient);
            statement.setString(2, safePatient);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(new Payment(resultSet.getInt("payment_id"),
                            resultSet.getString("patient_id"),
                            resultSet.getBigDecimal("amount_paid"),
                            resultSet.getDate("payment_date").toLocalDate(),
                            resultSet.getBigDecimal("outstanding_balance"),
                            resultSet.getString("description"),
                            resultSet.getString("status")));
                }
            }
            return payments;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load payments.", ex);
        }
    }

    @Override
    public Payment savePayment(Payment payment) {
        String sql = "insert into payments(patient_id, amount_paid, payment_date, outstanding_balance, description, status) "
                + "values (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, payment.getPatientId());
            statement.setBigDecimal(2, payment.getAmountPaid());
            statement.setDate(3, Date.valueOf(payment.getPaymentDate()));
            statement.setBigDecimal(4, payment.getOutstandingBalance());
            statement.setString(5, payment.getDescription());
            statement.setString(6, payment.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getInt(1));
                }
            }
            return payment;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not save payment.", ex);
        }
    }

    @Override
    public void updatePayment(Payment payment) {
        String sql = "update payments set amount_paid = ?, payment_date = ?, outstanding_balance = ?, description = ?, status = ? where payment_id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, payment.getAmountPaid());
            statement.setDate(2, Date.valueOf(payment.getPaymentDate()));
            statement.setBigDecimal(3, payment.getOutstandingBalance());
            statement.setString(4, payment.getDescription());
            statement.setString(5, payment.getStatus());
            statement.setInt(6, payment.getId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RepositoryException("Could not update payment.", ex);
        }
    }

    @Override
    public MedicalRecord saveMedicalRecord(MedicalRecord record) {
        String sql = "insert into medical_records(patient_id, doctor_id, diagnosis, treatment_notes, vital_signs, nursing_notes, follow_up_date) values (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, record.getPatientId());
            statement.setString(2, record.getDoctorId());
            statement.setString(3, record.getDiagnosis());
            statement.setString(4, record.getTreatmentNotes());
            statement.setString(5, record.getVitalSigns());
            statement.setString(6, record.getNursingNotes());
            statement.setDate(7, record.getFollowUpDate() == null ? null : Date.valueOf(record.getFollowUpDate()));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    record.setId(keys.getInt(1));
                }
            }
            return record;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not save medical record.", ex);
        }
    }

    @Override
    public List<MedicalRecord> findMedicalRecords(String patientId, String employeeId) {
        List<MedicalRecord> records = new ArrayList<MedicalRecord>();
        String sql = "select record_id, patient_id, doctor_id, diagnosis, treatment_notes, vital_signs, "
                + "nursing_notes, follow_up_date from medical_records "
                + "where (? = '' or patient_id = ?) and (? = '' or doctor_id = ?) order by record_id desc";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String safePatient = patientId == null ? "" : patientId;
            String safeEmployee = employeeId == null ? "" : employeeId;
            statement.setString(1, safePatient);
            statement.setString(2, safePatient);
            statement.setString(3, safeEmployee);
            statement.setString(4, safeEmployee);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Date followUpDate = resultSet.getDate("follow_up_date");
                    records.add(new MedicalRecord(resultSet.getInt("record_id"),
                            resultSet.getString("patient_id"),
                            resultSet.getString("doctor_id"),
                            resultSet.getString("diagnosis"),
                            resultSet.getString("treatment_notes"),
                            resultSet.getString("vital_signs"),
                            resultSet.getString("nursing_notes"),
                            followUpDate == null ? null : followUpDate.toLocalDate()));
                }
            }
            return records;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load medical records.", ex);
        }
    }

    @Override
    public ChatMessage saveChatMessage(ChatMessage message) {
        String sql = "insert into chat_messages(sender_id, receiver_role, message, sent_at) values (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, message.getSenderId());
            statement.setString(2, message.getReceiverRole());
            statement.setString(3, message.getMessage());
            statement.setTimestamp(4, Timestamp.valueOf(message.getSentAt()));
            statement.executeUpdate();
            return message;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not save chat message.", ex);
        }
    }

    @Override
    public List<ChatMessage> findChatMessages(String userId, String receiverRole) {
        List<ChatMessage> messages = new ArrayList<ChatMessage>();
        String sql = "select sender_id, receiver_role, message, sent_at from chat_messages "
                + "where (? = '' or sender_id = ?) or (? = '' or receiver_role = ?) order by sent_at";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            String safeUser = userId == null ? "" : userId;
            String safeRole = receiverRole == null ? "" : receiverRole;
            statement.setString(1, safeUser);
            statement.setString(2, safeUser);
            statement.setString(3, safeRole);
            statement.setString(4, safeRole);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    messages.add(new ChatMessage(resultSet.getString("sender_id"),
                            resultSet.getString("receiver_role"),
                            resultSet.getString("message"),
                            resultSet.getTimestamp("sent_at").toLocalDateTime()));
                }
            }
            return messages;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load chat messages.", ex);
        }
    }

    @Override
    public Map<String, Integer> complaintSummaryByCategory() {
        Map<String, Integer> summary = new HashMap<String, Integer>();
        String sql = "select category, count(*) total from complaints group by category";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                summary.put(resultSet.getString("category"), Integer.valueOf(resultSet.getInt("total")));
            }
            return summary;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load dashboard summary.", ex);
        }
    }

    private Patient readPatient(ResultSet resultSet) throws SQLException {
        return new Patient(resultSet.getString("patient_id"), resultSet.getString("first_name"),
                resultSet.getString("last_name"), resultSet.getString("contact_number"),
                resultSet.getString("password"), resultSet.getString("medical_history"));
    }

    private Employee readEmployee(ResultSet resultSet) throws SQLException {
        String role = resultSet.getString("role");
        if ("Doctor".equals(role)) {
            return new Doctor(resultSet.getString("staff_id"), resultSet.getString("first_name"),
                    resultSet.getString("last_name"), resultSet.getString("contact_number"),
                    resultSet.getString("password"), resultSet.getString("department"),
                    resultSet.getString("specialty"));
        }
        if ("Nurse".equals(role)) {
            return new Nurse(resultSet.getString("staff_id"), resultSet.getString("first_name"),
                    resultSet.getString("last_name"), resultSet.getString("contact_number"),
                    resultSet.getString("password"), resultSet.getString("department"));
        }
        if ("Admin".equals(role)) {
            return new Admin(resultSet.getString("staff_id"), resultSet.getString("first_name"),
                    resultSet.getString("last_name"), resultSet.getString("contact_number"),
                    resultSet.getString("password"), resultSet.getString("department"));
        }
        return new Receptionist(resultSet.getString("staff_id"), resultSet.getString("first_name"),
                resultSet.getString("last_name"), resultSet.getString("contact_number"),
                resultSet.getString("password"), resultSet.getString("department"));
    }

    private Complaint readComplaint(ResultSet resultSet) throws SQLException {
        Complaint complaint = new Complaint(resultSet.getInt("complaint_id"),
                resultSet.getString("patient_id"), resultSet.getString("category"),
                resultSet.getString("description"), resultSet.getDate("date_submitted").toLocalDate());
        complaint.setStatus(resultSet.getString("status"));
        complaint.setAssignedEmployeeId(resultSet.getString("assigned_employee_id"));
        complaint.setResponse(resultSet.getString("response"));
        Date responseDate = resultSet.getDate("response_date");
        complaint.setResponseDate(responseDate == null ? null : responseDate.toLocalDate());
        complaint.setRespondedBy(resultSet.getString("responded_by"));
        return complaint;
    }
}
