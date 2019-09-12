package org.eclipse.vorto.deviceadapter.impl.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.eclipse.vorto.deviceadapter.impl.IVortoConfigurationCreator;
import org.eclipse.vorto.deviceadapter.impl.VortoDeviceAdapter;
import org.eclipse.vorto.model.FunctionblockModel;
import org.eclipse.vorto.model.ModelProperty;
import org.eclipse.vorto.model.PrimitiveType;
import org.eclipse.vorto.model.runtime.PropertyValue;

public class EnableConfigurationCreator implements IVortoConfigurationCreator {
  
  private static final String STEREOTYPE_ON_CONNECT = "OnConnect";
  
  @Override
  public Collection<PropertyValue> getConfigurations(FunctionblockModel fbModel) {
    assert(fbModel != null);
    return fbModel.getStereotype(STEREOTYPE_ON_CONNECT)
        .map(stereotype -> {
          if (stereotype.getAttributes() == null) {
            return null;
          }
          
          String enableUUID = stereotype.getAttributes().get(VortoDeviceAdapter.ATTRIBUTE_UUID);
          String enableValue = stereotype.getAttributes().get(VortoDeviceAdapter.ATTRIBUTE_VALUE);
          String enable = stereotype.getAttributes().get(VortoDeviceAdapter.ATTRIBUTE_ENABLE);
          
          if (Objects.toString(enableUUID, "").trim().isEmpty() || 
              Objects.toString(enableValue, "").trim().isEmpty()) {
            return null;
          }
          
          Collection<PropertyValue> configurationValues = new ArrayList<>();
          
          configurationValues.add(newProperty(VortoDeviceAdapter.ATTRIBUTE_ENABLE, PrimitiveType.BOOLEAN, !"false".equalsIgnoreCase(enable)));
          configurationValues.add(newProperty(VortoDeviceAdapter.ATTRIBUTE_UUID, PrimitiveType.STRING, enableUUID));
          configurationValues.add(newProperty(VortoDeviceAdapter.ATTRIBUTE_VALUE, PrimitiveType.STRING, enableValue));
          
          return configurationValues;
        })
        .orElse(Collections.emptyList());
  }
  
  private PropertyValue newProperty(String propertyName, PrimitiveType type, Object propertyValue) {
    return new PropertyValue(ModelProperty.Builder(propertyName, type).build(), propertyValue);
  }
}
