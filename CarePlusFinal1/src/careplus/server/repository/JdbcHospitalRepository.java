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
        String sql = "select appointment_id, patient_id, doctor_id, appointment_date, status from appointments where (? = '' or patient_id = ?) and (? = '' or doctor_id = ?) order by appointment_date";
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
                            resultSet.getString("status")));
                }
            }
            return appointments;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load appointments.", ex);
        }
    }

    @Override
    public List<Payment> findPayments(String patientId) {
        List<Payment> payments = new ArrayList<Payment>();
        String sql = "select payment_id, patient_id, amount_paid, payment_date, outstanding_balance from payments where patient_id = ? order by payment_date desc";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    payments.add(new Payment(resultSet.getInt("payment_id"),
                            resultSet.getString("patient_id"),
                            resultSet.getBigDecimal("amount_paid"),
                            resultSet.getDate("payment_date").toLocalDate(),
                            resultSet.getBigDecimal("outstanding_balance")));
                }
            }
            return payments;
        } catch (SQLException ex) {
            throw new RepositoryException("Could not load payments.", ex);
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
        return new ArrayList<MedicalRecord>();
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
    public List<ChatMessage> findChatMessages(String userId) {
        return new ArrayList<ChatMessage>();
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
