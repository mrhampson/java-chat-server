/*
 * SimpleLogger.java
 * Created on Aug 16, 2018, 6:10 PM
 *
 * Copyright 2008-2018 LiveAction, Incorporated. All Rights Reserved.
 * 3500 W Bayshore Road, Palo Alto, California 94303, U.S.A.
 *
 * This software is the confidential and proprietary information
 * of LiveAction ("Confidential Information").
 * You shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with LiveAction.
 */
package com.mrhampson.javachat;

import net.jcip.annotations.ThreadSafe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Marshall Hampson
 */
@ThreadSafe
public class SimpleLogger implements AutoCloseable {
  private static final int DEFAULT_BUFFER_SIZE = 128;
  private final BufferedWriter bufferedWriter;
  private final Executor loggingExecutor = Executors.newSingleThreadExecutor();
  
  public SimpleLogger(String logFilePath) throws IOException {
    File logFile = new File(logFilePath);
    logFile.createNewFile();
    bufferedWriter = new BufferedWriter(
      new OutputStreamWriter(new FileOutputStream(logFile), StandardCharsets.US_ASCII),
      DEFAULT_BUFFER_SIZE
    );
  }
  
  public void log(String message) {
    loggingExecutor.execute(() -> {
      try {
        bufferedWriter.write(message);
        bufferedWriter.write("\n");
        bufferedWriter.flush();
      }
      catch (Exception ignored) {}
    });
  }
  
  @Override
  public void close() throws IOException {
    bufferedWriter.close();
  }
}
