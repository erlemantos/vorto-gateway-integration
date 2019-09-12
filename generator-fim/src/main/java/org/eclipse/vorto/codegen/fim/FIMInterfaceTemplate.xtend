package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel
import org.eclipse.vorto.plugin.generator.InvocationContext
import org.eclipse.vorto.plugin.generator.utils.IFileTemplate

class FIMInterfaceTemplate implements IFileTemplate<InformationModel> {
	
	override getFileName(InformationModel context) {
		return "I" + context.name + ".java";
	}
	
	override getPath(InformationModel context) {
		return "org.eclipse.vorto.deviceadapter.fim." + context.name.toLowerCase + 
			"/src/org/eclipse/vorto/deviceadapter/fim/" + context.name.toLowerCase + "/fi";
	}
	
	override getContent(InformationModel element, InvocationContext context) {
		return '''
package org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase».fi;

«FOR property : element.properties»
import org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase».fi.fb.«property.type.name»;
«ENDFOR»

import com.prosyst.mbs.services.fim.FunctionalItem;
import com.prosyst.mbs.services.fim.annotations.Description;
import com.prosyst.mbs.services.fim.annotations.Item;
import com.prosyst.mbs.services.fim.annotations.Name;
import com.prosyst.mbs.services.fim.annotations.Operation;
import com.prosyst.mbs.services.fim.annotations.Property;
import com.prosyst.mbs.services.fim.annotations.Version;

@Item
@Name("«element.name» Functional Item")
@Description("A functional item derived from the Vorto Model of «element.name»")
@Version("1.0.0")
public interface I«element.name» extends FunctionalItem {
	
	«FOR property : element.properties»
	@Property
	@Description("«property.type.name» property.")
	String PROPERTY_«property.type.name.toUpperCase» = "«Utils.getFbPropertyName(property)»";
	«ENDFOR»

	«FOR property : element.properties»
	«property.type.name» get«Utils.getFbPropertyName(property).toFirstUpper»();
	«ENDFOR»
	
	@Operation
	@Description("Get the current status value.")
	void getCurrent();
}
		'''
	}
	
}