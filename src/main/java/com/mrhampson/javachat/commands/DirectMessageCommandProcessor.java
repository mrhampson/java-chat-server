/*
 * DirectMessageCommandProcessor.java
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

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * @author Marshall Hampson
 */
public class DirectMessageCommandProcessor implements CommandProcessor {

  private final OutboundSocketMessageDispatcher socketMessageDispatcher;
  private final UsernameManager usernameManager;

  public DirectMessageCommandProcessor(
    OutboundSocketMessageDispatcher outboundSocketMessageDispatcher,
    UsernameManager usernameManager
  ) {
    this.socketMessageDispatcher = outboundSocketMessageDispatcher;
    this.usernameManager = usernameManager;
  }

  @Override public void processLine(String usernameContext, String line) {
    String[] words = line.split("\\s+");
    String destUsername = words[1];
    String message = "DM: " + String.join(" ", Arrays.copyOfRange(words, 2, words.length));
    InetAddress destAddress = usernameManager.getAddressForUser(destUsername);
    if (destAddress != null) {
      socketMessageDispatcher.dispatchMessageToAddress(destAddress, usernameContext, message);
    }
  }

  @Override public Set<String> getKeywords() {
    return Collections.singleton("DM");
  }
}
