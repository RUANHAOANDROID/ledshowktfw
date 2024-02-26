package com.uchi.db

import io.ktor.server.http.content.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import org.jetbrains.exposed.sql.*
internal object DaoImpl :Dao {

    override suspend fun setup() {

    }
}