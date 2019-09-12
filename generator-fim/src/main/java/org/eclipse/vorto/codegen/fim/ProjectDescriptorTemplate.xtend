package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel
import org.eclipse.vorto.plugin.generator.utils.IFileTemplate
import org.eclipse.vorto.plugin.generator.InvocationContext

class ProjectDescriptorTemplate implements IFileTemplate<InformationModel> {
	
	override getFileName(InformationModel context) {
		return ".project"
	}
	
	override getPath(InformationModel context) {
		return "org.eclipse.vorto.deviceadapter.fim." + context.name.toLowerCase;
	}
	
	override getContent(InformationModel element, InvocationContext context) {
		return '''
<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
	<name>org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase»</name>
	<comment></comment>
	<projects>
	</projects>
	<buildSpec>
		<buildCommand>
			<name>org.eclipse.jdt.core.javabuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
		<buildCommand>
			<name>org.eclipse.pde.ManifestBuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
		<buildCommand>
			<name>org.eclipse.pde.SchemaBuilder</name>
			<arguments>
			</arguments>
		</buildCommand>
	</buildSpec>
	<natures>
		<nature>org.eclipse.pde.PluginNature</nature>
		<nature>org.eclipse.jdt.core.javanature</nature>
	</natures>
</projectDescription>
		'''
	}
	
}