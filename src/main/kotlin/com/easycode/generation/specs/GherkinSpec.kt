package com.easycode.generation.specs

import com.easycode.generation.support.*
import com.squareup.kotlinpoet.*
import gherkin.ast.GherkinDocument
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File


internal fun generateGherkinReportFiles(folder: File, documents: List<GherkinDocument>, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] *** Generate Gherkin Report File")

    val fileSpec = FileSpec.builder("com.easycode", "Gherkin")
            .addType(generateGherkinReportDataClass(documents, logger))
            .addProperty(PropertySpec.builder("report", ClassName("com.easycode", "GherkinReport"))
                    .initializer("GherkinReport()")
                    .build())
            .build()
    fileSpec.writeTo(folder)

    logger.log(LogLevel.DEBUG, "[GHERK] *** Generate Support Report File")

    readFromResorces("GherkReport.kt").writeAsCodeFile("com.easycode", "GherkReport.kt", folder)
}

private fun generateGherkinReportDataClass(documents: List<GherkinDocument>, logger: Logger): TypeSpec {
    logger.log(LogLevel.DEBUG, "[GHERK] ***** Generate Gherkin Report  class")
    return TypeSpec.classBuilder("GherkinReport")
            .addProperties(documents.map { generateFeatureReportProperty(it) })
            .addFunction(generateReportFunction(documents))
            .build()
}

private fun generateFeatureReportProperty(document: GherkinDocument): PropertySpec {
    return PropertySpec.builder(document.feature.name.asProperty(), ClassName("com.easycode", "FeatureReport"))
            .initializer("FeatureReport(featureName = \"${document.feature.name}\")")
            .build()
}

private fun generateReportFunction(documents: List<GherkinDocument>): FunSpec {
    val builder = FunSpec.builder("report")
    builder.returns(listTypeName(ClassName("com.easycode", "FeatureReport")))
    builder.addCode("val list = %T()\n", arrayList(ClassName("com.easycode", "FeatureReport")))
    documents.map { it.feature }.forEach {
        builder.addCode("list.add(${it.name.asProperty()})\n")
    }
    builder.addCode("return list\n")
    return builder.build()
}
