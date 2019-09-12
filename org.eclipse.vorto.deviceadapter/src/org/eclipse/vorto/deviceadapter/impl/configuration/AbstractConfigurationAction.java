package org.eclipse.vorto.deviceadapter.impl.configuration;

import java.util.Optional;

import org.eclipse.vorto.deviceadapter.api.INewConfiguration;
import org.eclipse.vorto.model.PrimitiveType;
import org.eclipse.vorto.model.runtime.PropertyValue;

public abstract class AbstractConfigurationAction {
  
  protected boolean hasPropertyWithTypeAndValue(INewConfiguration configuration, String name, PrimitiveType type, Object value) {
    return configuration != null &&
        configuration.getConfigurationValues() != null &&
        !configuration.getConfigurationValues().isEmpty() &&
        configuration.getConfigurationValues().stream()
        .filter(property -> property.getMeta().getName().equals(name) &&
                            property.getMeta().getType() == type &&
                            property.getValue().equals(value))
        .findAny()
        .isPresent();
  }
  
  protected Optional<PropertyValue> getPropertyWithType(INewConfiguration configuration, String name, PrimitiveType type) {
    if (configuration == null ||
        configuration.getConfigurationValues() == null ||
        configuration.getConfigurationValues().isEmpty()) {
        return Optional.empty();
      }
    
    return configuration.getConfigurationValues().stream()
          .filter(property -> property.getMeta().getName().equals(name) &&
                              property.getMeta().getType() == type)
          .findAny();
  }
  
  protected Optional<PropertyValue> getArrayPropertyWithType(INewConfiguration configuration, String name, PrimitiveType type) {
    if (configuration == null ||
        configuration.getConfigurationValues() == null ||
        configuration.getConfigurationValues().isEmpty()) {
        return Optional.empty();
      }
    
    return configuration.getConfigurationValues().stream()
        .filter(property -> property.getMeta().getName().equals(name) &&
                            property.getMeta().isMultiple() &&
                            property.getMeta().getType() == type)
        .findAny();
  }
  
}
