package de.surala.containertool.dockerplugin

open class DockerExtension {
    val containers = mutableListOf<ContainerConfig>()

    var mainGroupName: String = "Docker" // Default group name

    /**
     * Adds a container configuration block to the extension.
     */
    fun container(action: ContainerConfig.() -> Unit) {
        containers.add(ContainerConfig().apply(action))
    }
}
