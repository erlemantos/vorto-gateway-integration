package org.eclipse.vorto.deviceadapter.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vorto.deviceadapter.api.INewConfiguration;
import org.eclipse.vorto.model.ModelProperty;
import org.eclipse.vorto.model.PrimitiveType;
import org.eclipse.vorto.model.runtime.PropertyValue;

public class GattPropertyConfiguration implements INewConfiguration {

  private List<PropertyValue> propertyConfigs = new ArrayList<>();
  private String infomodelProperty;
  
  public GattPropertyConfiguration(String infomodelProperty) {
    this.infomodelProperty = infomodelProperty;
  }

  @Override
  public void addConfigurationValue(String propertyName, Object propertyValue) {
    propertyConfigs.add(new PropertyValue(ModelProperty.Builder(propertyName, PrimitiveType.STRING).build(), propertyValue));
  }
  
  public void addConfigurationValue(String propertyName, PrimitiveType type, Object propertyValue) {
    propertyConfigs.add(new PropertyValue(ModelProperty.Builder(propertyName, type).build(), propertyValue));
  }
  
  public void addConfigurationValue(String propertyName, boolean isMultiple, PrimitiveType type, Object propertyValue) {
    if (isMultiple) {
      propertyConfigs.add(new PropertyValue(ModelProperty.Builder(propertyName, type).multiple().build(), propertyValue));
    } else {
      propertyConfigs.add(new PropertyValue(ModelProperty.Builder(propertyName, type).build(), propertyValue));
    }
  }

  @Override
  public String getInfomodelProperty() {
    return infomodelProperty;
  }

  @Override
  public List<PropertyValue> getConfigurationValues() {
    return propertyConfigs;
  }

}
