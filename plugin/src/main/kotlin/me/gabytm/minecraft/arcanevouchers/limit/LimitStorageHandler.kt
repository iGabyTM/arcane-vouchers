package me.gabytm.minecraft.arcanevouchers.limit

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.functions.error
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*

class LimitStorageHandler(plugin: ArcaneVouchers) {

    private val databaseFile = File(plugin.dataFolder, "limits.sql.db")
    private var connected = false
    private lateinit var connection: Connection

    init {
        if (connect()) {
            createTables()
        }
    }

    private fun connect(): Boolean {
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile()
            } catch (e: IOException) {
                error("Could not create ${databaseFile.path}", e)
                return false
            }
        }

        try {
            Class.forName("org.sqlite.JDBC")
            this.connection = DriverManager.getConnection("jdbc:sqlite:${databaseFile.path}")
        } catch (e: ClassNotFoundException) {
            error("Could not find class org.sqlite.JDBC", e)
            return false
        } catch (e: SQLException) {
            error("Could not establish database connection", e)
            return false
        }

        this.connected = true
        return true
    }

    private fun createTables() {
        try {
            Query.CREATE_GLOBAL_TABLE.prepare(connection).executeUpdate()
            Query.CREATE_PERSONAL_TABLE.prepare(connection).executeUpdate()
        } catch (e: SQLException) {
            error("Could not create tables", e)
        }
    }

    fun loadGlobalLimits(): Map<String, Long> {
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
            error("Could not load global limits", e)
        }

        return limits
    }

    fun loadPersonalLimits(): Table<UUID, String, Long> {
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
            error("Could not load personal limits", e)
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
            error("Could not update the global limit for voucher $voucher ($limit)", e)
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
            error("Could not update $uuid's limit for voucher $voucher ($limit)", e)
        }
    }

    private enum class Query(private val query: String) {

        CREATE_GLOBAL_TABLE(
            """
                CREATE TABLE IF NOT EXISTS `global_limits` (
                    id INTEGER PRIMARY KEY,
                    voucher VARCHAR(128),
                    usages INTEGER
                );
            """.trimIndent()
        ),

        CREATE_PERSONAL_TABLE(
            """
                CREATE TABLE IF NOT EXISTS `personal_limits` (
                    id INTEGER PRIMARY KEY,
                    uuid VARCHAR(36),
                    voucher VARCHAR(128),
                    usages INTEGER
                );
            """.trimIndent()
        ),

        INSERT_GLOBAL(
            """
                REPLACE INTO `global_limits` (id, voucher, usages) 
                VALUES (
                    (SELECT id FROM `global_limits` WHERE voucher = ?),
                    ?,
                    ?
                );
            """.trimIndent()
        ),

        INSERT_PERSONAL(
            """
                REPLACE INTO `personal_limits` (id, uuid, voucher, usages) 
                VALUES (
                    (SELECT id FROM `personal_limits` WHERE uuid = ? AND voucher = ?),
                    ?,
                    ?,
                    ?
                );
            """.trimIndent()
        ),

        SELECT_GLOBAL("SELECT voucher, usages FROM `global_limits`;"),

        SELECT_PERSONAL("SELECT uuid, voucher, usages FROM `personal_limits`;");

        fun prepare(connection: Connection): PreparedStatement {
            return connection.prepareStatement(this.query)
        }

    }

}