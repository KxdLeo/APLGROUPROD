package careplus.server.repository;

import java.util.List;
import java.util.Map;

import careplus.common.model.Appointment;
import careplus.common.model.ChatMessage;
import careplus.common.model.Complaint;
import careplus.common.model.Employee;
import careplus.common.model.MedicalRecord;
import careplus.common.model.Patient;
import careplus.common.model.Payment;

public interface HospitalRepository {
    Patient findPatient(String patientId);

    Employee findEmployee(String staffId);

    List<Patient> findAllPatients();

    List<Employee> findAllEmployees();

    Complaint saveComplaint(Complaint complaint);

    void updateComplaint(Complaint complaint);

    List<Complaint> findComplaints(String patientId, String category);

    List<Appointment> findAppointments(String patientId, String doctorId);

    List<Payment> findPayments(String patientId);

    MedicalRecord saveMedicalRecord(MedicalRecord record);

    List<MedicalRecord> findMedicalRecords(String patientId, String employeeId);

    ChatMessage saveChatMessage(ChatMessage message);

    List<ChatMessage> findChatMessages(String userId);

    Map<String, Integer> complaintSummaryByCategory();
}
