package org.eclipse.vorto.codegen.fim;

import org.eclipse.vorto.core.api.model.informationmodel.InformationModel;
import org.eclipse.vorto.plugin.generator.GeneratorException;
import org.eclipse.vorto.plugin.generator.GeneratorPluginInfo;
import org.eclipse.vorto.plugin.generator.ICodeGenerator;
import org.eclipse.vorto.plugin.generator.IGenerationResult;
import org.eclipse.vorto.plugin.generator.InvocationContext;
import org.eclipse.vorto.plugin.generator.utils.GenerationResultZip;
import org.eclipse.vorto.plugin.generator.utils.GeneratorTaskFromFileTemplate;

public class FIMGenerator implements ICodeGenerator {

  @Override
  public IGenerationResult generate(InformationModel model, InvocationContext context)
      throws GeneratorException {
    GenerationResultZip output = new GenerationResultZip(model, "fim");
    
    new GeneratorTaskFromFileTemplate<InformationModel>(new ProjectDescriptorTemplate()).generate(model, context, output);
    
    new GeneratorTaskFromFileTemplate<InformationModel>(new ProjectClasspathTemplate()).generate(model, context, output);
    
    new GeneratorTaskFromFileTemplate<InformationModel>(new BuildPropertiesTemplate()).generate(model, context, output);
    
    new GeneratorTaskFromFileTemplate<InformationModel>(new ManifestTemplate()).generate(model, context, output);
    
    new GeneratorTaskFromFileTemplate<InformationModel>(new FIMActivatorTemplate()).generate(model, context, output);
    
    new GeneratorTaskFromFileTemplate<InformationModel>(new FIMLoggerTemplate()).generate(model, context, output);
    
    new GeneratorTaskFromFileTemplate<InformationModel>(new FIMInterfaceTemplate()).generate(model, context, output);
    
    new GeneratorTaskFromFileTemplate<InformationModel>(new FIMImplementationTemplate()).generate(model, context, output);
    
    model.getProperties().forEach(fb -> {
      new GeneratorTaskFromFileTemplate<InformationModel>(new FIMFbTemplate(fb.getName())).generate(model, context, output);
    });
    
    return output;
  }

  @Override
  public GeneratorPluginInfo getMeta() {
    return GeneratorPluginInfo.Builder("fim")
        .withName("FIM")
        .withDescription("Creates a functional item bundle that can be deployed to IoT Gateway Runtime").build();
  }

}
