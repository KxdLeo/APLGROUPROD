package careplus.server.repository;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import careplus.common.model.Appointment;
import careplus.common.model.ChatMessage;
import careplus.common.model.Complaint;
import careplus.common.model.Employee;
import careplus.common.model.MedicalRecord;
import careplus.common.model.Patient;
import careplus.common.model.Payment;

public class HibernateHospitalRepository implements HospitalRepository {
    private static final Logger logger = LogManager.getLogger(HibernateHospitalRepository.class);

    private final HospitalRepository fallbackRepository = new InMemoryHospitalRepository();
    private SessionFactory factory;

    public synchronized SessionFactory getFactory() {
        if (factory == null) {
            factory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        }
        return factory;
    }

    @Override
    public Patient findPatient(String patientId) {
        try (Session session = getFactory().openSession()) {
            return session.get(Patient.class, patientId);
        } catch (RuntimeException ex) {
            logger.error("Hibernate patient lookup failed; using sample repository.", ex);
            return fallbackRepository.findPatient(patientId);
        }
    }

    @Override
    public Employee findEmployee(String staffId) {
        return fallbackRepository.findEmployee(staffId);
    }

    @Override
    public List<Patient> findAllPatients() {
        try (Session session = getFactory().openSession()) {
            return session.createQuery("from Patient", Patient.class).list();
        } catch (RuntimeException ex) {
            logger.error("Hibernate patient query failed; using sample repository.", ex);
            return fallbackRepository.findAllPatients();
        }
    }

    @Override
    public Patient savePatient(Patient patient) {
        Transaction transaction = null;
        try (Session session = getFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(patient);
            transaction.commit();
            return patient;
        } catch (RuntimeException ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Hibernate patient insert failed; using sample repository.", ex);
            return fallbackRepository.savePatient(patient);
        }
    }

    @Override
    public Patient updatePatient(Patient patient) {
        return fallbackRepository.updatePatient(patient);
    }

    @Override
    public List<Employee> findAllEmployees() { return fallbackRepository.findAllEmployees(); }

    @Override
    public Employee saveEmployee(Employee employee) { return fallbackRepository.saveEmployee(employee); }

    @Override
    public Employee updateEmployee(Employee employee) { return fallbackRepository.updateEmployee(employee); }

    @Override
    public Complaint saveComplaint(Complaint complaint) {
        Transaction transaction = null;
        try (Session session = getFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(complaint);
            transaction.commit();
            return complaint;
        } catch (RuntimeException ex) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Hibernate complaint insert failed; using sample repository.", ex);
            return fallbackRepository.saveComplaint(complaint);
        }
    }

    @Override
    public void updateComplaint(Complaint complaint) { fallbackRepository.updateComplaint(complaint); }
    @Override
    public List<Complaint> findComplaints(String patientId, String category) { return fallbackRepository.findComplaints(patientId, category); }
    @Override
    public List<Appointment> findAppointments(String patientId, String doctorId) { return fallbackRepository.findAppointments(patientId, doctorId); }
    @Override
    public Appointment saveAppointment(Appointment appointment) { return fallbackRepository.saveAppointment(appointment); }
    @Override
    public List<Payment> findPayments(String patientId) { return fallbackRepository.findPayments(patientId); }
    @Override
    public Payment savePayment(Payment payment) { return fallbackRepository.savePayment(payment); }
    @Override
    public void updatePayment(Payment payment) { fallbackRepository.updatePayment(payment); }
    @Override
    public MedicalRecord saveMedicalRecord(MedicalRecord record) { return fallbackRepository.saveMedicalRecord(record); }
    @Override
    public List<MedicalRecord> findMedicalRecords(String patientId, String employeeId) { return fallbackRepository.findMedicalRecords(patientId, employeeId); }
    @Override
    public ChatMessage saveChatMessage(ChatMessage message) { return fallbackRepository.saveChatMessage(message); }
    @Override
    public List<ChatMessage> findChatMessages(String userId, String receiverRole) { return fallbackRepository.findChatMessages(userId, receiverRole); }
    @Override
    public Map<String, Integer> complaintSummaryByCategory() { return fallbackRepository.complaintSummaryByCategory(); }
}
