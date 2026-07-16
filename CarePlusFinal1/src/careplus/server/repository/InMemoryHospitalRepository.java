package careplus.server.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

public class InMemoryHospitalRepository implements HospitalRepository {
    private final Map<String, Patient> patients = new LinkedHashMap<String, Patient>();
    private final Map<String, Employee> employees = new LinkedHashMap<String, Employee>();
    private final List<Complaint> complaints = new ArrayList<Complaint>();
    private final List<Appointment> appointments = new ArrayList<Appointment>();
    private final List<Payment> payments = new ArrayList<Payment>();
    private final List<MedicalRecord> medicalRecords = new ArrayList<MedicalRecord>();
    private final List<ChatMessage> chatMessages = new ArrayList<ChatMessage>();

    private int nextComplaintId = 3;
    private int nextRecordId = 2;
    private int nextChatId = 1;

    public InMemoryHospitalRepository() {
        patients.put("P1001", new Patient("P1001", "Alicia", "Grant", "876-555-1001",
                "pass123", "Asthma; penicillin allergy"));
        patients.put("P1002", new Patient("P1002", "Marcus", "Brown", "876-555-1002",
                "pass123", "Hypertension"));

        employees.put("D2001", new Doctor("D2001", "Nadia", "Lewis", "876-555-2001",
                "staff123", "Outpatient Care", "General Medicine"));
        employees.put("N3001", new Nurse("N3001", "Tamara", "Reid", "876-555-3001",
                "staff123", "Nursing"));
        employees.put("R4001", new Receptionist("R4001", "Kevin", "Morgan", "876-555-4001",
                "staff123", "Front Desk"));

        complaints.add(new Complaint(1, "P1001", "General Health Issue",
                "Shortness of breath after walking.", LocalDate.now().minusDays(2)));
        complaints.add(new Complaint(2, "P1002", "Medication Concern",
                "Dizziness after new medication.", LocalDate.now().minusDays(1)));
        complaints.get(0).setAssignedEmployeeId("D2001");

        appointments.add(new Appointment(1, "P1001", "D2001",
                LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), "Scheduled"));
        appointments.add(new Appointment(2, "P1002", "D2001",
                LocalDateTime.now().plusDays(4).withHour(13).withMinute(30), "Pending"));

        payments.add(new Payment(1, "P1001", new BigDecimal("3500.00"),
                LocalDate.now().minusDays(10), new BigDecimal("1200.00")));
        payments.add(new Payment(2, "P1002", new BigDecimal("5000.00"),
                LocalDate.now().minusDays(5), new BigDecimal("0.00")));

        medicalRecords.add(new MedicalRecord(1, "P1001", "D2001", "Mild asthma flare",
                "Prescribed inhaler and rest.", "BP 120/80, pulse 82",
                "Patient stable.", LocalDate.now().plusWeeks(2)));
    }

    @Override
    public synchronized Patient findPatient(String patientId) {
        return patients.get(patientId);
    }

    @Override
    public synchronized Employee findEmployee(String staffId) {
        return employees.get(staffId);
    }

    @Override
    public synchronized List<Patient> findAllPatients() {
        return new ArrayList<Patient>(patients.values());
    }

    @Override
    public synchronized List<Employee> findAllEmployees() {
        return new ArrayList<Employee>(employees.values());
    }

    @Override
    public synchronized Complaint saveComplaint(Complaint complaint) {
        complaint.setId(nextComplaintId++);
        complaints.add(complaint);
        return complaint;
    }

    @Override
    public synchronized void updateComplaint(Complaint complaint) {
        for (int i = 0; i < complaints.size(); i++) {
            if (complaints.get(i).getId() == complaint.getId()) {
                complaints.set(i, complaint);
                return;
            }
        }
    }

    @Override
    public synchronized List<Complaint> findComplaints(String patientId, String category) {
        List<Complaint> results = new ArrayList<Complaint>();
        for (Complaint complaint : complaints) {
            boolean patientMatches = patientId == null || patientId.length() == 0
                    || patientId.equals(complaint.getPatientId());
            boolean categoryMatches = category == null || category.length() == 0
                    || category.equalsIgnoreCase(complaint.getCategory());
            if (patientMatches && categoryMatches) {
                results.add(complaint);
            }
        }
        return results;
    }

    @Override
    public synchronized List<Appointment> findAppointments(String patientId, String doctorId) {
        List<Appointment> results = new ArrayList<Appointment>();
        for (Appointment appointment : appointments) {
            boolean patientMatches = patientId == null || patientId.length() == 0
                    || patientId.equals(appointment.getPatientId());
            boolean doctorMatches = doctorId == null || doctorId.length() == 0
                    || doctorId.equals(appointment.getDoctorId());
            if (patientMatches && doctorMatches) {
                results.add(appointment);
            }
        }
        return results;
    }

    @Override
    public synchronized List<Payment> findPayments(String patientId) {
        List<Payment> results = new ArrayList<Payment>();
        for (Payment payment : payments) {
            if (patientId == null || patientId.length() == 0 || patientId.equals(payment.getPatientId())) {
                results.add(payment);
            }
        }
        return results;
    }

    @Override
    public synchronized MedicalRecord saveMedicalRecord(MedicalRecord record) {
        record.setId(nextRecordId++);
        medicalRecords.add(record);
        return record;
    }

    @Override
    public synchronized List<MedicalRecord> findMedicalRecords(String patientId, String employeeId) {
        List<MedicalRecord> results = new ArrayList<MedicalRecord>();
        for (MedicalRecord record : medicalRecords) {
            boolean patientMatches = patientId == null || patientId.length() == 0
                    || patientId.equals(record.getPatientId());
            boolean employeeMatches = employeeId == null || employeeId.length() == 0
                    || employeeId.equals(record.getDoctorId());
            if (patientMatches && employeeMatches) {
                results.add(record);
            }
        }
        return results;
    }

    @Override
    public synchronized ChatMessage saveChatMessage(ChatMessage message) {
        nextChatId++;
        chatMessages.add(message);
        return message;
    }

    @Override
    public synchronized List<ChatMessage> findChatMessages(String userId) {
        List<ChatMessage> results = new ArrayList<ChatMessage>();
        for (ChatMessage message : chatMessages) {
            if (userId == null || userId.length() == 0 || userId.equals(message.getSenderId())) {
                results.add(message);
            }
        }
        return results;
    }

    @Override
    public synchronized Map<String, Integer> complaintSummaryByCategory() {
        Map<String, Integer> summary = new HashMap<String, Integer>();
        for (Complaint complaint : complaints) {
            Integer count = summary.get(complaint.getCategory());
            summary.put(complaint.getCategory(), count == null ? 1 : count + 1);
        }
        return summary;
    }
}
