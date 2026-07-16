package careplus.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import careplus.common.model.ChatMessage;
import careplus.common.model.Complaint;
import careplus.common.model.Employee;
import careplus.common.model.MedicalRecord;
import careplus.common.model.Patient;
import careplus.common.net.CarePlusRequest;
import careplus.common.net.CarePlusResponse;
import careplus.server.repository.HospitalRepository;

public class HospitalService {
    private static final Logger logger = LogManager.getLogger(HospitalService.class);
    private final HospitalRepository repository;

    public HospitalService(HospitalRepository repository) {
        this.repository = repository;
    }

    public CarePlusResponse handle(CarePlusRequest request) {
        String action = request.getAction();
        try {
            if ("LOGIN_PATIENT".equals(action)) {
                return loginPatient((String) request.get("id"), (String) request.get("password"));
            }
            if ("LOGIN_EMPLOYEE".equals(action)) {
                return loginEmployee((String) request.get("id"), (String) request.get("password"));
            }
            if ("SUBMIT_COMPLAINT".equals(action)) {
                Complaint complaint = new Complaint(0, (String) request.get("patientId"),
                        (String) request.get("category"), (String) request.get("description"), LocalDate.now());
                return CarePlusResponse.ok("Complaint submitted.", repository.saveComplaint(complaint));
            }
            if ("LIST_COMPLAINTS".equals(action)) {
                return CarePlusResponse.ok("Complaints loaded.",
                        repository.findComplaints((String) request.get("patientId"), (String) request.get("category")));
            }
            if ("RESPOND_COMPLAINT".equals(action)) {
                return respondToComplaint((Integer) request.get("complaintId"), (String) request.get("employeeId"),
                        (String) request.get("assignedEmployeeId"), (String) request.get("response"));
            }
            if ("LIST_APPOINTMENTS".equals(action)) {
                return CarePlusResponse.ok("Appointments loaded.",
                        repository.findAppointments((String) request.get("patientId"), (String) request.get("doctorId")));
            }
            if ("LIST_PAYMENTS".equals(action)) {
                return CarePlusResponse.ok("Payments loaded.", repository.findPayments((String) request.get("patientId")));
            }
            if ("LIST_PATIENTS".equals(action)) {
                return CarePlusResponse.ok("Patients loaded.", repository.findAllPatients());
            }
            if ("LIST_EMPLOYEES".equals(action)) {
                return CarePlusResponse.ok("Employees loaded.", repository.findAllEmployees());
            }
            if ("DASHBOARD_SUMMARY".equals(action)) {
                return CarePlusResponse.ok("Summary loaded.", repository.complaintSummaryByCategory());
            }
            if ("SAVE_MEDICAL_RECORD".equals(action)) {
                MedicalRecord record = (MedicalRecord) request.get("record");
                return CarePlusResponse.ok("Medical record saved.", repository.saveMedicalRecord(record));
            }
            if ("LIST_MEDICAL_RECORDS".equals(action)) {
                return CarePlusResponse.ok("Medical records loaded.",
                        repository.findMedicalRecords((String) request.get("patientId"), (String) request.get("employeeId")));
            }
            if ("SEND_CHAT".equals(action)) {
                return sendChat((String) request.get("senderId"), (String) request.get("receiverRole"),
                        (String) request.get("message"));
            }
            if ("LIST_CHAT".equals(action)) {
                return CarePlusResponse.ok("Chat loaded.", repository.findChatMessages((String) request.get("userId")));
            }
            return CarePlusResponse.fail("Unknown server action: " + action);
        } catch (RuntimeException ex) {
            logger.error("Request failed: {}", action, ex);
            return CarePlusResponse.fail("Server error while handling " + action + ".");
        }
    }

    private CarePlusResponse loginPatient(String id, String password) {
        Patient patient = repository.findPatient(id);
        if (patient != null && patient.getPassword().equals(password)) {
            logger.info("Patient logged in: {}", id);
            return CarePlusResponse.ok("Patient login successful.", patient);
        }
        logger.warn("Failed patient login for {}", id);
        return CarePlusResponse.fail("Invalid patient ID or password.");
    }

    private CarePlusResponse loginEmployee(String id, String password) {
        Employee employee = repository.findEmployee(id);
        if (employee != null && employee.getPassword().equals(password)) {
            logger.info("{} logged in: {}", employee.getRole(), id);
            return CarePlusResponse.ok("Employee login successful.", employee);
        }
        logger.warn("Failed employee login for {}", id);
        return CarePlusResponse.fail("Invalid staff ID or password.");
    }

    private CarePlusResponse respondToComplaint(Integer complaintId, String employeeId,
            String assignedEmployeeId, String responseText) {
        List<Complaint> complaints = repository.findComplaints(null, null);
        for (Complaint complaint : complaints) {
            if (complaint.getId() == complaintId.intValue()) {
                complaint.setAssignedEmployeeId(assignedEmployeeId);
                complaint.setResponse(responseText);
                complaint.setRespondedBy(employeeId);
                complaint.setResponseDate(LocalDate.now());
                complaint.setStatus("Resolved");
                repository.updateComplaint(complaint);
                logger.info("Complaint {} resolved by {}", complaintId, employeeId);
                return CarePlusResponse.ok("Complaint updated.", complaint);
            }
        }
        return CarePlusResponse.fail("Complaint not found.");
    }

    private CarePlusResponse sendChat(String senderId, String receiverRole, String messageText) {
        LocalTime now = LocalTime.now();
        if (now.isBefore(LocalTime.of(8, 0)) || now.isAfter(LocalTime.of(19, 0))) {
            return CarePlusResponse.fail("Live chat is available from 8:00 a.m. to 7:00 p.m.");
        }
        ChatMessage message = new ChatMessage(senderId, receiverRole, messageText, LocalDateTime.now());
        repository.saveChatMessage(message);
        logger.info("Chat message sent by {} to {}", senderId, receiverRole);
        return CarePlusResponse.ok("Chat message sent.", message);
    }
}
