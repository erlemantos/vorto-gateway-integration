/**
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.vorto.deviceadapter.api;

import org.eclipse.vorto.model.Infomodel;

/**
 * Device Adapter that abstracts the communication layer between the client application and the
 * device. The data returned or set is based on Vorto compliant data structures according to the
 * Vorto Information Model
 */
public interface IDeviceAdapter extends IDeviceDiscovery, IDeviceData {

  /**
   * Gets the target platform for which this adapter is configured
   * 
   * @return
   */
  String getTargetPlatform();

  /**
   * Gets the Information model for which this adapter is configured
   * 
   * @return
   */
  Infomodel getInfomodel();

  public static class DeviceAdapterProblem extends RuntimeException {
    private static final long serialVersionUID = 2225917858542181738L;

    public DeviceAdapterProblem(String message, Throwable cause) {
      super(message, cause);
    }

    public DeviceAdapterProblem(String message) {
      super(message);
    }
  }
}
