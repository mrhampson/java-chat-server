/*
 * CommandProcessorManager.java
 * Created on Aug 16, 2018, 8:49 PM
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
package com.mrhampson.javachat.commands;

import com.mrhampson.javachat.OutboundSocketMessageDispatcher;
import com.mrhampson.javachat.UsernameManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marshall Hampson
 */
public class CommandProcessorManager {
  private static final Pattern FIRST_WORD_PATTERN = Pattern.compile("^(\\w*)\\s+.*$");

  // Dependencies
  private final OutboundSocketMessageDispatcher outboundSocketMessageDispatcher;
  private final UsernameManager usernameManager;
  
  private final Map<String, CommandProcessor> allProcessors;
  
  public CommandProcessorManager(
    OutboundSocketMessageDispatcher outboundSocketMessageDispatcher,
    UsernameManager usernameManager
  ) {
    Objects.requireNonNull(outboundSocketMessageDispatcher);
    Objects.requireNonNull(usernameManager);
    this.outboundSocketMessageDispatcher = outboundSocketMessageDispatcher;
    this.usernameManager = usernameManager;
    
    // Setup map
    DirectMessageCommandProcessor directMessageCommandProcessor = new DirectMessageCommandProcessor(
      outboundSocketMessageDispatcher,
      usernameManager
    );
    
    Map<String, CommandProcessor> allProcessorMutable = new HashMap<>();
    allProcessorMutable.put(directMessageCommandProcessor.getKeyword(), directMessageCommandProcessor);
    allProcessors = Collections.unmodifiableMap(allProcessorMutable);
  }
  
  public void processLine(String usernameContext, String line) {
    Matcher matcher = FIRST_WORD_PATTERN.matcher(line);
    if (matcher.matches()) {
      String firstWord = matcher.group(1);
      CommandProcessor commandProcessor = allProcessors.get(firstWord.trim());
      if (commandProcessor != null) {
        commandProcessor.processLine(usernameContext, line);
      }
    }
  }
}
