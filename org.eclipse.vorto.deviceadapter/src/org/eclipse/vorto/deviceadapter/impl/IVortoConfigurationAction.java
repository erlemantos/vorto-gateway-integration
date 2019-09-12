package org.eclipse.vorto.deviceadapter.impl;

import org.eclipse.vorto.deviceadapter.api.INewConfiguration;

import com.prosyst.mbs.services.btle.BluetoothLEDevice;

public interface IVortoConfigurationAction {
  
  String actionName();
  
  boolean execute(BluetoothLEDevice device, INewConfiguration config);
  
}
