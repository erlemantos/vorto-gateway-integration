package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel
import org.eclipse.vorto.plugin.generator.utils.IFileTemplate
import org.eclipse.vorto.plugin.generator.InvocationContext

class BuildPropertiesTemplate implements IFileTemplate<InformationModel> {
	
	override getFileName(InformationModel context) {
		return "build.properties"
	}
	
	override getPath(InformationModel context) {
		return "org.eclipse.vorto.deviceadapter.fim." + context.name.toLowerCase
	}
	
	override getContent(InformationModel element, InvocationContext context) {
		return '''
source.. = src/
output.. = bin/
bin.includes = META-INF/,\
               .
		'''
	}
}