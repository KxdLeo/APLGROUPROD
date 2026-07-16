package careplus.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import careplus.common.net.CarePlusRequest;
import careplus.common.net.CarePlusResponse;

public class CarePlusServer {
    private static final Logger logger = LogManager.getLogger(CarePlusServer.class);

    private final int port;
    private final HospitalService hospitalService;
    private final ExecutorService clientPool = Executors.newCachedThreadPool();

    public CarePlusServer(int port, HospitalService hospitalService) {
        this.port = port;
        this.hospitalService = hospitalService;
    }

    public void start() {
        logger.info("CarePlus server starting on port {}", port);
        System.out.println("CarePlus Hospital server listening on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                clientPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        handleClient(socket);
                    }
                });
            }
        } catch (Exception ex) {
            logger.fatal("CarePlus server stopped.", ex);
            System.out.println("CarePlus server stopped. Check logs/server.log.");
        }
    }

    private void handleClient(Socket socket) {
        try (Socket client = socket;
                ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream input = new ObjectInputStream(client.getInputStream())) {
            Object object = input.readObject();
            if (!(object instanceof CarePlusRequest)) {
                output.writeObject(CarePlusResponse.fail("Invalid request object."));
                return;
            }
            CarePlusRequest request = (CarePlusRequest) object;
            logger.info("Received client request: {}", request.getAction());
            output.writeObject(hospitalService.handle(request));
        } catch (Exception ex) {
            logger.error("Could not handle client request.", ex);
        }
    }
}
