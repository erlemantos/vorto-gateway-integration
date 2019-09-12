package org.eclipse.vorto.deviceadapter.impl;

import java.util.Collection;

import org.eclipse.vorto.model.FunctionblockModel;
import org.eclipse.vorto.model.runtime.PropertyValue;

public interface IVortoConfigurationCreator {
  
  Collection<PropertyValue> getConfigurations(FunctionblockModel fbModel);
  
}
