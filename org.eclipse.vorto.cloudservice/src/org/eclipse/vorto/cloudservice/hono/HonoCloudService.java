package org.eclipse.vorto.cloudservice.hono;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.vorto.cloudservice.api.ICloudService;
import org.eclipse.vorto.mapping.targetplatform.ditto.TwinPayloadFactory;
import org.eclipse.vorto.model.runtime.FunctionblockValue;
import org.eclipse.vorto.model.runtime.InfomodelValue;

import com.google.gson.Gson;

public class HonoCloudService implements ICloudService {
  
  private static final String TELEMETRY = "telemetry";
  
  private HonoCloudServiceConfig config;
  private Map<String, HonoMqttClient> deviceClients = new HashMap<String, HonoMqttClient>();
  private Gson gson = new Gson();
  
  public HonoCloudService(HonoCloudServiceConfig config) {
    this.config = Objects.requireNonNull(config);
  }

  @Override
  public void publish(String deviceInfo, InfomodelValue infomodelValue) {
    infomodelValue.getProperties().forEach((key, fb) -> publish(deviceInfo, key, fb));
  }

  public void publish(String deviceInfo, String featureId, FunctionblockValue fbValue) {
    String payload = gson.toJson(TwinPayloadFactory.toDittoProtocol(fbValue, featureId, deviceInfo));
    getConnectedHonoClient(deviceInfo).send(TELEMETRY, payload);
  }
  
  private HonoMqttClient getConnectedHonoClient(String thingId) {
    HonoMqttClient client = deviceClients.get(thingId);
    if (client == null) {
      String user = thingId.replaceAll(":", "_") + "@" + config.getHonoTenant();
      client = new HonoMqttClient(config.getMqttHostUrl(), thingId, user, config.getPassword());
      deviceClients.put(thingId, client);
    }

    if (!client.isConnected()) {
      client.connect();
    }

    return client;
  }
}
