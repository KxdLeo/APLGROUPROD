package com.careplus.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.careplus.dto.Request;
import com.careplus.dto.Response;

public class ClientConnection {

    private static Socket socket;
    private static ObjectOutputStream oos;
    private static ObjectInputStream ois;
    private static ClientListener clientListener;
    private static Thread listenerThread;
    private static BlockingQueue<Response> responseQueue;
    private static String sessionToken = "";
    private static final Object LOCK = new Object();

    private ClientListener.NotificationListener notificationListener;

    public ClientConnection() {
        initializeConnection();
    }

    private void initializeConnection() {
        synchronized (LOCK) {
            try {
                if (isConnected()) return;

                socket = new Socket("127.0.0.1", 55000); // CarePlus port
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
                ois = new ObjectInputStream(socket.getInputStream());

                responseQueue = new LinkedBlockingQueue<>();
                clientListener = new ClientListener(ois, responseQueue);

                if (notificationListener != null) {
                    clientListener.setNotificationListener(notificationListener);
                }

                listenerThread = new Thread(clientListener, "CarePlus-Client-Listener");
                listenerThread.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setNotificationListener(ClientListener.NotificationListener listener) {
        this.notificationListener = listener;
        if (clientListener != null) {
            clientListener.setNotificationListener(listener);
        }
    }

    public Response sendRequest(Request request) {
        synchronized (LOCK) {
            try {
                if (!isConnected()) initializeConnection();
                if (!isConnected()) return null;

                if (request != null && sessionToken != null && !sessionToken.isEmpty()) {
                    request.setSessionToken(sessionToken);
                }

                responseQueue.clear();
                oos.writeObject(request);
                oos.flush();

                return responseQueue.take();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                closeConnection();
            }
            return null;
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed()
                && oos != null && ois != null && responseQueue != null;
    }

    public static String getSessionToken() { return sessionToken; }
    public static void clearSessionToken() { sessionToken = ""; }

    public void closeConnection() {
        synchronized (LOCK) {
            try { if (clientListener != null) clientListener.stopListener(); } catch (Exception e) {}
            try { if (oos != null) oos.close(); } catch (IOException e) {}
            try { if (ois != null) ois.close(); } catch (IOException e) {}
            try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException e) {}
            socket = null; oos = null; ois = null;
            clientListener = null; listenerThread = null;
            responseQueue = null; sessionToken = "";
        }
    }
}