package careplus.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import careplus.common.net.CarePlusRequest;
import careplus.common.net.CarePlusResponse;

public class CarePlusClient {
    private static final Logger logger = LogManager.getLogger(CarePlusClient.class);

    private final String host;
    private final int port;

    public CarePlusClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public CarePlusResponse send(CarePlusRequest request) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= 20; attempt++) {
            try (Socket socket = new Socket(host, port);
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {
                output.writeObject(request);
                output.flush();
                return (CarePlusResponse) input.readObject();
            } catch (Exception ex) {
                lastException = ex;
                sleepBeforeRetry();
            }
        }
        logger.error("Could not send request {} to server.", request.getAction(), lastException);
        return CarePlusResponse.fail("Server is not running on port " + port
                + ". Start CarePlus Server, wait for 'listening on port 8888', then try again.");
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
