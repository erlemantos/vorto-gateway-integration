package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.datatype.Property
import org.eclipse.vorto.core.api.model.informationmodel.FunctionblockProperty

class GenUtils {
	
	static def accessorMethods(Property property) {
		return '''
	public «Utils.mapSimpleDatatype(property.type)» get«Utils.getPropertyName(property).toFirstUpper»() {
		return «Utils.getPropertyName(property)»;
	}
	
	public void set«Utils.getPropertyName(property).toFirstUpper»(«Utils.mapSimpleDatatype(property.type)» «Utils.getPropertyName(property)») {
		this.«Utils.getPropertyName(property)» = «Utils.getPropertyName(property)»;
	}
		'''
	}
	
	static def firstConstructor(FunctionblockProperty fb) {
		var fbProperties = fb.type.functionblock.status.properties
		return '''
	@Constructor({ «FOR property : fbProperties  SEPARATOR ', '»"«Utils.getPropertyName(property)»"«ENDFOR» })
	public «fb.type.name»(«FOR property : fbProperties SEPARATOR ', '»«Utils.mapSimpleDatatype(property.type)» «Utils.getPropertyName(property)»«ENDFOR») {
		«FOR property : fb.type.functionblock.status.properties»
		this.«Utils.getPropertyName(property)» = «Utils.getPropertyName(property)»;
		«ENDFOR»
	}
		'''
	}
	
	static def secondConstructor(FunctionblockProperty fb) {
		return '''
	public «fb.type.name»(FunctionblockValue functionBlock) {
		«FOR property : fb.type.functionblock.status.properties»
		functionBlock.getStatusProperty("«property.name»").ifPresent(propValue -> {
			«Utils.getPropertyName(property)» = («Utils.mapSimpleDatatype(property.type)») propValue.getValue();
		});
		
		«ENDFOR»
	}
		'''
	}
	
}