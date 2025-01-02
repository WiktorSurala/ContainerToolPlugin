package de.surala.containertool.dockerplugin

class ContainerConfig {
    lateinit var name: String // The name of the Docker container
    lateinit var image: String // The Docker image to use
    var tag: String = "latest" // The tag of the Docker image (default: "latest")

    // Mappings for ports, environment variables, and volumes
    val ports = mutableMapOf<Int, Int>()
    val environments = mutableMapOf<String, String>()
    val volumes = mutableMapOf<String, String>()
    var group: String? = null // Optional subgroup name
}
