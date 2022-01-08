package me.gabytm.minecraft.arcanevouchers.sql

import me.gabytm.minecraft.arcanevouchers.ArcaneVouchers
import me.gabytm.minecraft.arcanevouchers.functions.exception
import java.io.File
import java.io.IOException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

abstract class Storage<Q: SqlQuery>(
    protected val plugin: ArcaneVouchers,
    databasePath: String,
    private val tables: Set<Q>
) {

    private val databaseFile = File(plugin.dataFolder, databasePath)

    protected lateinit var connection: Connection
    protected var connected: Boolean = false

    init {
        if (connect()) {
            createTables()
        }
    }

    private fun connect(): Boolean {
        if (!databaseFile.exists()) {
            databaseFile.parentFile.mkdirs()

            try {
                databaseFile.createNewFile()
            } catch (e: IOException) {
                exception("Could not create $databaseFile", e)
                return false
            }
        }

        try {
            Class.forName("org.sqlite.JDBC")
            this.connection = DriverManager.getConnection("jdbc:sqlite:$databaseFile")
        } catch (e: ClassNotFoundException) {
            exception("Could not find class org.sqlite.JDBC", e)
            return false
        } catch (e: SQLException) {
            exception("Could not establish database connection", e)
            return false
        }

        this.connected = true
        return true
    }

    private fun createTables() {
        try {
            this.tables.forEach { it.prepare(this.connection).execute() }
        } catch (e: IOException) {
            exception("Encountered an error while creating table(s)", e)
        }
    }

}