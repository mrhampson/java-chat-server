import javax.naming.ldap.SortKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Marshall Hampson
 */
public class ServerMain {
  private static final int DEFAULT_PORT = 1234;
  
  public static void main (String[] args) {
    List<Socket> allClientSockets = new CopyOnWriteArrayList<>();
    try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        allClientSockets.add(clientSocket);
        ClientHandlerThread clientHandlerThread = 
          new ClientHandlerThread(clientSocket, allClientSockets);
        clientHandlerThread.start();
      }
    }
    catch (IOException e) {
      System.out.println(e.toString());
    }
  }
  
  private static final class ClientHandlerThread extends Thread {
    private static final int CHAR_BUF_SIZE = 256;
    private final Socket clientSocket;
    private final List<Socket> allSockets;
    
    private ClientHandlerThread(
      Socket clientSocket,
      List<Socket> allSockets) {
      Objects.requireNonNull(clientSocket);
      this.clientSocket = clientSocket;
      this.allSockets = allSockets;
    }
    
    @Override public void run() {
      System.out.println("New client thread started: (thread id: " + Thread.currentThread().getId() + ")");
      try (
        BufferedReader inputBufferReader = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.US_ASCII), CHAR_BUF_SIZE);
        BufferedWriter outputBufferWriter = new BufferedWriter(
          new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.US_ASCII), CHAR_BUF_SIZE)
      ) {
        while (true) {
          try {
            if (inputBufferReader.ready()) {
              String line = inputBufferReader.readLine();
              if (line != null) {
                System.out.println(line);
                if (line.startsWith("BYE")) {
                  break;
                }
                else if (line.startsWith("SEND")) {
                  String message = line.substring(4);
                  for (Socket socket : allSockets) {
                    try {
                      socket.getOutputStream().write(message.getBytes(StandardCharsets.US_ASCII));
                    }
                    catch (IOException ignored)  {}
                  }
                }
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
        allSockets.remove(clientSocket);
      }
    }
  }
}
