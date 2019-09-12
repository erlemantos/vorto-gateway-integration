package org.eclipse.vorto.cloudservice.hono;

import java.lang.reflect.Type;
import java.util.Map;

import org.eclipse.vorto.cloudservice.api.ICloudServiceConfig;
import org.eclipse.vorto.cloudservice.api.ICloudServiceConfigBuilder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HonoCloudServiceConfigBuilder implements ICloudServiceConfigBuilder {

  private static final String HOST_URL = "hostUrl";
  private static final String TENANT = "tenant";
  private static final String PASSWORD = "password";
  
  private HonoCloudServiceConfig config;
  
  HonoCloudServiceConfigBuilder(String config) {
    this.config = parse(config);
  }
  
  private HonoCloudServiceConfig parse(String config) {
    Map<String, String> configMap = getConfigMap(config);
    
    HonoCloudServiceConfig honoConfig = new HonoCloudServiceConfig();
    
    honoConfig.setHonoTenant(configMap.get(TENANT));
    honoConfig.setMqttHostUrl(configMap.get(HOST_URL));
    honoConfig.setPassword(configMap.get(PASSWORD));
    
    return honoConfig;
  }
  
  private Map<String, String> getConfigMap(String deviceInfo) {
    Type type = new TypeToken<Map<String, String>>(){}.getType();
    Map<String, String> deviceInfoMap = new Gson().fromJson(deviceInfo, type);
    return deviceInfoMap;
  }

  @Override
  public ICloudServiceConfig build() {
    return config;
  }

}
