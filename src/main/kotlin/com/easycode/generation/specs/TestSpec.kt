package com.easycode.generation.specs

import com.easycode.generation.support.*
import com.squareup.kotlinpoet.*
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

enum class ScenarioType {
    BACKGROUND, OUTLINE, SCENARIO
}

data class Scene(val type: ScenarioType, val featName: String, val name: String, val steps: List<SceneStep>, val examples: List<SceneExample>)

data class SceneStep(val text: String, val params: List<String>)

data class SceneExample(val map: Map<String, String>)

internal fun Scene.generateBackground(folder: File, pack: String, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] *** Scenario Background -> ${name.asFileName()}")
    val file = FileSpec.builder(pack, name.asFileName())
            .addStaticImport("com.easycode", "*")
            .addType(generateScenarioType(name.asCamelcaseClassName(), generateBundleType(steps, generateExecuteFunction(steps))))
            .addFunction(generateBackgroundFunction(name))
            .addFunctions(steps.map { dslFunction(ClassName("", name.asCamelcaseClassName()), it) })
            .build()
    folder.mkdirs()
    file.writeTo(folder)
}


internal fun Scene.generateScenario(folder: File, background: Scene?, pack: String, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] *** Scenario -> ${name.asFileName()}")
    val file = FileSpec.builder(pack, name.asFileName())
            .addStaticImport("com.easycode", "*")
            .addType(generateScenarioType(name.asClassName(), generateBundleType(steps, generateExecuteFunction(steps))))
            .addFunction(generateScenarioTestFunction(this, background))
            .addFunctions(steps.map { dslFunction(ClassName("", name.asClassName()), it) })

            .build()
    folder.mkdirs()
    file.writeTo(folder)
}

internal fun Scene.generateOutlineScenario(folder: File, background: Scene?, pack: String, logger: Logger) {
    logger.log(LogLevel.DEBUG, "[GHERK] *** Scenario Outline -> ${name.asFileName()} $examples")
    val file = FileSpec.builder(pack, name.asFileName())
            .addStaticImport("com.easycode", "*")
            .addType(generateScenarioType(name.asClassName(), generateBundleType(steps, generateExecuteOutlineFunction(steps))))
            .addFunction(generateScenarioOutlineTestFunction(this, background))
            .addFunction(generateExamplesFunction(name.asClassName(), examples))
            .addFunctions(steps.map { dslFunction(ClassName("", name.asClassName()), it) })
            .build()
    folder.mkdirs()
    file.writeTo(folder)
}

private fun generateScenarioType(scenarioName: String, bundleType: TypeSpec): TypeSpec {
    return TypeSpec.classBuilder(scenarioName)
            .addType(bundleType)
            .addProperty(PropertySpec.builder("bundle", ClassName("", "Bundle"))
                    .initializer("Bundle()")
                    .build())
            .build()
}

private fun generateBundleType(steps: List<SceneStep>, exec: FunSpec): TypeSpec {
    return TypeSpec.classBuilder("Bundle")
            .addProperties(steps.map(::lambdaProperty))
            .addFunction(exec)
            .build()
}

private fun generateExecuteFunction(steps: List<SceneStep>): FunSpec {
    val builder = FunSpec.builder("execute")
    steps.forEach { builder.addCode("${it.text.asProperty()}()\n") }
    return builder.build()
}

private fun generateExecuteOutlineFunction(steps: List<SceneStep>): FunSpec {
    val builder = FunSpec.builder("execute")
    builder.addParameter("map", ParameterizedTypeName.get(Map::class, String::class, String::class))
    steps.forEach {
        val params = it.params.map { "map.getValue(\"$it\")" }.asSeparatedString(",")
        builder.addCode("${it.text.asProperty()}($params)\n")
    }
    return builder.build()
}

private fun lambdaProperty(step: SceneStep): PropertySpec {
    val parameters = step.params.map { String::class.asTypeName() }
    val params = parameters.map { "_" }.asSeparatedString(",")
    val arrow = if (params.isNotEmpty()) " -> " else ""
    val lambdaType = LambdaTypeName.get(returnType = Unit::class.asTypeName(), parameters = parameters)
    return PropertySpec.varBuilder(step.text.asProperty(), lambdaType)
            .initializer("{$params$arrow}")
            .build()
}

private fun dslFunction(scenario: TypeName, step: SceneStep): FunSpec {
    val parameters = step.params.map { String::class.asTypeName() }
    val lambdaType = LambdaTypeName.get(returnType = Unit::class.asTypeName(), parameters = parameters)
    return FunSpec.builder(step.text.asMethod())
            .receiver(scenario)
            .addParameter("dsl", lambdaType)
            .addCode("bundle.${step.text.asProperty()} = dsl\n")
            .build()
}

