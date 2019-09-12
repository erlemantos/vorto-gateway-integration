package org.eclipse.vorto.deviceadapter.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.vorto.deviceadapter.api.IDeviceData;
import org.eclipse.vorto.deviceadapter.api.INewConfiguration;
import org.eclipse.vorto.mapping.engine.MappingEngine;
import org.eclipse.vorto.mapping.engine.model.blegatt.GattCharacteristic;
import org.eclipse.vorto.mapping.engine.model.blegatt.GattDevice;
import org.eclipse.vorto.mapping.engine.model.blegatt.GattService;
import org.eclipse.vorto.model.runtime.FunctionblockValue;
import org.eclipse.vorto.model.runtime.InfomodelValue;

import com.prosyst.mbs.services.btle.BluetoothLEDevice;
import com.prosyst.mbs.services.btle.GattException;

public class VortoDeviceData {

  private BluetoothLEDevice device;
  private MappingEngine mappingEngine;
  private List<IVortoConfigurationAction> configActions;
  Map<String, INewConfiguration> infomodelPropertiesConfig;

  public static VortoDeviceData getDeviceData(MappingEngine mappingEngine, 
      BluetoothLEDevice device, 
      List<IVortoConfigurationAction> configActions,
      Map<String, INewConfiguration> infomodelPropConfig) {
    return new VortoDeviceData(device, mappingEngine, configActions, infomodelPropConfig);
  }

  private VortoDeviceData(BluetoothLEDevice device, 
      MappingEngine mappingEngine, 
      List<IVortoConfigurationAction> configActions,
      Map<String, INewConfiguration> infomodelPropertiesConfig) {
    this.device = Objects.requireNonNull(device);
    this.mappingEngine = Objects.requireNonNull(mappingEngine);
    this.configActions = configActions;
    
    if (infomodelPropertiesConfig == null) {
      this.infomodelPropertiesConfig = new HashMap<>();
    } else {
      this.infomodelPropertiesConfig = infomodelPropertiesConfig;
    }
  }
  
  public void setConfiguration(INewConfiguration configuration) {
    infomodelPropertiesConfig.put(configuration.getInfomodelProperty(), configuration);
  }

  public FunctionblockValue receive(String infomodelProperty) {
    try {
      if (!device.isConnected()) {
        device.connectGatt();
      }
      
      INewConfiguration configuration = infomodelPropertiesConfig.get(infomodelProperty);
      
      if (configuration != null && configActions != null) {
        configActions.forEach(action -> {
          boolean actionSuccessful = action.execute(device, configuration);
          if (actionSuccessful) {
            printMsg("Action '" + action.actionName() + "' is successful for " + device.getAddress());
          }
        });
      }

      GattDevice gattDevice = convertToVortoGattDevice(device);

      InfomodelValue mappedObject = mappingEngine.mapSource(gattDevice);

      if (device.isConnected()) {
        device.disconnectGatt();
      }

      return mappedObject.get(infomodelProperty);
    } catch (Exception e) {
      throw new IDeviceData.DeviceConfigurationProblem("Problem accessing device", e);
    }
  }
  
  public InfomodelValue receive() {
    try {
      if (!device.isConnected()) {
        device.connectGatt();
      }
      
      if (infomodelPropertiesConfig != null && configActions != null) {
        for(INewConfiguration configuration : infomodelPropertiesConfig.values()) {
          configActions.forEach(action -> {
            boolean actionSuccessful = action.execute(device, configuration);
            if (actionSuccessful) {
              printMsg("Action '" + action.actionName() + "' is successful for " + device.getAddress());
            }
          });
        }
      }

      GattDevice gattDevice = convertToVortoGattDevice(device);

      InfomodelValue mappedObject = mappingEngine.mapSource(gattDevice);

      if (device.isConnected()) {
        device.disconnectGatt();
      }

      return mappedObject;
    } catch (Exception e) {
      throw new IDeviceData.DeviceConfigurationProblem("Problem accessing device", e);
    }
  }

  private final GattCharacteristic convertToVortoGattCharacteristic(
      com.prosyst.mbs.services.btle.GattCharacteristic gatt) {
    try {
      printMsg("Characteristic: '" + gatt.getUuid().toLowerCase() + "' AssignedName: '" + gatt.getAssignedName() + "' Value: " + Hex.encodeHexString(gatt.getValue()));
      GattCharacteristic gattCharacteristic = new GattCharacteristic(gatt.getUuid().toLowerCase(), gatt.getValue());
      return gattCharacteristic;
    } catch (GattException e) {
      return null;
    }
  }

  private final GattService convertToVortoGattService(com.prosyst.mbs.services.btle.GattService service) {
    GattService gattService = new GattService();
    printMsg("Service: " + service.getUuid() + " - " + service.getAssignedName());
    gattService.setUuid(service.getUuid());
    gattService.setCharacteristics(Stream.of(service.getCharacteristics()).map(this::convertToVortoGattCharacteristic)
        .collect(Collectors.toList()));
    return gattService;
  }

  private GattDevice convertToVortoGattDevice(BluetoothLEDevice bleDevice) {
    GattDevice device = new GattDevice();
    //printMsg("Device: " + bleDevice.getName() + ":" + bleDevice.getAddress() + " Num of services:" + bleDevice.getServices().length);
    device.setModelNumber(bleDevice.getName() + ":" + bleDevice.getAddress());
    device.setServices(
        Stream.of(bleDevice.getServices()).map(this::convertToVortoGattService).collect(Collectors.toList()));
    return device;
  }

  private void printMsg(String string) {
    //System.out.println("[DEMO] " + string);
  }
}
