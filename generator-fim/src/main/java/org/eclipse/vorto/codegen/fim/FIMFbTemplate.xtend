package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel
import org.eclipse.vorto.plugin.generator.InvocationContext
import org.eclipse.vorto.plugin.generator.utils.IFileTemplate

class FIMFbTemplate implements IFileTemplate<InformationModel> {
	
	String fbName;
	
	new(String fbName) {
		this.fbName = fbName;
	}
	
	override getFileName(InformationModel context) {
		var fb = context.properties.stream.filter[name == fbName].findAny.orElseThrow[]
		return fb.type.name + ".java"
	}
	
	override getPath(InformationModel context) {
		return "org.eclipse.vorto.deviceadapter.fim." + context.name.toLowerCase + 
			"/src/org/eclipse/vorto/deviceadapter/fim/" + context.name.toLowerCase + "/fi/fb";
	}
	
	override getContent(InformationModel element, InvocationContext context) {
		var fb = element.properties.stream.filter[name == fbName].findAny.orElseThrow[]
		return '''
package org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase».fi.fb;

import org.eclipse.vorto.model.runtime.FunctionblockValue;

import com.prosyst.mbs.services.fim.annotations.Constructor;
import com.prosyst.mbs.services.fim.annotations.Version;

@Version("1.0.0")
public class «fb.type.name» {
	«FOR property : fb.type.functionblock.status.properties»
	private «Utils.mapSimpleDatatype(property.type)» «Utils.getPropertyName(property)»;
	«ENDFOR»
	
	«GenUtils.firstConstructor(fb)»
	
	«GenUtils.secondConstructor(fb)»
	
	«FOR property : fb.type.functionblock.status.properties»
	«GenUtils.accessorMethods(property)»
	
	«ENDFOR»
}
		'''
	}

}
