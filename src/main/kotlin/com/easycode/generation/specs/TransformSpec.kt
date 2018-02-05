package com.easycode.generation.specs

import com.easycode.generation.support.readFromResorces
import com.easycode.generation.support.writeAsCodeFile
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File


internal fun generateTransformFile(outputDirectory: File, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] *** Generate Transform Report File")

    val cucumberFile = readFromResorces("CucumberFeatureReport.kt")
    cucumberFile.writeAsCodeFile("com.easycode", "CucumberFeatureReport.kt", outputDirectory)
}

