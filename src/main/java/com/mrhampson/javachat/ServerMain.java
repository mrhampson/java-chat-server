package com.mrhampson.javachat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Marshall Hampson
 */
public class ServerMain {
  private static final int DEFAULT_PORT = 1234;
  private static final String LOG_FILE_PATH = "chat-" + 
    LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".log";
  
  public static void main (String[] args) {
    final OutboundSocketMessageDispatcher socketMessageDispatcher = new OutboundSocketMessageDispatcher();
    final UsernameManager usernameManager = new UsernameManager();
    
    try (
      final ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
      final SimpleLogger logger = new SimpleLogger(LOG_FILE_PATH)
    ) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        socketMessageDispatcher.registerSocket(clientSocket);
        ClientHandlerThread clientHandlerThread = new ClientHandlerThread(clientSocket, socketMessageDispatcher, logger,
          usernameManager);
        clientHandlerThread.start();
      }
    }
    catch (IOException e) {
      Logger.getGlobal().warning(e.toString());
    }
  }
  
  private static final class ClientHandlerThread extends Thread {
    private static final int DEFAULT_BUFFER_SIZE = 128;
    
    private final Socket clientSocket;
    private final OutboundSocketMessageDispatcher socketMessageDispatcher;
    private final SimpleLogger logger;
    private final UsernameManager usernameManager;
    private String username;
    
    private ClientHandlerThread(
      Socket clientSocket,
      OutboundSocketMessageDispatcher socketMessageDispatcher,
      SimpleLogger sharedLogger,
      UsernameManager usernameManager) {
      Objects.requireNonNull(clientSocket);
      Objects.requireNonNull(socketMessageDispatcher);
      Objects.requireNonNull(sharedLogger);
      Objects.requireNonNull(usernameManager);
      this.clientSocket = clientSocket;
      this.socketMessageDispatcher = socketMessageDispatcher;
      this.username = this.clientSocket.getInetAddress().toString();
      this.logger = sharedLogger;
      this.usernameManager = usernameManager;
    }
    
    @Override public void run() {
      logger.log("New client thread started: (thread id: " + Thread.currentThread().getId() + ", client addr: " + 
        clientSocket.getInetAddress() + ")");
      try (
        BufferedReader inputBufferReader = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.US_ASCII), DEFAULT_BUFFER_SIZE)
      ) {
        while (true) {
          try {
            String line = inputBufferReader.readLine();
            if (line != null) {
              logger.log(line);
              if (line.startsWith("s ") || line.startsWith("SEND ")) {
                String message = line.substring(line.indexOf(' ') + 1);
                socketMessageDispatcher.dispatchMessage(username, message);
              }
              else if (line.startsWith("NICK ")) {
                String proposedUsername = line.substring(line.indexOf(' ') + 1);
                if (usernameManager.swapName(username, proposedUsername)) {
                  String formerName = username;
                  username = proposedUsername;
                  socketMessageDispatcher.dispatchMessage(username, formerName + "is now known as " + username);
                }
                else {
                  socketMessageDispatcher.dispatchMessage(username, "Username: " + proposedUsername + " taken by another user!");
                }
              }
              else if (line.startsWith("BYE")) {
                break;
              }
            }
          }
          catch (IOException e) {
            e.printStackTrace();
            break;
          }
        }
        clientSocket.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
      finally {
        socketMessageDispatcher.removeSocket(clientSocket);
        logger.log("Ended client thread: (thread id: " + Thread.currentThread().getId() + ", client addr: " +
          clientSocket.getInetAddress() + ")");     
      }
    }
  }
}
