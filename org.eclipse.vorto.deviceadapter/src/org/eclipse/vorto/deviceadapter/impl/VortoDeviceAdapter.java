package org.eclipse.vorto.deviceadapter.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.vorto.deviceadapter.api.IDataCallback;
import org.eclipse.vorto.deviceadapter.api.IDeviceAdapter;
import org.eclipse.vorto.deviceadapter.api.IDeviceDiscovery;
import org.eclipse.vorto.deviceadapter.api.IDeviceDiscoveryCallback;
import org.eclipse.vorto.deviceadapter.api.INewConfiguration;
import org.eclipse.vorto.deviceadapter.impl.configuration.EnableConfigurationAction;
import org.eclipse.vorto.deviceadapter.impl.configuration.EnableConfigurationCreator;
import org.eclipse.vorto.mapping.engine.MappingEngine;
import org.eclipse.vorto.mapping.engine.model.spec.IMappingSpecification;
import org.eclipse.vorto.model.Infomodel;
import org.eclipse.vorto.model.runtime.FunctionblockValue;
import org.eclipse.vorto.model.runtime.InfomodelValue;
import org.eclipse.vorto.model.runtime.PropertyValue;
import org.osgi.framework.BundleContext;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.prosyst.mbs.services.btle.BluetoothLEController;
import com.prosyst.mbs.services.btle.BluetoothLEDevice;

public class VortoDeviceAdapter implements IDeviceAdapter {
  
  public static final String ATTRIBUTE_ENABLE = "enable";

  public static final String ATTRIBUTE_VALUE = "value";

  public static final String ATTRIBUTE_UUID = "uuid";

  private static final String ATTRIBUTE_MODEL_NUMBER_STRING = "modelNumberString";

  private static final String STEREOTYPE_DEVICE_PROFILE = "DeviceProfile";

  private VortoDeviceDiscovery deviceDiscovery;
  
  private IMappingSpecification mappingSpecs;
  
  private String targetPlatform;
  
  private LoadingCache<String, VortoDeviceData> vortoDeviceCache;
  
  private Collection<IVortoConfigurationCreator> configCreators = new ArrayList<>();
  
  public VortoDeviceAdapter(
      BundleContext bundleContext, 
      BluetoothLEController bluetoothLeController,
      IMappingSpecification mappingSpecs,
      MappingEngine mappingEngine) {
    
    this.targetPlatform = mappingSpecs.getInfoModel().getTargetPlatformKey();
    this.mappingSpecs = Objects.requireNonNull(mappingSpecs);
    
    List<IVortoConfigurationAction> configActions = new ArrayList<>();
    configActions.add(new EnableConfigurationAction());
    
    configCreators.add(new EnableConfigurationCreator());
    
    Map<String, INewConfiguration> infomodelPropConfig = getConfigurations(mappingSpecs, configCreators);
    
    vortoDeviceCache = CacheBuilder.newBuilder().build(cacheLoader(mappingEngine, configActions, infomodelPropConfig));
    
    this.deviceDiscovery = new VortoDeviceDiscovery(bundleContext, bluetoothLeController, 
        mappingSpecs.getInfoModel(), getModelNumber(mappingSpecs));
  }

  @Override
  public void listAvailableDevicesAsync(int scantimeInMs, IDeviceDiscoveryCallback discoveryCallbackHandler) {
    deviceDiscovery.start();
    deviceDiscovery.listAvailableDevicesAsync(scantimeInMs, discoveryCallbackHandler);
    Executors.newScheduledThreadPool(1).schedule(() -> deviceDiscovery.stop(), scantimeInMs, TimeUnit.MILLISECONDS);
  }

  @Override
  public INewConfiguration newConfiguration(String infomodelProperty) {
    if (Objects.toString(infomodelProperty, "").trim().isEmpty()) {
      return null;
    }
    return getConfigurations(mappingSpecs, configCreators).get(infomodelProperty);
  }
  
