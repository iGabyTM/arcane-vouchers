package me.gabytm.minecraft.arcanevouchers.sql

import java.sql.Connection
import java.sql.PreparedStatement

interface SqlQuery {

    fun prepare(connection: Connection): PreparedStatement

}