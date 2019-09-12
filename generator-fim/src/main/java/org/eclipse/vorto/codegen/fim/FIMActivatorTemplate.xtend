package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel
import org.eclipse.vorto.plugin.generator.utils.IFileTemplate
import org.eclipse.vorto.plugin.generator.InvocationContext

class FIMActivatorTemplate implements IFileTemplate<InformationModel> {
	
	override getFileName(InformationModel context) {
		return "Activator.java"
	}
	
	override getPath(InformationModel context) {
		return "org.eclipse.vorto.deviceadapter.fim." + context.name.toLowerCase + 
			"/src/org/eclipse/vorto/deviceadapter/fim/" + context.name.toLowerCase;
	}
	
	override getContent(InformationModel element, InvocationContext context) {
		return '''
package org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase»;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.vorto.cloudservice.api.ICloudService;
import org.eclipse.vorto.cloudservice.api.ICloudServiceConfig;
import org.eclipse.vorto.cloudservice.api.ICloudServiceProvider;
import org.eclipse.vorto.deviceadapter.api.DeviceInfo;
import org.eclipse.vorto.deviceadapter.api.IDeviceAdapter;
import org.eclipse.vorto.deviceadapter.api.IDeviceDiscoveryCallback;
import org.eclipse.vorto.deviceadapter.api.IVortoDeviceAdapters;
import org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase».fi.«element.name»;
import org.eclipse.vorto.model.runtime.InfomodelValue;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import com.google.gson.Gson;
import com.prosyst.mbs.services.fim.spi.AbstractFunctionalItem;
import com.prosyst.mbs.services.fim.spi.FunctionalItemAdminSpi;

public class Activator implements BundleActivator, Runnable {

	private static final String PASSWORD_KEY = "password";
	
	private static final String TENANT_KEY = "tenant";
	
	private static final String CLOUD_SERVICE_PROVIDER_URL_KEY = "hostUrl";

	private static final String NAMESPACE = "THE NAMESPACE THAT YOU REGISTERED";

	private static final String CLOUD_SERVICE_PROVIDER_URL = "ssl://mqtt.bosch-iot-hub.com:8883";

	private static final String TENANT = "THE TENANT ID CREATED WHEN YOU PROVISIONED YOUR DEVICE.";
	
	private static final String DEVICE_PASSWORD = "YOUR DEVICE PASSWORD";

	private static final String DEVICE_MAPPING_SPEC = "YOUR MAPPING SPEC FILE";

	private static final Logger logger = Logger.instance();
	
	private BundleContext context;
	
	private ServiceTracker<FunctionalItemAdminSpi, FunctionalItemAdminSpi> fimTracker;
	
	private ServiceTracker<IVortoDeviceAdapters, IVortoDeviceAdapters> deviceAdapterTracker;
	
	private ServiceTracker<ICloudServiceProvider, ICloudServiceProvider> cloudServiceTracker;
	
	private List<AbstractFunctionalItem> functionalItems = new ArrayList<AbstractFunctionalItem>();
	
	private String mappingSpec;
	
	private ScheduledFuture<?> discoveryTask = null;
	
	private Gson gson = new Gson();
	
	private String cloudConfig = null;
	
	ICloudService cloudService = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		mappingSpec = getMappingSpec(DEVICE_MAPPING_SPEC);
		
		this.cloudConfig = getCloudConfig();
		
		fimTracker = new ServiceTracker<FunctionalItemAdminSpi, FunctionalItemAdminSpi>
		  (context, FunctionalItemAdminSpi.class, null);
		
		deviceAdapterTracker = new ServiceTracker<IVortoDeviceAdapters, IVortoDeviceAdapters>
			(context, IVortoDeviceAdapters.class, null);
		
		cloudServiceTracker = new ServiceTracker<ICloudServiceProvider, ICloudServiceProvider>
			(context, ICloudServiceProvider.class, null);
		
		fimTracker.open();
		deviceAdapterTracker.open();
		cloudServiceTracker.open();
		
		functionalItems.clear();
		
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		discoveryTask = executor.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
	}

	// TODO : modify based on your cloud service provider
	private String getCloudConfig() {
		Map<String, String> cloudConfig = new HashMap<>();
		cloudConfig.put(TENANT_KEY, TENANT);
		cloudConfig.put(CLOUD_SERVICE_PROVIDER_URL_KEY, CLOUD_SERVICE_PROVIDER_URL);
		cloudConfig.put(PASSWORD_KEY, DEVICE_PASSWORD);
		
		return gson.toJson(cloudConfig);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		if (discoveryTask != null) {
			discoveryTask.cancel(true);
			discoveryTask = null;
		}

		functionalItems.forEach(item -> item.unregister());
		functionalItems.clear();
	
		fimTracker.close();
		deviceAdapterTracker.close();
		cloudServiceTracker.close();

		fimTracker = null;
		deviceAdapterTracker = null;
		cloudServiceTracker = null;
	}

	public void run() {
		logger.info("Running task.");

		IVortoDeviceAdapters deviceAdapters = deviceAdapterTracker.getService();
		if (deviceAdapters == null) {
			if (!functionalItems.isEmpty()) {
				functionalItems.forEach(item -> item.unregister());
				functionalItems.clear();
			}
			return;
		}

		IDeviceAdapter deviceAdapter = deviceAdapters.getAdapter(mappingSpec);
		try {
			logger.info("Starting discovery.");
			deviceAdapter.listAvailableDevicesAsync(3000, new IDeviceDiscoveryCallback() {
				public void onDeviceFound(DeviceInfo deviceInfo) {
					logger.info("Found Device [" + deviceInfo.getDeviceId() + "].");

					InfomodelValue value = deviceAdapter.receive(deviceInfo.getDeviceId());
					logger.info("Got infomodel.");

					updateTwin(deviceInfo, value);

					if (!hasFunctionalItem(deviceInfo)) {
						addFunctionalItem(deviceAdapter, deviceInfo);
					}
				} 
			});
		} catch (Exception e) {
			logger.error("From device data acquisition.", e);
		}
	}
  
	private void updateTwin(DeviceInfo deviceInfo, InfomodelValue value) {
		try {
			ICloudService cloudService = getCloudService();
			if (cloudService == null) {
				return;
			}

			logger.info("Updating twin.");

			cloudService.publish(getDeviceInfo(deviceInfo.getDeviceId()), value);
		} catch (Exception e) {
			logger.error("While updating twin.", e);
		}
	}
	
	private ICloudService getCloudService() {
		if (cloudService == null) {
			ICloudServiceProvider cloudServiceProvider = cloudServiceTracker.getService();
			if (cloudServiceProvider != null) {
				ICloudServiceConfig config = cloudServiceProvider.getBuilder(cloudConfig).build();
				cloudService = cloudServiceProvider.getCloudService(config);
			}
		} else {
			// service disappeared
			if (cloudServiceTracker.getService() == null) {
				cloudService = null;
			}
		}

		return cloudService;
	}

	// TODO : Modify based on your cloud service provider
	private String getDeviceInfo(String deviceId) {
		return NAMESPACE + ":" + deviceId;
	}

	private boolean hasFunctionalItem(DeviceInfo deviceInfo) {
		return functionalItems.stream().anyMatch(item -> item.getUID().equals(getUid(deviceInfo)));
	}

	private void addFunctionalItem(IDeviceAdapter deviceAdapter, DeviceInfo deviceInfo) {
		FunctionalItemAdminSpi fimService = fimTracker.getService();
		if (fimService == null) {
			return;
		}

		logger.info("Creating functional item for Device [" + deviceInfo.getDeviceId() + "] with UID [ " + getUid(deviceInfo) + " ].");
		AbstractFunctionalItem fnItem = new «element.name»(deviceAdapter, deviceInfo, getUid(deviceInfo)); 
		fnItem.register(context, fimService);
		functionalItems.add(fnItem);
	}

	private String getUid(DeviceInfo deviceInfo) {
		return String.format("%s::%s::%s", "vorto", deviceInfo.getDeviceId(), "«element.name»");
	}
	
	private String getMappingSpec(String filename) throws IOException {
		return IOUtils.toString(
			context.getBundle().getResource(filename).openConnection().getInputStream(), 
			StandardCharsets.UTF_8.toString());
	}
}
		'''
	}
	
}