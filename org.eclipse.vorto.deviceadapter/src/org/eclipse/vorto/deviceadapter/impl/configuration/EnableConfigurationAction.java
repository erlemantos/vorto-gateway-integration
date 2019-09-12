package org.eclipse.vorto.deviceadapter.impl.configuration;

import java.util.Optional;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.vorto.deviceadapter.api.IDeviceData;
import org.eclipse.vorto.deviceadapter.api.INewConfiguration;
import org.eclipse.vorto.deviceadapter.impl.IVortoConfigurationAction;
import org.eclipse.vorto.deviceadapter.impl.VortoDeviceAdapter;
import org.eclipse.vorto.model.PrimitiveType;
import org.eclipse.vorto.model.runtime.PropertyValue;

import com.prosyst.mbs.services.btle.BluetoothLEDevice;
import com.prosyst.mbs.services.btle.GattException;

public class EnableConfigurationAction extends AbstractConfigurationAction implements IVortoConfigurationAction {

  @Override
  public String actionName() {
    return "enable";
  }

  @Override
  public boolean execute(BluetoothLEDevice device, INewConfiguration configuration) {
    try {
      if (hasPropertyWithTypeAndValue(configuration, VortoDeviceAdapter.ATTRIBUTE_ENABLE, 
          PrimitiveType.BOOLEAN, true)) {
        Optional<PropertyValue> maybeEnableGattAddr = getPropertyWithType(configuration, 
            VortoDeviceAdapter.ATTRIBUTE_UUID,
            PrimitiveType.STRING);

        if (maybeEnableGattAddr.isPresent()) {
          Optional<PropertyValue> maybeEnableGattValue = getPropertyWithType(configuration, 
              VortoDeviceAdapter.ATTRIBUTE_VALUE,
              PrimitiveType.STRING);

          if (maybeEnableGattValue.isPresent()) {
            String gattCharEnable = (String) maybeEnableGattAddr.get().getValue();
            String gattEnableValue = (String) maybeEnableGattValue.get().getValue();
            
            for (com.prosyst.mbs.services.btle.GattService service : device.getServices()) {
              com.prosyst.mbs.services.btle.GattCharacteristic gattChar = service.getCharacteristic(gattCharEnable.toUpperCase());
              if (gattChar != null) {
                gattChar.writeValue(Hex.decodeHex(gattEnableValue));
                return true;
              }
            }
          }
        }
      }
    } catch (DecoderException | GattException e) {
      throw new IDeviceData.DeviceConfigurationProblem("Problem accessing device", e);
    }

    return false;
  }

}
