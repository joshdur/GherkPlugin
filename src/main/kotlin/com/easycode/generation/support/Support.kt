package com.easycode.generation.support

import com.easycode.generation.specs.ScenarioType
import com.easycode.generation.specs.Scene
import com.easycode.generation.specs.SceneExample
import com.easycode.generation.specs.SceneStep
import com.squareup.kotlinpoet.*
import gherkin.ast.*
import org.gradle.api.GradleException
import java.io.File
import java.net.URL


internal fun String.asFileName(): String {
    return camelCase(clean())
}

internal fun String.asClassName(): String {
    return camelCase(clean(), false, "_")
}

internal fun String.asCamelcaseClassName(): String {
    return camelCase(clean(), false)
}

internal fun String.asMethod(): String {
    return camelCase(clean(), true, "_")
}

internal fun String.asCamelcaseMethod(): String {
    return camelCase(clean(), true)
}

internal fun String.asProperty(): String {

    return camelCase(clean(), true)
}

internal fun String.clean(): String {
    return replace("['\"_\\-+.^:,<>]".toRegex(), "")
}

internal fun camelCase(str: String, ignoreFirst: Boolean = false, append: String = ""): String {
    val builder = StringBuilder()
    val split = str.split(" ")
    split.forEach {
        if (it.isNotEmpty()) {
            builder.append(capitalize(it))
            builder.append(append)
        }
    }
    val value = if (append.isNotEmpty()) builder.toString().reversed().substring(append.length).reversed() else builder.toString()
    return if (ignoreFirst) unCapitalize(value) else value
}

internal fun capitalize(str: String): String {
    return when {
        str.length > 1 -> str.first().toUpperCase() + str.substring(1)
        str.length == 1 -> str.toUpperCase()
        else -> str
    }
}

internal fun unCapitalize(str: String): String {
    return when {
        str.length > 1 -> str.first().toLowerCase() + str.substring(1)
        str.length == 1 -> str.toLowerCase()
        else -> str
    }
}


internal fun listTypeName(typeName: TypeName): TypeName {
    return ParameterizedTypeName.get(MutableList::class.asClassName(), typeName)
}

internal fun gherkClassName(name: String) = ClassName("com.easycode", name)

internal fun arrayList(name: String) = ParameterizedTypeName.get(ArrayList::class.asTypeName(), gherkClassName(name))


internal fun list(name: String) = ParameterizedTypeName.get(List::class.asTypeName(), gherkClassName(name))

internal fun readFromResorces(fileName: String): String {
    return getResource(fileName, fileName::class.java)?.readText() ?: throw GradleException("Couldn't find $fileName")
}

internal fun String.writeAsCodeFile(pack: String, name: String, outputFolder: File) {
    val file = File(outputFolder, name)
    file.writeText(this.replace("{OUTPUT_PACKAGE}", pack))
}

private fun getResource(resourceName: String?, callingClass: Class<*>): URL? {
    var url = Thread.currentThread().contextClassLoader.getResource(resourceName)

    if (url == null) {
        url = String::class.java.classLoader.getResource(resourceName)
    }

    if (url == null) {
        val cl = callingClass.classLoader

        if (cl != null) {
            url = cl.getResource(resourceName)
        }
    }

    return if (url == null && resourceName != null && (resourceName.isEmpty() || resourceName[0] != '/')) {
        getResource('/' + resourceName, callingClass)
    } else url

}

internal fun buildScene(feature: Feature, scenarioDefinition: ScenarioDefinition): Scene {
    val type = scenarioDefinition.getType()
    val steps = scenarioDefinition.steps.map { "${it.keyword} ${it.text}" }
    val examples = if (type == ScenarioType.OUTLINE) (scenarioDefinition as ScenarioOutline).examples.asSceneExample() else null
    val headers = examples?.first

    val sceneSteps = steps.map { step ->
        SceneStep(step, headers?.filter { step.contains("<$it>", true) } ?: emptyList())
    }
    val name = if (type == ScenarioType.BACKGROUND) "background ${feature.name}" else scenarioDefinition.name
    return Scene(
            type,
            feature.name,
            name,
            sceneSteps,
            examples?.second ?: emptyList())
}

private fun ScenarioDefinition.getType() = when (this) {
    is ScenarioOutline -> ScenarioType.OUTLINE
    is Background -> ScenarioType.BACKGROUND
    else -> ScenarioType.SCENARIO
}

private fun List<Examples>.asSceneExample(): Pair<List<String>, List<SceneExample>>? {
    if (isNotEmpty()) {
        val examples = this[0]
        return examples.tableHeader.cells.map { it.value } to examples.tableBody.map {
            SceneExample(examples.tableHeader.cells.zip(it.cells).map { it.first.value to it.second.value }.toMap())
        }
    }
    return null
}

internal fun List<String>.asSeparatedString(separator: String): String {
    return fold("") { acc, s ->
        val a = if (acc.isNotEmpty()) separator else ""
        return a + s
    }
}

internal fun FileSpec.Builder.addFunctions(specs: List<FunSpec>): FileSpec.Builder {
    specs.forEach {
        addFunction(it)
    }
    return this
}