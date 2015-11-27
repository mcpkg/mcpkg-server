package org.mcpkg.server;

import org.apache.maven.model.building.DefaultModelBuilderFactory
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.ModelBuilder
import org.apache.maven.model.building.ModelBuildingRequest
import org.mcpkg.server.FileRoot
import org.mcpkg.server.FileRootNative
import org.mcpkg.server.FileWrapper
import java.io.File

class DumpPomDeps {
    fun doDump(inputFile: File) {
        val factory = DefaultModelBuilderFactory();
        val builder = factory.newInstance();
        val req = DefaultModelBuildingRequest();
        req.setProcessPlugins(false);
        req.setPomFile(inputFile);
        req.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
        val model = builder.build(req).effectiveModel;
        for(d in model.dependencies) {
            println("everything:${d}");
        }
        println("test:${model}");
    }
}

fun main(args: Array<String>) {
    val inputFile = File(args[0]);
    val dumper = DumpPomDeps();
    dumper.doDump(inputFile);
}