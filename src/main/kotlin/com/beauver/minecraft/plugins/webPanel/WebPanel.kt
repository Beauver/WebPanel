package com.beauver.minecraft.plugins.webPanel

import co.aikar.commands.PaperCommandManager
import com.beauver.minecraft.plugins.webPanel.Website.PanelWebsite
import org.bukkit.plugin.java.JavaPlugin


class WebPanel : JavaPlugin() {
    
    private lateinit var manager: PaperCommandManager
    
    override fun onEnable() {
        plugin = this
        manager = PaperCommandManager(this)

        logger.info("|---------[ WebPanel ]---------|")
        logger.info("|                              |")

        saveDefaultConfig()

        // Plugin startup logic
        registerCommands()
        registerEvents()
        registerWebsites()

        logger.info("|                              |")
        logger.info("|---[ Enabled Successfully ]---|")
    }

    override fun onDisable() {
        logger.info("|--------[ Web Panel ]--------|")
        logger.info("|                             |")

        logger.info("|                             |")
        logger.info("|--[ Disabled Successfully ]--|")
    }

    private fun registerCommands(){
        logger.info("| Registered Commands          |")
    }

    private fun registerEvents(){
        logger.info("| Registered Listeners         |")
    }

    private fun registerWebsites(){

        Thread {
            PanelWebsite();
        }.start()

        logger.info("| Registered Website           |")
    }

    companion object {
        lateinit var plugin: WebPanel
    }
}
