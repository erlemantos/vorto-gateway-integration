package org.eclipse.vorto.deviceadapter.impl;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.vorto.deviceadapter.api.DeviceInfo;
import org.eclipse.vorto.deviceadapter.api.IDeviceDiscovery;
import org.eclipse.vorto.deviceadapter.api.IDeviceDiscoveryCallback;
import org.eclipse.vorto.model.Infomodel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.prosyst.mbs.services.btle.BluetoothLEController;
import com.prosyst.mbs.services.btle.BluetoothLEDevice;
import com.prosyst.mbs.services.btle.GattCharacteristic;
import com.prosyst.mbs.services.btle.GattException;
import com.prosyst.mbs.services.btle.GattService;

public class VortoDeviceDiscovery implements IDeviceDiscovery, EventHandler  {

  private static final String MODEL_NUMBER = "Model Number String";

  private static final String DEVICE_INFORMATION_UUID = "180A";
  
  private final BundleContext bundleContext;
  private final BluetoothLEController bluetoothLeController;
  
  private IDeviceDiscoveryCallback callback = null;
  private Infomodel infomodel;
  private ServiceRegistration<EventHandler> eventHandlerServiceRegistration = null;
  private Vector<BluetoothLEDevice> devices;
  
  private final Predicate<BluetoothLEDevice> deviceFilter;
  
  public VortoDeviceDiscovery(
      BundleContext bundleContext,
      BluetoothLEController bluetoothLeController, 
      Infomodel infomodel,
      String deviceModelNumber) {
    this.bundleContext = bundleContext;
    this.bluetoothLeController = bluetoothLeController;
    this.infomodel = infomodel;
    this.deviceFilter = getDeviceFilter(deviceModelNumber);
  }
  
  public void start() {
    devices = Stream.of(bluetoothLeController.getDevices())
        .filter(deviceFilter)
        .collect(Collectors.toCollection(Vector::new));
    
    registerAsEventHandler();
  }
  
  public void stop() {
    if (eventHandlerServiceRegistration != null) {
      eventHandlerServiceRegistration.unregister();
      eventHandlerServiceRegistration = null;
    }
  }
  
  public Collection<BluetoothLEDevice> getDevices() {
    return devices;
  }
  
  public Optional<BluetoothLEDevice> getDevice(String id) {
    return getDevices().stream().filter(dev -> dev.getAddress().equals(id)).findAny();
  }

  @Override
  public void listAvailableDevicesAsync(int scantimeInMs, IDeviceDiscoveryCallback discoveryCallbackHandler) {
    this.callback = discoveryCallbackHandler;
    devices.forEach(dev -> callback.onDeviceFound(new DeviceInfo(dev.getAddress(), infomodel))); 
  }
  
  private void registerAsEventHandler() {
    Dictionary<String, Object> props = new Hashtable<String, Object>(10);
    props.put(EventConstants.EVENT_TOPIC, new String[] {
      BluetoothLEController.TOPIC_DEVICE_ADDED,
      BluetoothLEController.TOPIC_DEVICE_REMOVED});
    eventHandlerServiceRegistration = bundleContext.registerService(EventHandler.class, this, props);
  }

  @Override
  public void handleEvent(Event event) {
    Optional<BluetoothLEDevice> device = getDevice(event);
    if (device.isPresent()) {
      if (event.getTopic().equals(BluetoothLEController.TOPIC_DEVICE_ADDED)) {
        printMsg(event.toString() + " [" + device.get().getAddress() + "]");
        devices.add(device.get());
        callback.onDeviceFound(new DeviceInfo(device.get().getAddress(), infomodel));
      } else if(event.getTopic().equals(BluetoothLEController.TOPIC_DEVICE_REMOVED)) {
        printMsg(event.toString() + " [" + device.get().getAddress() + "]");
        devices.removeIf(savedDevice -> savedDevice.getAddress().equals(device.get().getAddress()));
      }
    }
  }
  
  private Optional<BluetoothLEDevice> getDevice(Event event) {
    String address = (String) event.getProperty(BluetoothLEDevice.PROPERTY_DEVICE_ADDRESS);
    if (bluetoothLeController == null && address == null) {
      return Optional.empty();
    }
    
    BluetoothLEDevice device = bluetoothLeController.getDevice(address);
    if (device == null || !deviceFilter.test(device)) {
      return Optional.empty();
    }
       
    return Optional.of(device);
  }
  
  private Predicate<BluetoothLEDevice> getDeviceFilter(String modelNumberString) {
    return (device) -> {
      GattService service = device.getService(DEVICE_INFORMATION_UUID);
      
      if (service == null || 
          service.getCharacteristics() == null || 
          service.getCharacteristics().length <= 0) {
        return false;
      }
      
      try {
        
        if(!device.isConnected()) {
          device.connectGatt();
        }
        
        boolean isOfModelNumber = false;
        
        for(GattCharacteristic characteristic : service.getCharacteristics()) {
          if (characteristic.getAssignedName().equals(MODEL_NUMBER) && 
              characteristic.getValue() != null) {
            String valueAsString = new String(characteristic.getValue(), "UTF-8").replaceAll("\\P{Print}","");
            if (modelNumberString.equals(valueAsString)) {
              isOfModelNumber = true;
              break;
            }
          }
        }
        
        if (device.isConnected()) {
          device.disconnectGatt();
        }
      
        return isOfModelNumber;
      } catch (GattException | UnsupportedEncodingException e) {
        return false;
      }
    };
  }

  private void printMsg(String string) {
    System.out.println("[DEMO] " + string);
  }
}
