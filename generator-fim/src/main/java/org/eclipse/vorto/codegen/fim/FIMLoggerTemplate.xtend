package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel
import org.eclipse.vorto.plugin.generator.utils.IFileTemplate
import org.eclipse.vorto.plugin.generator.InvocationContext

class FIMLoggerTemplate implements IFileTemplate<InformationModel> {
	
	override getFileName(InformationModel context) {
		return "Logger.java"
	}
	
	override getPath(InformationModel context) {
		return "org.eclipse.vorto.deviceadapter.fim." + context.name.toLowerCase + 
			"/src/org/eclipse/vorto/deviceadapter/fim/" + context.name.toLowerCase;
	}
	
	override getContent(InformationModel element, InvocationContext context) {
		return '''
package org.eclipse.vorto.deviceadapter.fim.tisensortag;

public class Logger {
	
	public static Logger instance() {
		return new Logger();
	}

	public void info(String str) {
		System.out.println(str);
	}

	public void error(String str) {
		System.out.println(str);
	}

	public void error(String str, Throwable e) {
		System.out.println(str);
		e.printStackTrace();
	}

}
		'''
	}
	
}