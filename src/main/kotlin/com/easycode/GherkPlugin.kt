package com.easycode

import com.android.build.gradle.AppExtension
import com.easycode.generation.generateRecursive
import com.easycode.generation.generateReportClasses
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File


open class GherkPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.add("gherk", GherkExtension(project))
        val generationTask = project.tasks.create("generateGherkClasses", GenerationTask::class.java)
        project.plugins.withId("android") {
            addSourceDirectory(project, project.logger)
        }

        project.tasks.all {
            if (it.name.contains("generate", true) && it.name.contains("AndroidTestResources", true)) {
                it.dependsOn(generationTask)
            }
        }
        project.dependencies.add("androidTestCompile", "com.google.code.gson:gson:2.8.2")
    }

    private fun addSourceDirectory(project: Project, logger: Logger) {
        logger.log(LogLevel.DEBUG, "[GHERK] Adding generated path Source Set")
        project.plugins.findPlugin("android") ?: project.plugins.findPlugin("android-library") ?: return
        logger.log(LogLevel.DEBUG, "[GHERK] Android plugin found!!")
        val appExtension = project.properties["android"] as AppExtension
        val sourceSet = appExtension.sourceSets.first { it.name == "androidTest" }
        val gherkExtension = project.extensions.getByName("gherk") as GherkExtension
        sourceSet.java.srcDirs(gherkExtension.outputDirectory)
    }

}

open class GenerationTask : DefaultTask() {

    @OutputDirectory
    var outputDirectory: File = File(project.buildDir, "generated/gherk/src/")

    @InputDirectory
    var featureDirectory: File = File(project.projectDir, "src/androidTest/assets")

    @Input
    var mobileExternalStorageReportFolder: String = "gherk-report/"

    @TaskAction
    fun createGherkClasses() {
        logger.log(LogLevel.DEBUG, "[GHERK] Creating gherk Classes")
        logger.log(LogLevel.DEBUG, "[GHERK] *** output -> ${outputDirectory.path}")
        logger.log(LogLevel.DEBUG, "[GHERK] *** input -> ${featureDirectory.path}")
        val gherkExtension = project.extensions.getByName("gherk") as GherkExtension
        outputDirectory = gherkExtension.outputDirectory
        featureDirectory = gherkExtension.featureDirectory
        mobileExternalStorageReportFolder = gherkExtension.mobileExternalStorageReportFolder
        if (featureDirectory.exists()) {
            cleanRecursively(outputDirectory)
            generateRecursive(featureDirectory, outputDirectory, logger)
            generateReportClasses(outputDirectory, mobileExternalStorageReportFolder, logger)
        }
    }

    private fun cleanRecursively(outputDirectory: File) {
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()
    }

}


class GherkExtension(project: Project) {

    val outputDirectory: File = File(project.buildDir, "generated/gherk/src/")
    val featureDirectory: File = File(project.projectDir, "src/androidTest/assets")
    val mobileExternalStorageReportFolder: String = "gherk-report/"

}
