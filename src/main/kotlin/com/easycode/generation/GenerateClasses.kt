package com.easycode.generation

import com.easycode.generation.specs.*
import com.easycode.generation.support.buildScene
import com.easycode.generation.support.parseFromUri
import gherkin.ast.GherkinDocument
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File


internal fun generateRecursive(featureDirectory: File, outputDirectory: File, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] Generating resources")
    val featureFiles = featureFiles(featureDirectory)
    logger.log(LogLevel.DEBUG, "[GHERK] Features -> ${featureFiles.map { it.path }}")
    val documents = featureFiles.mapNotNull { parseFromUri(it.inputStream()) }
    generateClasses(outputDirectory, documents, logger)
}

private fun featureFiles(dir: File): List<File> {
    val files = mutableListOf<File>()
    val stack = mutableListOf<File>()
    stack.add(dir)
    while (stack.isNotEmpty()) {
        val theFile = stack[0]
        theFile.listFiles().forEach {
            if (it.isDirectory) stack.add(it)
            else if (it.name.endsWith(".feature")) files.add(it)
        }
        stack.remove(theFile)
    }
    return files
}


private fun generateClasses(outputDirectory: File, documents: List<GherkinDocument>, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] * Generate Classes")
    generateGherkinReportFiles(outputDirectory, documents, logger)
    logger.log(LogLevel.DEBUG, "[GHERK] * Generate Scenarios")

    documents.forEach { document ->
        val scenes = document.feature.children.map { buildScene(document.feature, it) }
        val background = scenes.firstOrNull { it.type == ScenarioType.BACKGROUND }
        background?.generateBackground(outputDirectory, logger)
        scenes.filter { it.type == ScenarioType.OUTLINE }.forEach { it.generateOutlineScenario(outputDirectory, background, logger) }
        scenes.filter { it.type == ScenarioType.SCENARIO }.forEach { it.generateScenario(outputDirectory, background, logger) }

    }
}


internal fun generateReportClasses(outputDirectory: File, reportFolder: String, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] * Generate Report writter")
    generateTransformFile(outputDirectory, logger)
    generateWritterFile(outputDirectory, reportFolder, logger)
}

