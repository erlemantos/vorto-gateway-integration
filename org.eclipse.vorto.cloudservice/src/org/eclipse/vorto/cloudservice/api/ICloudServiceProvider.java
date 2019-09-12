package org.eclipse.vorto.cloudservice.api;

public interface ICloudServiceProvider {
  
  ICloudService getCloudService(ICloudServiceConfig config);
 
  ICloudServiceConfigBuilder getBuilder(String config);
  
}
