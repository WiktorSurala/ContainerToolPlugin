package de.surala.containertool.dockerplugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.process.ExecOperations
import java.util.*
import javax.inject.Inject

class DockerPlugin @Inject constructor(private val execOperations: ExecOperations) : Plugin<Project> {
    override fun apply(project: Project) {
        // Check if Docker is installed
        val dockerHelper = DockerHelper(execOperations)

        // Check if Docker is installed
        if (!dockerHelper.isDockerInstalled()) {
            throw GradleException("Docker is not installed or not available in the PATH. Please install Docker to use this plugin.")
        }

        // Register the Docker extension for the DSL
        val extension = project.extensions.create("docker", DockerExtension::class.java)

        // Utility function to capitalize the first character
        fun String.capitalizeFirst(): String {
            return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }

        // After evaluating the project, register tasks based on the extension configuration
        project.afterEvaluate {
            val mainGroupName = extension.mainGroupName

            extension.containers.forEach { config ->
                val stopTask = "stop${config.name.capitalizeFirst()}"
                val startTask = "start${config.name.capitalizeFirst()}"
                val restartTask = "restart${config.name.capitalizeFirst()}"
                val taskGroup = "${mainGroupName}${config.group?.let { " -> $it" } ?: ""}"

                // Register the start task
                project.tasks.register(startTask, StartDockerContainer::class.java) { task ->
                    task.containerName.set(config.name)
                    task.imageName.set(config.image)
                    task.imageTag.set(config.tag)
                    task.portMap.set(config.ports)
                    task.environmentMap.set(config.environments)
                    task.group = taskGroup
                    task.volumeMap.set(config.volumes)
                    task.description = "Starts the ${config.name} Docker container"
                }

                // Register the stop task
                project.tasks.register(stopTask, StopDockerContainer::class.java) { task ->
                    task.containerName.set(config.name)
                    task.group = taskGroup
                    task.description = "Stops the ${config.name} Docker container"
                }

                // Register the restart task
                project.tasks.register(restartTask) { task ->
                    // Configure the dependencies
                    task.dependsOn(stopTask, startTask)
                    task.group = taskGroup
                    task.description = "Restarts the ${config.name} Docker container"
                }

                // Ensure stop always runs before start
                project.tasks.named(startTask) {
                    it.mustRunAfter(stopTask)
                }
            }
        }
    }

}
