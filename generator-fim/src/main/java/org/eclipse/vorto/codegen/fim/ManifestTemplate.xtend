package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel
import org.eclipse.vorto.plugin.generator.utils.IFileTemplate
import org.eclipse.vorto.plugin.generator.InvocationContext

class ManifestTemplate implements IFileTemplate<InformationModel> {
	
	override getFileName(InformationModel context) {
		return "MANIFEST.MF";
	}
	
	override getPath(InformationModel context) {
		return "org.eclipse.vorto.deviceadapter.fim." + context.name.toLowerCase + "/META-INF";
	}
	
	override getContent(InformationModel element, InvocationContext context) {
		return '''
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: Vorto-generated Functional Item for «element.name» 
Bundle-SymbolicName: org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase»
Bundle-Version: 1.0.0.qualifier
Bundle-Activator: org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase».Activator
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
Import-Package: com.google.gson,
 com.prosyst.mbs.services.fim;version="1.3.1",
 com.prosyst.mbs.services.fim.annotations;version="1.1.0",
 com.prosyst.mbs.services.fim.spi;version="1.5.0",
 org.apache.commons.io;version="1.4.0",
 org.eclipse.vorto.cloudservice.api,
 org.eclipse.vorto.deviceadapter.api,
 org.eclipse.vorto.model,
 org.eclipse.vorto.model.runtime,
 org.osgi.framework;version="1.3.0",
 org.osgi.util.tracker;version="1.5.1"
Automatic-Module-Name: org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase»
Export-Package: org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase»,
 org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase».fi
		'''
	}
	
}