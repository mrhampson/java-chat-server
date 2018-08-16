import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import java.nio.CharBuffer;
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
    
    ClientHandlerThread(Socket clientSocket) {
      Objects.requireNonNull(clientSocket);
      this.clientSocket = clientSocket;
    }
    
    @Override public void run() {
      System.out.println("New client thread started: (thread id: " + Thread.currentThread().getId() + ")");
      try (
        InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.US_ASCII);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.US_ASCII)
      ) {
        final CharBuffer charBuffer = CharBuffer.allocate(CHAR_BUF_SIZE);
        while (true) {
          if (inputStreamReader.ready()) {
            int readCharCount = inputStreamReader.read(charBuffer);
            if (readCharCount != -1) {
              outputStreamWriter.write(charBuffer.array(), 0, readCharCount);
              printBuffer(charBuffer.array(), readCharCount);
              if (doesFirstWordMatch("BYE", charBuffer.array())) {
                break;
              }
            }
          }
        }
        clientSocket.close();
      }
      catch (IOException e) {
        System.out.println(e);
      }
    }
  }
  
  private static boolean doesFirstWordMatch(String toMatch, char[] chars) {
    Objects.requireNonNull(toMatch);
    for (int i = 0; i < toMatch.length(); i++) {
      if (i > chars.length - 1) {
        return false;
      }
      if (chars[i] != toMatch.charAt(i)) {
        return false;
      }
    }
    return true;
  }
  
  private static void printBuffer(char[] buffer, int length) {
    for (int i = 0; i < length; i++) {
      System.out.print(buffer[i]);
    }
  }
}
