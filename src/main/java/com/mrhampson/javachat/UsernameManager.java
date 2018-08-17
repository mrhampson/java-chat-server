/*
 * UsernameManager.java
 * Created on Aug 16, 2018, 6:33 PM
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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Class manages the usernames and makes sure no one takes the same name
 * @author Marshall Hampson
 */
public class UsernameManager {
  private final Object allActiveUsersLock = new Object();
  private final Map<String, InetAddress> allActiveUsers = new HashMap<>();

  /**
   * Attempts to swap the username for a new one
   * @param existingName the existing name of this user
   * @param newName the new desired name of this user
   * @return true if this username was successfully claimed, false otherwise
   */
  public boolean swapName(InetAddress userAddress, String existingName, String newName) {
    Objects.requireNonNull(existingName);
    Objects.requireNonNull(newName);
    synchronized (allActiveUsersLock) {
      if (allActiveUsers.keySet().contains(newName)) {
        return false;
      }
      allActiveUsers.put(newName, userAddress);
    }
    return true;
  }
  
  public InetAddress getAddressForUser(String username) {
    synchronized (allActiveUsersLock) {
      return allActiveUsers.get(username);
    }
  }
}
