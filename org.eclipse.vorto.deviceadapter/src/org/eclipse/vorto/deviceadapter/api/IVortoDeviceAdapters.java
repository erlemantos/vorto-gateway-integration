package org.eclipse.vorto.deviceadapter.api;

public interface IVortoDeviceAdapters {
  
  IDeviceAdapter getAdapter(String mappingSpec);
  
}
