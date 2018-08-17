package com.mrhampson.javachat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A class which tracks all the available sockets and provides a way to dispatch messages to all of them
 * @author Marshall Hampson
 */
public class OutboundSocketMessageDispatcher {
  private static final char ALARM_CHAR = (char)7;
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
  
  private final Map<InetAddress, Socket> addressToSocketMap = new ConcurrentHashMap<>();
  private final Executor outboundMessageExecutor = Executors.newSingleThreadExecutor();
  
  public void registerSocket(Socket socket) {
    Objects.requireNonNull(socket);
    addressToSocketMap.put(socket.getInetAddress(), socket);
  }
  
  public void removeSocket(Socket socket) {
    Objects.requireNonNull(socket);
    addressToSocketMap.remove(socket.getInetAddress());
  }
  
  public void dispatchMessageToAddress(InetAddress destAddress, String sendingUsername, String message){
    String formattedMessage = formatMessage(sendingUsername, message);
    Socket destSocket = addressToSocketMap.get(destAddress);
    writeMessageOnSocket(destSocket, formattedMessage);
  }
  
  public void dispatchMessageToAll(String sendingUsername, String message) {
    Objects.requireNonNull(sendingUsername);
    Objects.requireNonNull(message);
    outboundMessageExecutor.execute(() -> dispatchMessageInternal(sendingUsername, message));
  }

  private void dispatchMessageInternal(String username, String message) {
    String fullMessage = formatMessage(username, message);
    for (Socket socket : addressToSocketMap.values()) {
      writeMessageOnSocket(socket, fullMessage);
    }
  }
  
  private String formatMessage(String username, String message) {
    return ALARM_CHAR + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " (" + username + "): " + message + "\n";
  }
  
  private void writeMessageOnSocket(Socket socket, String message) {
    try {
      OutputStream outputStream = socket.getOutputStream();
      outputStream.write(message.getBytes(StandardCharsets.US_ASCII));
    }
    catch (IOException ignored)  {}
  }
}
