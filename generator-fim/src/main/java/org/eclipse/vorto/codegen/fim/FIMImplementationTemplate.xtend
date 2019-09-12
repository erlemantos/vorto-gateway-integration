package org.eclipse.vorto.codegen.fim

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel
import org.eclipse.vorto.plugin.generator.utils.IFileTemplate
import org.eclipse.vorto.plugin.generator.InvocationContext

class FIMImplementationTemplate implements IFileTemplate<InformationModel> {
	
	override getFileName(InformationModel context) {
		return context.name + ".java";
	}
	
	override getPath(InformationModel context) {
		return "org.eclipse.vorto.deviceadapter.fim." + context.name.toLowerCase + 
			"/src/org/eclipse/vorto/deviceadapter/fim/" + context.name.toLowerCase + "/fi";
	}
	
	override getContent(InformationModel element, InvocationContext context) {
		return '''
package org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase».fi;

import org.eclipse.vorto.deviceadapter.api.DeviceInfo;
import org.eclipse.vorto.deviceadapter.api.IDeviceAdapter;
«FOR property : element.properties»
import org.eclipse.vorto.deviceadapter.fim.«element.name.toLowerCase».fi.fb.«property.type.name»;
«ENDFOR»
import org.eclipse.vorto.model.runtime.FunctionblockValue;

import com.prosyst.mbs.services.fim.spi.AbstractFunctionalItem;

public class «element.name» extends AbstractFunctionalItem implements I«element.name» {
	
	«FOR property : element.properties»
	private «property.type.name» «Utils.getFbPropertyName(property)»;
	«ENDFOR»
	
	private IDeviceAdapter fimDeviceAdapter;
	private DeviceInfo fimDeviceInfo;
  
	public «element.name»(IDeviceAdapter fimDeviceAdapter, DeviceInfo fimDeviceInfo, String fimUid) {
		this(fimUid);
		this.fimDeviceAdapter = fimDeviceAdapter;
		this.fimDeviceInfo = fimDeviceInfo;
	}

	protected «element.name»(String uid) {
		super(uid);
	}
	
	«FOR property : element.properties»
	@Override
	public «property.type.name» get«Utils.getFbPropertyName(property).toFirstUpper»() {
		FunctionblockValue functionBlock = fimDeviceAdapter.receive("«property.name»", fimDeviceInfo.getDeviceId());
		if (functionBlock != null) {
			«property.type.name» old«Utils.getFbPropertyName(property).toFirstUpper» = this.«Utils.getFbPropertyName(property)»;
			this.«Utils.getFbPropertyName(property)» = new «property.type.name»(functionBlock);
			propertyChanged(PROPERTY_«property.type.name.toUpperCase», this.«Utils.getFbPropertyName(property)», old«Utils.getFbPropertyName(property).toFirstUpper», true);
		}
		
		return «Utils.getFbPropertyName(property)»;
	}
	«ENDFOR»
	
	public void getCurrent() {
		«FOR property : element.properties»
		get«Utils.getFbPropertyName(property).toFirstUpper»();
		«ENDFOR»
  	}
}
		'''
	}
	
}