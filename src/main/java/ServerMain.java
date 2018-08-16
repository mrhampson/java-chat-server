import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    try (ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT)) {
      while (true) {
        Socket clientSocket = serverSocket.accept();
        ClientHandlerThread clientHandlerThread = new ClientHandlerThread(clientSocket);
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
    
    private ClientHandlerThread(Socket clientSocket) {
      Objects.requireNonNull(clientSocket);
      this.clientSocket = clientSocket;
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
          // Handle input
          if (inputBufferReader.ready()) {
            String line = inputBufferReader.readLine();
            if (line != null) {
              System.out.println(line);
              if (line.startsWith("BYE")) {
                break;
              }
            }
          }
        }
        clientSocket.close();
      }
      catch (IOException e) {
        System.out.println(e.toString());
      }
    }
  }
}
