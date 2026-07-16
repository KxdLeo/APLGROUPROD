package com.careplus.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.careplus.dto.Request;
import com.careplus.dto.Response;

public class ClientHandler implements Runnable {

    private static final Logger logger = LogManager.getLogger(ClientHandler.class);

    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Server server;

    // stores the logged-in user after successful login
    // role will be: PATIENT, DOCTOR, NURSE, or RECEPTIONIST
    private int currentUserId = -1;
    private String currentRole = null;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            // outputStream MUST be created first to avoid deadlock
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            outputStream.flush();
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

            while (true) {
                Object obj = inputStream.readObject();

                if (obj instanceof Request) {
                    Request request = (Request) obj;
                    Response response = handleRequest(request);
                    outputStream.writeObject(response);
                    outputStream.flush();
                }
            }

        } catch (Exception e) {
            logger.info("Client disconnected: {}", e.getMessage());

        } finally {
            if (outputStream != null) {
                server.unregisterClient(outputStream);
            }
            closeQuietly();
        }
    }

    private Response handleRequest(Request request) {

        Response response = new Response();
        response.setCorrelationId(request.getCorrelationId());

        try {
            RequestType type = request.getActionType();

            // ----- LOGIN -----
            if (type == RequestType.LOGIN) {

                // payload is a String array: [id, password, role]
                // e.g. ["P001", "pass123", "PATIENT"] or ["S005", "pass", "DOCTOR"]
                String[] credentials = (String[]) request.getPayload();
                String id = credentials[0];
                String password = credentials[1];
                String role = credentials[2];

                // TODO: call Member 1's AuthDAO to verify id + password + role
                // boolean valid = authDAO.login(id, password, role);
                boolean valid = true; // placeholder until DAO is ready

                if (valid) {
                    // TODO: get the real integer userId from the DB
                    int userId = Integer.parseInt(id.replaceAll("[^0-9]", ""));
                    currentUserId = userId;
                    currentRole = role;
                    server.registerClient(userId, outputStream);
                    response.setStatus("SUCCESS");
                    response.setMessage("Login successful.");
                    response.setPayload(role); // send role back so client knows which dashboard to open
                } else {
                    response.setStatus("ERROR");
                    response.setMessage("Invalid credentials.");
                }

            // ----- PATIENT: submit a complaint -----
            } else if (type == RequestType.SUBMIT_COMPLAINT) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("PATIENT")) return accessDenied(response);

                // payload is a String array: [category, description, dateSubmitted]
                String[] complaint = (String[]) request.getPayload();

                // TODO: call Member 1's ComplaintDAO to save it
                // complaintDAO.insert(currentUserId, complaint[0], complaint[1], complaint[2]);
                logger.info("Complaint submitted by patientId: {}", currentUserId);

                response.setStatus("SUCCESS");
                response.setMessage("Complaint submitted successfully.");

            // ----- PATIENT: view their own complaints -----
            } else if (type == RequestType.VIEW_MY_COMPLAINTS) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("PATIENT")) return accessDenied(response);

                // TODO: return complaintDAO.getByPatientId(currentUserId);
                response.setStatus("SUCCESS");
                response.setMessage("Complaints loaded.");
                response.setPayload(null); // replace null with DAO result

            // ----- PATIENT: view upcoming appointments -----
            } else if (type == RequestType.VIEW_MY_APPOINTMENTS) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("PATIENT")) return accessDenied(response);

                // TODO: return appointmentDAO.getByPatientId(currentUserId);
                response.setStatus("SUCCESS");
                response.setMessage("Appointments loaded.");
                response.setPayload(null); // replace null with DAO result

            // ----- PATIENT: view payment history -----
            } else if (type == RequestType.VIEW_MY_PAYMENTS) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("PATIENT")) return accessDenied(response);

                // TODO: return paymentDAO.getByPatientId(currentUserId);
                response.setStatus("SUCCESS");
                response.setMessage("Payments loaded.");
                response.setPayload(null); // replace null with DAO result

            // ----- RECEPTIONIST: view all complaints -----
            } else if (type == RequestType.GET_ALL_COMPLAINTS) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("RECEPTIONIST")) return accessDenied(response);

                // TODO: return complaintDAO.getAll();
                response.setStatus("SUCCESS");
                response.setMessage("All complaints loaded.");
                response.setPayload(null); // replace null with DAO result

            // ----- RECEPTIONIST: respond to a complaint -----
            } else if (type == RequestType.RESPOND_TO_COMPLAINT) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("RECEPTIONIST")) return accessDenied(response);

                // payload is a String array: [complaintId, responseText]
                String[] data = (String[]) request.getPayload();

                // TODO: complaintDAO.respond(data[0], data[1], currentUserId);
                logger.info("Receptionist {} responded to complaintId: {}",
                        currentUserId, data[0]);

                response.setStatus("SUCCESS");
                response.setMessage("Response saved.");

            // ----- RECEPTIONIST: assign doctor or nurse to a complaint -----
            } else if (type == RequestType.ASSIGN_STAFF) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("RECEPTIONIST")) return accessDenied(response);

                // payload is a String array: [complaintId, staffId]
                String[] data = (String[]) request.getPayload();

                // TODO: complaintDAO.assignStaff(data[0], data[1]);
                logger.info("Staff {} assigned to complaintId: {}", data[1], data[0]);

                response.setStatus("SUCCESS");
                response.setMessage("Staff assigned successfully.");

            // ----- DOCTOR: view assigned patients -----
            } else if (type == RequestType.GET_ASSIGNED_PATIENTS) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("DOCTOR")) return accessDenied(response);

                // TODO: return patientDAO.getAssignedToDoctor(currentUserId);
                response.setStatus("SUCCESS");
                response.setMessage("Assigned patients loaded.");
                response.setPayload(null); // replace null with DAO result

            // ----- DOCTOR: add diagnosis/treatment notes -----
            } else if (type == RequestType.ADD_DIAGNOSIS) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("DOCTOR")) return accessDenied(response);

                // payload is a String array: [patientId, diagnosis, treatmentNotes]
                String[] data = (String[]) request.getPayload();

                // TODO: medicalRecordDAO.addDiagnosis(data[0], data[1], data[2], currentUserId);
                logger.info("Doctor {} added diagnosis for patientId: {}",
                        currentUserId, data[0]);

                response.setStatus("SUCCESS");
                response.setMessage("Diagnosis saved.");

            // ----- DOCTOR: schedule follow-up -----
            } else if (type == RequestType.SCHEDULE_FOLLOWUP) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("DOCTOR")) return accessDenied(response);

                // payload is a String array: [patientId, followUpDate]
                String[] data = (String[]) request.getPayload();

                // TODO: appointmentDAO.scheduleFollowUp(data[0], data[1], currentUserId);
                response.setStatus("SUCCESS");
                response.setMessage("Follow-up scheduled.");

            // ----- NURSE: view assigned cases -----
            } else if (type == RequestType.GET_ASSIGNED_CASES) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("NURSE")) return accessDenied(response);

                // TODO: return caseDAO.getAssignedToNurse(currentUserId);
                response.setStatus("SUCCESS");
                response.setMessage("Assigned cases loaded.");
                response.setPayload(null); // replace null with DAO result

            // ----- NURSE: record vitals -----
            } else if (type == RequestType.RECORD_VITALS) {

                if (!isLoggedIn()) return notLoggedIn(response);
                if (!currentRole.equals("NURSE")) return accessDenied(response);

                // payload is a String array: [patientId, vitals, observations, notes]
                String[] data = (String[]) request.getPayload();

                // TODO: nursingDAO.recordVitals(data[0], data[1], data[2], data[3]);
                logger.info("Nurse {} recorded vitals for patientId: {}",
                        currentUserId, data[0]);

                response.setStatus("SUCCESS");
                response.setMessage("Vitals recorded.");

            // ----- LIVE CHAT -----
            } else if (type == RequestType.SEND_CHAT_MESSAGE) {

                if (!isLoggedIn()) return notLoggedIn(response);

                // check hospital operating hours: 8:00 AM - 7:00 PM
                LocalTime now = LocalTime.now();
                LocalTime open = LocalTime.of(8, 0);
                LocalTime close = LocalTime.of(19, 0);

                if (now.isBefore(open) || now.isAfter(close)) {
                    response.setStatus("ERROR");
                    response.setMessage("Chat is only available between 8:00 AM and 7:00 PM.");
                    return response;
                }

                // payload is a String array: [recipientId, messageText, senderName]
                String[] data = (String[]) request.getPayload();
                int recipientId = Integer.parseInt(data[0]);
                String messageText = data[1];
                String senderName = data[2];

                // build the push notification to send to the recipient
                Response chatPush = new Response();
                chatPush.setStatus("CHAT_MESSAGE_PUSH");
                chatPush.setMessage(senderName + ": " + messageText);
                chatPush.setPayload(new String[]{
                    String.valueOf(currentUserId), // senderId
                    senderName,
                    messageText
                });

                // push directly to the recipient's stream
                server.sendChatMessage(recipientId, chatPush);

                logger.info("Chat message from userId: {} to userId: {}",
                        currentUserId, recipientId);

                response.setStatus("SUCCESS");
                response.setMessage("Message sent.");

            } else {
                response.setStatus("ERROR");
                response.setMessage("Unknown request type.");
            }

        } catch (Exception e) {
            logger.error("Error handling request: {}", e.getMessage(), e);
            response.setStatus("ERROR");
            response.setMessage("Server error: " + e.getMessage());
        }

        return response;
    }

    // helpers to keep the code above clean
    private boolean isLoggedIn() {
        return currentUserId != -1 && currentRole != null;
    }

    private Response notLoggedIn(Response response) {
        response.setStatus("ERROR");
        response.setMessage("Not logged in. Please login first.");
        return response;
    }

    private Response accessDenied(Response response) {
        response.setStatus("ERROR");
        response.setMessage("Access denied. You do not have permission for this action.");
        return response;
    }

    private void closeQuietly() {
        try { if (inputStream != null) inputStream.close(); } catch (Exception e) {}
        try { if (outputStream != null) outputStream.close(); } catch (Exception e) {}
        try { if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close(); } catch (Exception e) {}
    }
}