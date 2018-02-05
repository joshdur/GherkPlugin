package com.easycode.generation.specs

import com.easycode.generation.support.list
import com.squareup.kotlinpoet.*
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter


internal fun generateWritterFile(outputDirectory: File, reportFolder: String, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] * Generate Report writter")
    val fileSpec = FileSpec.builder("com.easycode", "ReportWriter")
            .addProperty(PropertySpec.builder("REPORT_PATH", String::class, KModifier.CONST)
                    .initializer("\"$reportFolder\"".replace("//", "/"))
                    .build())
            .addFunction(writeReportFunction(logger))
            .addFunction(reportAsStringFunction(logger))
            .addFunction(transformedReportFunction(logger))
            .build()
    fileSpec.writeTo(outputDirectory)
}

private fun writeReportFunction(logger: Logger): FunSpec {
    logger.log(LogLevel.DEBUG, "[GHERK] **** Generate Report writter")
    return FunSpec.builder("writeReport")
            .addCode("synchronized(REPORT_PATH){\n")
            .addCode("    val path = android.support.test.InstrumentationRegistry.getTargetContext().cacheDir.absolutePath\n")
            .addCode("    val folder = %T(\"\$path/\$REPORT_PATH\")\n", File::class)
            .addCode("    folder.mkdirs()\n")
            .addCode("    val file = File(folder, \"gherk_report.json\")\n")
            .addCode("    if(file.exists()) file.delete()\n")
            .addCode("    val writer = %T(%T(file))\n", OutputStreamWriter::class, FileOutputStream::class)
            .addCode("    writer.write(reportAsString())\n")
            .addCode("    writer.close()\n")
            .addCode("}\n")
            .build()
}

private fun reportAsStringFunction(logger: Logger): FunSpec {
    logger.log(LogLevel.DEBUG, "[GHERK] **** Generate Report toString")
    return FunSpec.builder("reportAsString")
            .returns(String::class)
            .addCode("return com.google.gson.Gson().toJson(transformedReport())\n")
            .build()
}

private fun transformedReportFunction(logger: Logger): FunSpec {
    logger.log(LogLevel.DEBUG, "[GHERK] **** Generate Report transformation")
    return FunSpec.builder("transformedReport")
            .addCode("val reportList = report.report()\n")
            .addCode("return transformToCucumberReport(reportList)\n")
            .returns(list(ClassName("com.easycode", "cucumberFeatureReport")))
            .build()
}
