package de.surala.containertool.dockerplugin

import org.gradle.api.tasks.StopExecutionException
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream

class DockerHelper(private val execOperations: ExecOperations) {
    fun getContainerStatus(containerName: String): String {
        return execCommand(
            listOf(
                "docker",
                "container",
                "ps",
                "--all",
                "--filter", "name=$containerName",
                "--format", "{{.Status}}"
            )
        )
    }

    data class StartContainerConfig(
        val repositoryName: String? = null,
        val imageName: String,
        val imageTag: String = "latest",
        val containerName: String,
        val portMap: Map<Int, Int> = emptyMap(),
        val environmentMap: Map<String, String?> = emptyMap(),
        val volumeMap: Map<String, String> = emptyMap()
    )


    fun startContainer(
        startContainerConfig: StartContainerConfig
    ) {
        val defaultWorkingDir = System.getProperty("user.dir")
        println("Default Working Directory: $defaultWorkingDir")
        val imageReference = getImageName(startContainerConfig)
        val containerStatus = getContainerStatus(startContainerConfig.containerName)
        if (containerStatus.isEmpty()) {
            println("${startContainerConfig.containerName} container not found. Starting a new container...")
            val commandLineArguments = mutableListOf(
                "docker", "run", "-d",
                "--name", startContainerConfig.containerName
            )

            for ((key, value) in startContainerConfig.portMap) {
                commandLineArguments.add("-p")
                commandLineArguments.add("$key:$value")
            }

            for ((key, value) in startContainerConfig.environmentMap) {
                commandLineArguments.add("-e")
                commandLineArguments.add("$key=$value")
            }

            for ((key, value) in startContainerConfig.volumeMap) {
                commandLineArguments.add("-v")
                commandLineArguments.add("$key:$value")
            }

            commandLineArguments.add(imageReference)

            execStartContainer(startContainerConfig, commandLineArguments)
        } else if (containerStatus.contains("Exited")) {
            println("${startContainerConfig.containerName} container exists but is not running. Starting the container...")
            execStartContainer(
                startContainerConfig,
                listOf("docker", "container", "start", startContainerConfig.containerName)
            )
        } else {
            println("${startContainerConfig.containerName} container is already running.")
        }

        // Verify if Container started successfully
        val checkContainerStatus = getContainerStatus(startContainerConfig.containerName)
        if (checkContainerStatus.contains("Up")) {
            println("${startContainerConfig.containerName} started successfully.")
        } else {
            throwContainerStartupException(startContainerConfig, checkContainerStatus)
        }
    }

    private fun execStartContainer(startContainerConfig: StartContainerConfig, commandList: List<String>) {
        try {
            execCommand(commandList)
        } catch (e: CommandExecutionException) {
            throwContainerStartupException(startContainerConfig, e.message!!)
        }
    }

    private fun throwContainerStartupException(startContainerConfig: StartContainerConfig, exceptionMessage: String) {
        val hint = if (exceptionMessage.contains("pull access denied")) {
            "Hint: The Docker image ${getImageName(startContainerConfig)} may not exist, or you may need to log in to a Docker registry."
        } else {
            ""
        }

        val detailedMessage = buildString {
            appendLine("${startContainerConfig.containerName} had issues starting:")
            appendLine("Attempted image: $${getImageName(startContainerConfig)}")
            appendLine("Ports: ${startContainerConfig.portMap}")
            appendLine("Environment variables: ${startContainerConfig.environmentMap}")
            appendLine("Volumes: ${startContainerConfig.volumeMap}")
            appendLine(hint)
            appendLine("Command Output: $exceptionMessage")
        }
        println(detailedMessage)
        throw ContainerStartupException(detailedMessage)
    }

    fun stopContainer(containerName: String) {
        val exists = getContainerStatus(containerName)
        println("$containerName Status: ${exists.trim()}")
        if (exists.isNotEmpty()) {
            println("Stopping $containerName container...")
            execCommand(listOf("docker", "container", "stop", containerName))
            println("Removing $containerName container...")
            execCommand(listOf("docker", "container", "remove", containerName))
        } else {
            println("$containerName container does not exist.")
        }
    }

    fun isDockerInstalled(): Boolean {
        return try {
            val result = execCommand(listOf("docker", "--version"))
            println("Docker version: $result")
            result.isNotEmpty() && result.contains("Docker version")
        } catch (e: Exception) {
            println("Docker version error: ${e.message}")
            false
        }
    }

    private fun getImageName(startContainerConfig: StartContainerConfig): String {
        return if (startContainerConfig.repositoryName.isNullOrBlank()) {
            "${startContainerConfig.imageName}:${startContainerConfig.imageTag}"
        } else {
            "${startContainerConfig.repositoryName}/${startContainerConfig.imageName}:${startContainerConfig.imageTag}"
        }
    }

    private fun execCommand(commandList: List<String>): String {
        println("Executing command: ${commandList.joinToString(" ")}")
        val standardOutputStream = ByteArrayOutputStream()
        val errorOutputStream = ByteArrayOutputStream()
        try {
            execOperations.exec { operation ->
                operation.commandLine = commandList
                operation.standardOutput = standardOutputStream
                operation.errorOutput = errorOutputStream
            }
            return standardOutputStream.toString(Charsets.UTF_8).trim()
        } catch (e: Exception) {
            print("Exception e: ${e.message}")
            throw CommandExecutionException(errorOutputStream.toString(Charsets.UTF_8))
        }
    }
}

class CommandExecutionException(message: String) : StopExecutionException(message)
class ContainerStartupException(message: String) : StopExecutionException(message)