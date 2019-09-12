package org.eclipse.vorto.cloudservice.api;

import org.eclipse.vorto.model.runtime.FunctionblockValue;
import org.eclipse.vorto.model.runtime.InfomodelValue;

public interface ICloudService {
  
  void publish(String deviceInfo, InfomodelValue infomodelValue);
  
  void publish(String deviceInfo, String featureId, FunctionblockValue fbValue);
  
}
