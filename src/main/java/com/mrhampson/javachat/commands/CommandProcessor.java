/*
 * CommandProcessor.java
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

import java.util.Set;

/**
 * @author Marshall Hampson
 */
public interface CommandProcessor {
  void processLine(String usernameContext, String line);
  Set<String> getKeywords();
}
