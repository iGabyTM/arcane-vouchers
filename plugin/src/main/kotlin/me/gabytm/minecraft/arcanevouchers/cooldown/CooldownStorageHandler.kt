package me.gabytm.minecraft.arcanevouchers.cooldown

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.minecraft.arcanevouchers.functions.info
import me.gabytm.minecraft.arcanevouchers.sql.SqlQuery
import me.gabytm.minecraft.arcanevouchers.sql.Storage
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

class CooldownStorageHandler(plugin: ArcaneVouchers) : Storage<CooldownStorageHandler.Query>(
    plugin, "/storage/cooldowns.sql.db", setOf(Query.CREATE_TABLE)
) {

    fun loadCooldowns(): Table<UUID, String, Long> {
        val table = HashBasedTable.create<UUID, String, Long>()

        if (!connected) {
            return table
        }

        try {
            val deletedCooldowns = Query.DELETE_EXPIRED_COOLDOWNS.prepare(connection).apply {
                setLong(1, System.currentTimeMillis())
            }.executeUpdate()
            info("Deleted $deletedCooldowns expired cooldowns")
        } catch (e: SQLException) {
            exception("Could not delete expired cooldowns", e)
        }

        try {
            with (Query.SELECT_COOLDOWNS.prepare(connection).executeQuery()) {
                while (next()) {
                    table.put(
                        UUID.fromString(getString("uuid")),
                        getString("voucher"),
                        getLong("expiration")
                    )
                }
            }
        } catch (e: SQLException) {
            exception("Could not load the cooldowns", e)
        }

        return table
    }

    fun insertCooldown(player: UUID, voucher: String, expiration: Long) {
        if (!connected) {
            return
        }

        try {
            Query.INSERT_COOLDOWN.prepare(connection).apply {
                val uuid = player.toString()

                setString(1, uuid)
                setString(2, voucher)
                setString(3, uuid)
                setString(4, voucher)
                setLong(5, expiration)
            }.executeUpdate()
        } catch (e: SQLException) {
            exception("Could not insert $player's cooldown for voucher $voucher", e)
        }
    }

    enum class Query(private val query: String) : SqlQuery {

        CREATE_TABLE(
            """
               CREATE TABLE IF NOT EXISTS `cooldowns` (
                   id INTEGER PRIMARY KEY,
                   uuid VARCHAR(36),
                   voucher VARCHAR(128),
                   expiration INTEGER
               ); 
            """.trimIndent()
        ),

        INSERT_COOLDOWN(
            """
                REPLACE INTO `cooldowns` (id, uuid, voucher, expiration) 
                VALUES (
                    (SELECT id FROM `cooldowns` WHERE uuid = ? AND voucher = ?),
                    ?,
                    ?,
                    ?
                );
            """.trimIndent()
        ),

        SELECT_COOLDOWNS("SELECT uuid, voucher, expiration FROM `cooldowns`;"),

        DELETE_EXPIRED_COOLDOWNS("DELETE FROM `cooldowns` WHERE expiration <= ?;")
        ;

        override fun prepare(connection: Connection): PreparedStatement {
            return connection.prepareStatement(this.query)
        }

    }

}