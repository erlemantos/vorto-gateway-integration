package org.eclipse.vorto.cloudservice.hono;

import org.eclipse.vorto.cloudservice.api.ICloudService;
import org.eclipse.vorto.cloudservice.api.ICloudServiceConfig;
import org.eclipse.vorto.cloudservice.api.ICloudServiceConfigBuilder;
import org.eclipse.vorto.cloudservice.api.ICloudServiceProvider;

public class HonoCloudServiceProvider implements ICloudServiceProvider {

  @Override
  public ICloudService getCloudService(ICloudServiceConfig config) {
    return new HonoCloudService((HonoCloudServiceConfig) config);
  }

  @Override
  public ICloudServiceConfigBuilder getBuilder(String config) {
    return new HonoCloudServiceConfigBuilder(config);
  }

}
