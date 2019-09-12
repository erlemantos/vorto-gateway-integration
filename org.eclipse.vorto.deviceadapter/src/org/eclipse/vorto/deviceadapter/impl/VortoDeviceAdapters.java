package org.eclipse.vorto.deviceadapter.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.vorto.deviceadapter.api.IDeviceAdapter;
import org.eclipse.vorto.deviceadapter.api.IVortoDeviceAdapters;
import org.eclipse.vorto.mapping.engine.MappingEngine;
import org.eclipse.vorto.mapping.engine.model.spec.IMappingSpecification;
import org.osgi.framework.BundleContext;

import com.prosyst.mbs.services.btle.BluetoothLEController;

public class VortoDeviceAdapters implements IVortoDeviceAdapters {

  private BundleContext bundleContext;
  private BluetoothLEController bleController;
  
  public VortoDeviceAdapters(BundleContext context, BluetoothLEController bluetoothLeController) {
    this.bundleContext = context;
    this.bleController = bluetoothLeController;
  }
  
  @Override
  public IDeviceAdapter getAdapter(String mappingSpec) {
    try {
      return new VortoDeviceAdapter(bundleContext, bleController, 
          getMappingSpecs(mappingSpec), getMappingEngine(mappingSpec));
    } catch (IOException e) {
      throw new IDeviceAdapter.DeviceAdapterProblem("Error while trying to instantiate device adapter", e);
    }  
  }

  private MappingEngine getMappingEngine(String mappingSpec) throws IOException {
    return MappingEngine.createFromInputStream(getInputStream(mappingSpec));
  }

  private IMappingSpecification getMappingSpecs(String mappingSpec) throws IOException {
    return IMappingSpecification.newBuilder().fromInputStream(getInputStream(mappingSpec)).build();
  }
  
  private InputStream getInputStream(String string) throws IOException {
    return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    //return bundleContext.getBundle().getResource(string).openConnection().getInputStream();
  }
}
