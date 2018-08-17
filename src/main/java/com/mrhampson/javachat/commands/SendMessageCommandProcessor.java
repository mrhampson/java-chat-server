/*
 * SendMessageCommandProcessor.java
 * Created on Aug 16, 2018, 9:19 PM
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marshall Hampson
 */
public class SendMessageCommandProcessor implements CommandProcessor {
  private static final Set<String> keywords;
  
  private final OutboundSocketMessageDispatcher socketMessageDispatcher;
  
  public SendMessageCommandProcessor(OutboundSocketMessageDispatcher socketMessageDispatcher) {
    this.socketMessageDispatcher = socketMessageDispatcher;
  }
  
  static {
    Set<String> mutableKeywords = new HashSet<>();
    mutableKeywords.add("s");
    mutableKeywords.add("SEND");
    keywords = Collections.unmodifiableSet(mutableKeywords);
  }
  
  @Override public void processLine(String usernameContext, String line) {
    String message = line.substring(line.indexOf(' ') + 1);
    socketMessageDispatcher.dispatchMessageToAll(usernameContext, message);
  }

  @Override public Set<String> getKeywords() {
    return keywords;
  }
}
