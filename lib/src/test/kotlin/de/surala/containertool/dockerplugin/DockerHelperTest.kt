package de.surala.containertool.dockerplugin

import org.gradle.api.Action
import org.gradle.api.tasks.StopExecutionException
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.process.JavaExecSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class DockerHelperTest {

    @Test
    fun `test isDockerInstalled returns true`() {
        val execOperations = createExecOperations("Docker version 20.10.7","")

        // Test the DockerHelper with the custom ExecOperations
        val dockerHelper = DockerHelper(execOperations)
        assertTrue(dockerHelper.isDockerInstalled(), "Expected Docker to be installed")
    }

    @Test
    fun `test isDockerInstalled returns false when Docker is not found`() {
        val execOperations = createExecOperations("","'docker' is not recognized")

        // Test the DockerHelper with the custom ExecOperations
        val dockerHelper = DockerHelper(execOperations)
        assertFalse(dockerHelper.isDockerInstalled(), "Expected Docker not to be installed")
    }

    @Test
    fun `test getContainerStatus returns container status when container exists`() {
        val simulatedOutput = "Up 5 minutes"
        val expectedOutput = "Up 5 minutes"
        val containerName = "any-container"

        val execOperations = createExecOperations(simulatedOutput,"")
        val dockerHelper = DockerHelper(execOperations)
        val status = dockerHelper.getContainerStatus(containerName)
        assertEquals(expectedOutput, status)
    }

    @Test
    fun `test startContainer when container not found`() {
        //This is the actual response that docker gives
        val simulatedOutput =
            "docker: Error response from daemon: pull access denied for test-image, repository does not exist or may require 'docker login'."
        val execOperations = createExecOperations("",simulatedOutput)
        val dockerHelper = DockerHelper(execOperations)

        val exception = assertThrows<StopExecutionException> {
            dockerHelper.startContainer(
                DockerHelper.StartContainerConfig(
                    repositoryName = null,
                    imageName = "test-image",
                    imageTag = "latest",
                    containerName = "test-container",
                    portMap = mapOf(8080 to 80),
                    environmentMap = mapOf("ENV_VAR" to "value"),
                    volumeMap = mapOf("/host/path" to "/container/path")
                )
            )
        }

        // Verify the exception message contains specific details
        val message = exception.message ?: ""
        assertTrue(
            message.contains("test-container had issues starting"),
            "Exception message should indicate the container had issues starting. Actual message: $message"
        )
    }

    @Test
    fun `test startContainer when container exists but could not get started`() {
        val simulatedOutput = "Exited (0)"
        val execOperations = createExecOperations("",simulatedOutput)
        val dockerHelper = DockerHelper(execOperations)

        val exception = assertThrows<StopExecutionException> {
            dockerHelper.startContainer(
                DockerHelper.StartContainerConfig(
                    repositoryName = null,
                    imageName = "test-image",
                    imageTag = "latest",
                    containerName = "test-container",
                    portMap = mapOf(8080 to 80),
                    environmentMap = mapOf("ENV_VAR" to "value"),
                    volumeMap = mapOf("/host/path" to "/container/path")
                )
            )
        }

        // Verify the exception message contains specific details
        val message = exception.message ?: ""
        assertTrue(
            message.contains("test-container had issues starting"),
            "Exception message should indicate the container had issues starting. Actual message: $message"
        )
    }

    @Test
    fun `test startContainer successful`() {
        val execOperations = createExecOperations("Up","")
        val dockerHelper = DockerHelper(execOperations)

        dockerHelper.startContainer(
            DockerHelper.StartContainerConfig(
                repositoryName = null,
                imageName = "test-image",
                imageTag = "latest",
                containerName = "test-container",
                portMap = mapOf(8080 to 80),
                environmentMap = mapOf("ENV_VAR" to "value"),
                volumeMap = mapOf("/host/path" to "/container/path")
            )
        )
    }

    @Test
    fun `test stopContainer when container exists`() {
        val logs = ByteArrayOutputStream()
        System.setOut(PrintStream(logs))
        val execOperations = createExecOperations("Up 5 minutes","")
        val dockerHelper = DockerHelper(execOperations)
        // Simulate container status as existing

        dockerHelper.stopContainer("test-container")

        // Verify the log output
        val logOutput = logs.toString().trim()
        val expectedLog = """
            test-container Status: Up 5 minutes
            Stopping test-container container...
            Executing command: docker container stop test-container
            Removing test-container container...
            Executing command: docker container remove test-container
        """.trimIndent()

        // Split expected log into lines and verify each line is in the log output
        val expectedLines = expectedLog.split("\n")
        for (line in expectedLines) {
            assertTrue(
                logOutput.contains(line.trim()),
                "Expected log to contain: '$line'. Actual logs:\n$logOutput"
            )
        }
    }


    @Test
    fun `test stopContainer when container doesnt exists`() {
        val logs = ByteArrayOutputStream()
        System.setOut(PrintStream(logs))
        val execOperations = createExecOperations("","container does not exist")
        val dockerHelper = DockerHelper(execOperations)
        // Simulate container status as existing

        dockerHelper.stopContainer("test-container")

        // Verify the log output
        val logOutput = logs.toString().trim()
        val expectedLog = "container does not exist"

        // Split expected log into lines and verify each line is in the log output
        assertTrue(
            logOutput.contains(expectedLog),
            "Expected log to contain: '$expectedLog'. Actual logs:\n$logOutput"
        )
    }

    private fun createExecOperations(simulatedStandardOutput: String, simulatedErrorOutput: String): ExecOperations {
        return object : ExecOperations {
            override fun exec(action: Action<in ExecSpec>?): ExecResult {
                val execSpec = StubExecSpec()
                action?.execute(execSpec)

                // Simulate Docker output
                val standardOutputStream = execSpec.standardOutput as ByteArrayOutputStream
                val errorOutputStream = execSpec.errorOutput as ByteArrayOutputStream
                standardOutputStream.write(simulatedStandardOutput.toByteArray(Charsets.UTF_8))
                errorOutputStream.write(simulatedErrorOutput.toByteArray(Charsets.UTF_8))

                return StubExecResult(0) // Assume success
            }

            override fun javaexec(action: Action<in JavaExecSpec>?): ExecResult {
                throw UnsupportedOperationException("Not implemented for this test")
            }
        }
    }

}