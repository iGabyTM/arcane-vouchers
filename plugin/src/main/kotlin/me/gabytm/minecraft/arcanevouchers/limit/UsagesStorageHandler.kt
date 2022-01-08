package me.gabytm.minecraft.arcanevouchers.limit

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.functions.exception
import me.gabytm.minecraft.arcanevouchers.sql.SqlQuery
import me.gabytm.minecraft.arcanevouchers.sql.Storage
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

class UsagesStorageHandler(plugin: ArcaneVouchers) : Storage<UsagesStorageHandler.Query>(
    plugin, "/storage/usages.sql.db", setOf(Query.CREATE_GLOBAL_TABLE, Query.CREATE_PERSONAL_TABLE)
) {

    fun loadGlobalUsages(): Map<String, Long> {
        if (!connected) {
            return emptyMap()
        }

        val limits = mutableMapOf<String, Long>()

        try {
            with (Query.SELECT_GLOBAL.prepare(connection).executeQuery()) {
                while (next()) {
                    limits[getString("voucher")] = getLong("usages")
                }
            }
        } catch (e: SQLException) {
            exception("Could not load global limits", e)
        }

        return limits
    }

    fun loadPersonalUsages(): Table<UUID, String, Long> {
        val limits = HashBasedTable.create<UUID, String, Long>()

        if (!connected) {
            return limits
        }

        try {
            with (Query.SELECT_PERSONAL.prepare(connection).executeQuery()) {
                while (next()) {
                    limits.put(
                        UUID.fromString(getString("uuid")),
                        getString("voucher"),
                        getLong("usages")
                    )
                }
            }
        } catch (e: SQLException) {
            exception("Could not load personal limits", e)
        }

        return limits
    }

    fun updateGlobalUsages(voucher: String, limit: Long) {
        if (!connected) {
            return
        }

        try {
            Query.INSERT_GLOBAL.prepare(connection).apply {
                setString(1, voucher)
                setString(2, voucher)
                setLong(3, limit)
            }.executeUpdate()
        } catch (e: SQLException) {
            exception("Could not update the global limit for voucher $voucher ($limit)", e)
        }
    }

    fun updatePersonalUsages(player: UUID, voucher: String, limit: Long) {
        if (!connected) {
            return
        }

        val uuid = player.toString()

        try {
            Query.INSERT_PERSONAL.prepare(connection).apply {
                setString(1, uuid)
                setString(2, voucher)
                setString(3, uuid)
                setString(4, voucher)
                setLong(5, limit)
            }.executeUpdate()
        } catch (e: SQLException) {
            exception("Could not update $uuid's limit for voucher $voucher ($limit)", e)
        }
    }

    enum class Query(private val query: String) : SqlQuery {

        CREATE_GLOBAL_TABLE(
            """
                CREATE TABLE IF NOT EXISTS `global_usages` (
                    id INTEGER PRIMARY KEY,
                    voucher VARCHAR(128),
                    usages INTEGER
                );
            """.trimIndent()
        ),

        CREATE_PERSONAL_TABLE(
            """
                CREATE TABLE IF NOT EXISTS `personal_usages` (
                    id INTEGER PRIMARY KEY,
                    uuid VARCHAR(36),
                    voucher VARCHAR(128),
                    usages INTEGER
                );
            """.trimIndent()
        ),

        INSERT_GLOBAL(
            """
                REPLACE INTO `global_usages` (id, voucher, usages) 
                VALUES (
                    (SELECT id FROM `global_usages` WHERE voucher = ?),
                    ?,
                    ?
                );
            """.trimIndent()
        ),

        INSERT_PERSONAL(
            """
                REPLACE INTO `personal_usages` (id, uuid, voucher, usages) 
                VALUES (
                    (SELECT id FROM `personal_limits` WHERE uuid = ? AND voucher = ?),
                    ?,
                    ?,
                    ?
                );
            """.trimIndent()
        ),

        SELECT_GLOBAL("SELECT voucher, usages FROM `global_usages`;"),

        SELECT_PERSONAL("SELECT uuid, voucher, usages FROM `personal_usages`;");

        override fun prepare(connection: Connection): PreparedStatement {
            return connection.prepareStatement(this.query)
        }

    }

}