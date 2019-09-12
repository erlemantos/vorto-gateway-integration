package org.eclipse.vorto.cloudservice.hono;

import org.eclipse.vorto.cloudservice.api.ICloudServiceConfig;

public class HonoCloudServiceConfig implements ICloudServiceConfig {
  private String mqttHostUrl;
  private String honoTenant;
  private String password;

  public String getMqttHostUrl() {
    return mqttHostUrl;
  }

  public void setMqttHostUrl(String mqttHostUrl) {
    this.mqttHostUrl = mqttHostUrl;
  }

  public String getHonoTenant() {
    return honoTenant;
  }

  public void setHonoTenant(String honoTenant) {
    this.honoTenant = honoTenant;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
