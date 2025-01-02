package de.surala.containertool.dockerplugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class DockerPluginIntegrationTest {

    @TempDir
    lateinit var tempProjectDir: File

    lateinit var buildFile: File

    @Test
    fun `test startDockerContainer and stopDockerContainer execute in order`() {
        // Create a temporary Gradle build file
        buildFile = File(tempProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
            plugins {
                id("de.surala.containertool.docker-plugin")
            }

            docker {
                container {
                    name = "testNginx"
                    image = "nginx"
                    tag = "latest"
                    ports[8888] = 80
                }
            }
            """
        )

        // Start and stop the container in a single Gradle invocation
        val resultStart = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments("startTestNginx")
            .withPluginClasspath()
            .build()

        val outputStart = resultStart.output
        println(outputStart)
        assertTrue(outputStart.contains("testNginx started successfully"))

        val resultStop = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments("stopTestNginx")
            .withPluginClasspath()
            .build()

        val outputStop = resultStop.output
        println(outputStop)
        assertTrue(outputStop.contains("Stopping testNginx container"))

    }

    @Test
    fun `test startDockerContainer with nonexistent image`() {
        // Create a temporary Gradle build file
        buildFile = File(tempProjectDir, "build.gradle.kts")
        buildFile.writeText(
            """
        plugins {
            id("de.surala.containertool.docker-plugin")
        }

        docker {
            container {
                name = "invalidImageContainer"
                image = "nonexistent-image"
                tag = "latest"
                ports[8080] = 80
            }
        }
        """
        )

        // Attempt to start the container
        val result = GradleRunner.create()
            .withProjectDir(tempProjectDir)
            .withArguments("startInvalidImageContainer")
            .withPluginClasspath()
            .build() // Expect failure because the image does not exist

        val output = result.output
        println(output)
        // Verify that the error message is logged
        assertTrue(output.contains("The Docker image nonexistent-image:latest may not exist"), "Expected hint about non existing image")
        assertTrue(output.contains("invalidImageContainer had issues starting"), "Expected message about container startup issues")
    }

}
