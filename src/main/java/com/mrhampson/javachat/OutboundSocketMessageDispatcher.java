package com.mrhampson.javachat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
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
  
  private final Set<Socket> sockets = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Executor outboundMessageExecutor = Executors.newSingleThreadExecutor();
  
  public void registerSocket(Socket socket) {
    sockets.add(socket);
  }
  
  public void removeSocket(Socket socket) {
    sockets.remove(socket);
  }
  
  public void dispatchMessage(String username, String message) {
    Objects.requireNonNull(username);
    Objects.requireNonNull(message);
    outboundMessageExecutor.execute(() -> dispatchMessageInternal(username, message));
  }

  private void dispatchMessageInternal(String username, String message) {
    String fullMessage = ALARM_CHAR + DATE_TIME_FORMATTER.format(LocalDateTime.now()) + 
      " (" + username + "): " + message + "\n";
    for (Socket socket : sockets) {
      try {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(fullMessage.getBytes(StandardCharsets.US_ASCII));
      }
      catch (IOException ignored)  {}
    }
  }
}