  private Map<String, INewConfiguration> getConfigurations(IMappingSpecification mappingSpecs,
      Collection<IVortoConfigurationCreator> configCreators) {
    Map<String, INewConfiguration> configurations = new HashMap<>();
    
    if (mappingSpecs == null || configCreators == null || configCreators.isEmpty()) {
      return configurations;
    }
    
    mappingSpecs.getInfoModel().getFunctionblocks().forEach(modelProperty -> {
      configCreators.forEach(configCreator -> {
        Collection<PropertyValue> configValues = 
            configCreator.getConfigurations(mappingSpecs.getFunctionBlock(modelProperty.getName()));
        if (!configValues.isEmpty()) {
          if (configurations.containsKey(modelProperty.getName())) {
            configurations.get(modelProperty.getName()).getConfigurationValues().addAll(configValues);
          } else {
            INewConfiguration config = new GattPropertyConfiguration(modelProperty.getName());
            config.getConfigurationValues().addAll(configValues);
            configurations.put(modelProperty.getName(), config);
          }
        }
      });
    });
    
    return configurations;
  }
  
  @Override
  public void setConfiguration(INewConfiguration configuration, String deviceId) {
    try {
      vortoDeviceCache.get(deviceId).setConfiguration(configuration);
    } catch (ExecutionException e) {
      printMsg("Problem setting configuration of device[" + deviceId + "]:" + e.getMessage());
    }
  }

  @Override
  public FunctionblockValue receive(String infomodelProperty, String deviceId) {
    try {
      return vortoDeviceCache.get(deviceId).receive(infomodelProperty);
    } catch (ExecutionException e) {
      printMsg("Problem receiving from device [" + deviceId + "]:" + e.getMessage());
      return null;
    }
  }
  
  @Override
  public InfomodelValue receive(String deviceId) {
    try {
      return vortoDeviceCache.get(deviceId).receive();
    } catch (ExecutionException e) {
      printMsg("Problem receiving from device [" + deviceId + "]:" + e.getMessage());
      return null;
    }
  }

  @Override
  public void receiveAsync(String infomodelProperty, String deviceId, IDataCallback dataCallback) {
    // TODO : are we supposed to be in a loop here and call dataCallback everytime we get data?
    // throw not supported since Prosyst doesn't support notifications from devices?
  }
  
  private String getModelNumber(IMappingSpecification mappingSpecs) {    
    return mappingSpecs.getInfoModel().getStereotype(STEREOTYPE_DEVICE_PROFILE)
        .map(stereotype -> stereotype.getAttributes().get(ATTRIBUTE_MODEL_NUMBER_STRING))
        .orElseThrow(() -> {
          throw new IDeviceAdapter.DeviceAdapterProblem(
              "No 'DeviceProfile' stereotype with 'modelNumberString' attribute in mapping file.");
        });
  }
  
  private CacheLoader<String, VortoDeviceData> cacheLoader(MappingEngine mappingEngine,
      List<IVortoConfigurationAction> configActions, Map<String, INewConfiguration> infomodelPropConfig) {
    return new CacheLoader<String, VortoDeviceData>() {
      public VortoDeviceData load(String deviceId) throws Exception {
        if (!Objects.toString(deviceId, "").trim().isEmpty()) {
          Optional<BluetoothLEDevice> maybeDevice = deviceDiscovery.getDevice(deviceId);
          if (maybeDevice.isPresent()) {
            return VortoDeviceData.getDeviceData(mappingEngine, maybeDevice.get(), 
                configActions, infomodelPropConfig);
          }
        }
        
        throw new IDeviceDiscovery.DeviceDiscoveryProblem("Device with deviceId '" + deviceId + " cannot be found.");
      }          
    };
  }

  @Override
  public String getTargetPlatform() {
    return targetPlatform;
  }

  @Override
  public Infomodel getInfomodel() {
    return mappingSpecs.getInfoModel();
  }
  
  private void printMsg(String string) {
    System.out.println("[DEMO] " + string);
  }
}
