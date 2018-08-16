import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Marshall Hampson
 */
public class ServerMain {
  private static final int DEFAULT_PORT = 1234;
  
  public static void main (String[] args) {
    OutboundSocketMessageDispatcher socketMessageDispatcher = new OutboundSocketMessageDispatcher();
    try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        socketMessageDispatcher.registerSocket(clientSocket);
        ClientHandlerThread clientHandlerThread = new ClientHandlerThread(clientSocket, socketMessageDispatcher);
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
    private final OutboundSocketMessageDispatcher socketMessageDispatcher;
    private String username;
    
    private ClientHandlerThread(
      Socket clientSocket,
      OutboundSocketMessageDispatcher socketMessageDispatcher) {
      Objects.requireNonNull(clientSocket);
      Objects.requireNonNull(socketMessageDispatcher);
      this.clientSocket = clientSocket;
      this.socketMessageDispatcher = socketMessageDispatcher;
      this.username = this.clientSocket.getInetAddress().toString();
    }
    
    @Override public void run() {
      System.out.println("New client thread started: (thread id: " + Thread.currentThread().getId() + ")");
      try (
        BufferedReader inputBufferReader = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.US_ASCII), CHAR_BUF_SIZE)
      ) {
        while (true) {
          try {
            String line = inputBufferReader.readLine();
            if (line != null) {
              System.out.println(line);
              if (line.startsWith("s ") || line.startsWith("SEND ")) {
                String message = line.substring(5);
                socketMessageDispatcher.dispatchMessage(username, message);
              }
              else if (line.startsWith("NICK ")) {
                username = line.substring(5);
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
      }
    }
  }
}
