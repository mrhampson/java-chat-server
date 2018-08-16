import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A class which tracks all the available sockets and provides a way to dispatch messages to all of them
 * @author Marshall Hampson
 */
public class OutboundSocketMessageDispatcher {
  
  private final Set<Socket> sockets = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private final Executor outboundMessageExecutor = Executors.newSingleThreadExecutor();
  
  public void registerSocket(Socket socket) {
    sockets.add(socket);
  }
  
  public void removeSocket(Socket socket) {
    sockets.remove(socket);
  }
  
  public void dispatchMessage(String message) {
    outboundMessageExecutor.execute(() -> dispatchMessageInternal(message));
  }

  private void dispatchMessageInternal(String message) {
    for (Socket socket : sockets) {
      try {
        socket.getOutputStream().write(message.getBytes(StandardCharsets.US_ASCII));
      }
      catch (IOException ignored)  {}
    }
  }
}