private fun generateBackgroundFunction(backgroundName: String): FunSpec {
    val backgroundClassName = ClassName("", backgroundName.asCamelcaseClassName())
    val builder = FunSpec.builder(backgroundName.asCamelcaseMethod())
    builder.addParameter("dsl", LambdaTypeName.get(receiver = backgroundClassName, returnType = Unit::class.asTypeName()))
    builder.returns(backgroundClassName)
    builder.addCode("    val background = ${backgroundName.asCamelcaseClassName()}()\n")
    builder.addCode("    background.dsl()\n")
    builder.addCode("    return background\n")
    return builder.build()
}

private fun generateScenarioTestFunction(scene: Scene, background: Scene?): FunSpec {
    val scenarioClassName = ClassName("", scene.name.asClassName())
    val builder = FunSpec.builder("test_${scene.name.asClassName()}")
    background?.let {
        builder.addParameter("background", backgroundTypeName(background))
    }
    builder.addParameter("dsl", LambdaTypeName.get(receiver = scenarioClassName, returnType = Unit::class.asTypeName()))
    builder.addCode("try {\n")
    builder.addCode("    val scenario = ${scene.name.asClassName()}()\n")
    background?.let {
        builder.addCode("    background.bundle.execute()\n")
    }
    builder.addCode("    scenario.dsl()\n")
    builder.addCode("    scenario.bundle.execute()\n")
    builder.addCode("    report.${scene.featName.asProperty()}.scenarioReports.add(ScenarioReport(\"${scene.name}\", ScenarioTestStatus.PASSED))\n")
    builder.addCode("} catch(throwable : Throwable){\n")
    builder.addCode("    report.${scene.featName.asProperty()}.scenarioReports.add(ScenarioReport(\"${scene.name}\", ScenarioTestStatus.ERROR, throwable))\n")
    builder.addCode("    throw throwable\n")
    builder.addCode("} finally {\n")
    builder.addCode("    writeReport()\n")
    builder.addCode("}\n")
    return builder.build()
}

private fun backgroundTypeName(background: Scene): TypeName {
    val bgClassName = background.name.asCamelcaseClassName()
    return ClassName("", bgClassName)
}

private fun generateScenarioOutlineTestFunction(scene: Scene, background: Scene?): FunSpec {
    val scenarioClassName = ClassName("", scene.name.asClassName())

    val builder = FunSpec.builder("test_${scene.name.asClassName()}")
    background?.let {
        builder.addParameter("background", backgroundTypeName(background))
    }
    builder.addParameter("dsl", LambdaTypeName.get(receiver = scenarioClassName, returnType = Unit::class.asTypeName()))
    builder.addCode("try {\n")
    builder.addCode("    val scenario = ${scene.name.asClassName()}()\n")
    background?.let {
        builder.addCode("    background.bundle.execute()\n")
    }
    builder.addCode("    scenario.dsl()\n")
    builder.addCode("    examples_${scene.name.asClassName()}().forEach{ scenario.bundle.execute(it) }\n")
    builder.addCode("    report.${scene.featName.asProperty()}.scenarioReports.add(ScenarioReport(\"${scene.name}\", ScenarioTestStatus.PASSED))\n")
    builder.addCode("} catch(throwable : Throwable){\n")
    builder.addCode("    report.${scene.featName.asProperty()}.scenarioReports.add(ScenarioReport(\"${scene.name}\", ScenarioTestStatus.ERROR, throwable))\n")
    builder.addCode("    throw throwable\n")
    builder.addCode("} finally {\n")
    builder.addCode("    writeReport()\n")
    builder.addCode("}\n")
    return builder.build()
}


private fun generateExamplesFunction(name: String, examples: List<SceneExample>): FunSpec {
    val hashTypeName = ParameterizedTypeName.get(LinkedHashMap::class, String::class, String::class)
    val listTypeName = ParameterizedTypeName.get(ArrayList::class.asTypeName(), hashTypeName)
    val builder = FunSpec.builder("examples_$name")
    builder.returns(listTypeName)
    builder.addCode("val list = %T()\n", listTypeName)
    examples.forEachIndexed { index, it ->
        builder.addCode("val map$index = %T()\n", hashTypeName)
        it.map.entries.forEach {
            builder.addCode("    map$index.put(\"${it.key}\", \"${it.value}\")\n")
        }
        builder.addCode("list.add(map$index)\n")
    }
    builder.addCode("return list\n")
    return builder.build()
}



