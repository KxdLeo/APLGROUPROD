package com.careplus.network;

import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;

import com.careplus.dto.Response;

public class ClientListener implements Runnable {

    public interface NotificationListener {
        void onNotification(Response response);
    }

    private ObjectInputStream inputStream;
    private BlockingQueue<Response> responseQueue;
    private boolean running;
    private NotificationListener notificationListener;

    public ClientListener(ObjectInputStream inputStream,
            BlockingQueue<Response> responseQueue) {
        this.inputStream = inputStream;
        this.responseQueue = responseQueue;
        this.running = true;
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Object obj = inputStream.readObject();

                if (obj instanceof Response) {
                    Response response = (Response) obj;

                    // if it's a chat push, send to the notification listener
                    // otherwise it's a normal request response, put it in the queue
                    if ("CHAT_MESSAGE_PUSH".equalsIgnoreCase(response.getStatus())
                            && notificationListener != null) {
                        notificationListener.onNotification(response);
                    } else {
                        responseQueue.put(response);
                    }
                }
            }
        } catch (Exception e) {
            if (running) {
                System.out.println("Client listener stopped: " + e.getMessage());
            }
        }
    }

    public void stopListener() {
        running = false;
        try { if (inputStream != null) inputStream.close(); } catch (Exception e) {}
    }
}