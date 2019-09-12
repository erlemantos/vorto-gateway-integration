package org.eclipse.vorto.deviceadapter;

import org.eclipse.vorto.deviceadapter.api.IVortoDeviceAdapters;
import org.eclipse.vorto.deviceadapter.impl.VortoDeviceAdapters;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.prosyst.mbs.services.btle.BluetoothLEController;

public class Activator implements BundleActivator,
  ServiceTrackerCustomizer<BluetoothLEController, BluetoothLEController> {

  private BundleContext context;
  private ServiceTracker<BluetoothLEController, BluetoothLEController> bluetoothServiceTracker;
  private ServiceRegistration<IVortoDeviceAdapters> serviceRegistration;
  
	@Override
	public void start(BundleContext context) throws Exception {
	  printMsg("starting bundle");
	  
	  this.context = context;
	  
	  bluetoothServiceTracker = new ServiceTracker<BluetoothLEController, BluetoothLEController>(context,
	      BluetoothLEController.class, this);
	  
	  bluetoothServiceTracker.open();
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
	  printMsg("stopping bundle");
	  
	  if (serviceRegistration != null) {
	    context.ungetService(serviceRegistration.getReference());
	    serviceRegistration = null;
	  }
	  
	  bluetoothServiceTracker.close();
		bluetoothServiceTracker = null;
	}

  @Override
  public BluetoothLEController addingService(ServiceReference<BluetoothLEController> reference) {
    printMsg("service retrieved");
    
    BluetoothLEController bluetoothLeController = context.getService(reference);
    
    serviceRegistration = 
        context.registerService(IVortoDeviceAdapters.class, 
        new VortoDeviceAdapters(context, bluetoothLeController), null);
    
    printMsg("service registered!");
    
    return null;
  }

  @Override
  public void modifiedService(ServiceReference<BluetoothLEController> reference, BluetoothLEController service) {
    
  }

  @Override
  public void removedService(ServiceReference<BluetoothLEController> reference, BluetoothLEController service) {
    printMsg("stopping service");
    
    context.ungetService(serviceRegistration.getReference());
    serviceRegistration = null;
    
    context.ungetService(reference);
  }

  public BundleContext getContext() {
    return context;
  }
  
  private void printMsg(String str) {
    System.out.println("[DEMO] " + str);
  }
}
