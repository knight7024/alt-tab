package com.example.module

import com.example.adapter.UserDocument
import com.example.secretConfig
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.config.tryGetString

internal fun Application.userDao(): MongoCollection<UserDocument> {
    val uri = secretConfig.tryGetString("mongodb-user.uri")!!
    val databaseName = secretConfig.tryGetString("mongodb-user.database")!!
    val collectionName = secretConfig.tryGetString("mongodb-user.collection")!!

    val mongoClient =
        MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(uri))
            .build()
            .let { MongoClients.create(it) }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return mongoClient
        .getDatabase(databaseName)
        .getCollection(collectionName, UserDocument::class.java)
}
