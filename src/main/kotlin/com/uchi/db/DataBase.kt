package com.uchi.db

import data.db.entity.CountTable
import data.db.entity.MaxCountTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DataBase {
    private val db by lazy {
        val file = File("data.db")
        if (!file.exists()) {
            file.createNewFile()
            javaClass.getResourceAsStream("data.db")?.let {
                it.copyTo(file.outputStream())
            }
        }
        val db = Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")

        transaction(db) {
            addLogger(StdOutSqlLogger)
            if (!SchemaUtils.checkCycle(CountTable)) {
                SchemaUtils.create(CountTable)
            }
            if (!SchemaUtils.checkCycle(MaxCountTable)) {
                SchemaUtils.create(MaxCountTable)
            }
        }
        db
    }

    init {
        println(db.name)
    }
    val dao: Dao = DaoImpl
}