package org.eclipse.vorto.cloudservice;

import org.eclipse.vorto.cloudservice.api.ICloudServiceProvider;
import org.eclipse.vorto.cloudservice.hono.HonoCloudServiceProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	public static BundleContext context;
	private ServiceRegistration<ICloudServiceProvider> serviceRegistration;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		serviceRegistration = 
        context.registerService(ICloudServiceProvider.class, 
        new HonoCloudServiceProvider(), null);
	}

	public void stop(BundleContext bundleContext) throws Exception {
	  if (serviceRegistration != null) {
      context.ungetService(serviceRegistration.getReference());
      serviceRegistration = null;
    }
	  
		Activator.context = null;
	}

}
