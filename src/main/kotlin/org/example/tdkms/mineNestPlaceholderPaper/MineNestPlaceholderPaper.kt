package org.example.tdkms.mineNestPlaceholderPaper

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.nio.charset.StandardCharsets

class MineNestPlaceholderPaper : JavaPlugin(), PluginMessageListener {

    private val channel = "mynest:survival"
    private val survivalPrefix = "survival:".toByteArray(StandardCharsets.UTF_8)
    private var lastCountSurvival = 0

    override fun onEnable() {
//        logger.info("=== Plugin Enabling ===")
//        logger.info("Registering incoming channel: $channel")
        server.messenger.registerIncomingPluginChannel(this, channel, this)
//        logger.info("=== Plugin Enabled Successfully ===")
    }

    override fun onDisable() {
//        logger.info("Unregistering plugin channel")
        server.messenger.unregisterIncomingPluginChannel(this, channel)
    }

    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
//        logger.info("\n=== NEW MESSAGE RECEIVED ===")
//        logger.info("Channel: '$channel'")
//        logger.info("Sender: ${player.name}")
//        logger.info("Raw message size: ${message.size} bytes")
//        logger.info("Hex dump: ${bytesToHex(message)}")
//        logger.info("String representation: '${String(message, StandardCharsets.UTF_8)}'")

        // Channel verification
        if (channel != this.channel) {
            logger.info("❌ Rejected: Channel mismatch (expected '${this.channel}')")
            return
        }

        // Minimum length check
        if (message.size < survivalPrefix.size) {
            logger.info("❌ Rejected: Message too short (${message.size} < ${survivalPrefix.size})")
            return
        }

        // Prefix verification
        //logger.info("Checking prefix match...")
        for (i in survivalPrefix.indices) {
            val expected = survivalPrefix[i]
            val actual = message[i]
            if (actual != expected) {
                logger.info("❌ Prefix mismatch at byte $i (expected $expected, got $actual)")
                return
            }
        }
        //logger.info("✅ Prefix verified")

        // Count parsing
        //logger.info("Parsing count...")
        var count = 0
        val digits = StringBuilder()
        for (i in survivalPrefix.size until message.size) {
            val c = message[i].toInt().toChar()
            when {
                c in '0'..'9' -> {
                    count = count * 10 + (c - '0')
                    digits.append(c)
                }
                else -> {
                    logger.info("Non-digit character '${c}' (${c.code}) at position $i, stopping parse")
                    break
                }
            }
        }

//        logger.info("Digits found: '$digits'")
//        logger.info("✅ Final count: $count")
//        logger.info("=== MESSAGE PROCESSING COMPLETE ===\n")

        // Update hologram/scoreboard
        if (lastCountSurvival != count) {
            lastCountSurvival = count
            updateHologram(count)
        }
    }

    private fun updateHologram(count: Int) {
        try {
            val console = Bukkit.getServer().consoleSender

            val command = "hologram edit survival setLine 6 <bold><gradient:#1FAB0D:#7CFC00>online:</gradient> <white>$count</white></bold>"
            Bukkit.dispatchCommand(console, command)

            //logger.info("Updated hologram to show $count players")
        } catch (e: Exception) {
            //logger.info("Failed to update hologram, ${e.message}")
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 3)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 3] = HEX_ARRAY[v ushr 4]
            hexChars[i * 3 + 1] = HEX_ARRAY[v and 0x0F]
            hexChars[i * 3 + 2] = ' '
        }
        return String(hexChars)
    }

    companion object {
        private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()
    }
}