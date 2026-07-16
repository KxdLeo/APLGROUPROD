package com.careplus.network;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger; // sort out later /possibly a group members task 

import com.careplus.dto.Response;

public class Server {
	
	private static final Logger logger = LogManager.getLogger(Server.class);
	
	private ServerSocket serverSocket;
	private ExecutorService threadPool;
	private boolean running;
	
	//map userId to oupput stream so we can push chat message
	private final Map<Integer,ObjectOutputStream> connectedClients=
			new ConcurrentHashMap<>();// unsure
	
	public Server() {
		try {
			serverSocket = new ServerSocket(55000);
			threadPool = Executors.newFixedThreadPool(10);
			running = true;
			logger.info("CarePlus server port 55000");
			
		} catch (Exception e) {
			logger.error("Server was not created.port may already be in use ");
			return;
		}
	}
	
	public void startServer() {
		if (serverSocket == null) {
			logger.error("Server socket was not made");
			return;
		}
	
		try {
			while(running) {
				Socket clientSocket = serverSocket.accept();
				logger.info("Client connected:{}", clientSocket.getInetAddress());
				
				ClientHandler handler = new ClientHandler(clientSocket, this);
				threadPool.submit(handler);
			}
		}catch(Exception e) {
				if(running) {
					logger.error("Server accept error.",e);
				}
			} finally {
				stopServer();
			}
		}
		
	//called after login so we know which user Id owns this stream 
	public void registerClient(int userId,ObjectOutputStream outputStream) {
		connectedClients.put(userId, outputStream);
		logger.info("Client registered. UserId :{}| Total connected:{}",userId,connectedClients.size());
	}
	
	//called when client disconnects
	public void unregisterClient(ObjectOutputStream outputStream) {
		connectedClients.values().remove(outputStream);
		logger.info("Client unregistered.Total connected: {}",connectedClients.size());
	}
	
	
	// pushes a chat message directly to a specific user
	public void sendChatMessage(int recipentId, Response chatResponse) {
		 ObjectOutputStream client = connectedClients.get(Integer.valueOf(recipentId));

	        if (client != null) {
	            try {
	                client.writeObject(chatResponse);
	                client.flush();
	                logger.info("Chat message sent to userId: {}", recipentId);
	            } catch (Exception e) {
	                logger.warn("Failed to send chat message to userId: {}", recipentId);
	            }
	        } else {
	            logger.info("UserId: {} is not currently connected.", recipentId);
	        }
	}

	public void stopServer() {
        running = false;
        try {
            if (threadPool != null && !threadPool.isShutdown()) {
                threadPool.shutdown();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            logger.info("Server stopped.");
        } catch (Exception e) {
            logger.error("Error stopping server.", e);
        }
    }

}
