package io.github.yuange86.heavenworld.data.internal

import io.github.yuange86.heavenworld.data.HWPlayer
import io.github.yuange86.heavenworld.objectives.RelativeDirectories
import io.github.yuange86.heavenworld.plugin.HWPlugin

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

import java.io.File

/**
 * tested: 0.2.0
 * @author yuange86
 * @since 0.1.0
 */
@Suppress("unused")
class HWPlayerImpl(
    override val player: Player,
    patch: Boolean = false,
    override val defaultRelativeDirectoryPath: String = RelativeDirectories.PLAYER_DIR
) : HWPlayer {

    private var status: HashMap<String, Any> = HashMap()

    init {
        if(patch) {
            patch(File(defaultRelativeDirectoryPath, "${player.uniqueId}.yml"))
        }
    }

    override fun patch(yamlFile: File) {
        if(yamlFile.parent != File(defaultRelativeDirectoryPath).parent)
            HWPlugin.instance.logger.warning("HWPlayer[name: ${player.name}] has unique Directory Path!")
        yamlFile.let { YamlConfiguration.loadConfiguration(yamlFile) }
            .getConfigurationSection("user.status")?.run {
                status.clear()
                status.putAll(getValues(true))
            } ?: HWPlugin.instance.logger.info("new Player[name: ${player.name}]")
    }

    override fun storage(yamlFile: File) {
        yamlFile.createNewFile()
        yamlFile.let { YamlConfiguration.loadConfiguration(yamlFile) }
            .let {
                it.createSection("player").run {
                    set("name", player.name)
                    set("uuid", player.uniqueId.toString())
                    createSection("status", status)
                }
                it.save(yamlFile)
            }
    }


    @Suppress("UNCHECKED_CAST")
    override fun <T: Any> getStatus(key: String) : T = status[key] as T

    override fun <T: Any> setStatus(key: String, value: T) {
        status[key] = value
    }


}