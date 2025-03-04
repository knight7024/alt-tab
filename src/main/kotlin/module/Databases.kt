package com.example.module

import com.example.adapter.RefreshTokenDocument
import com.example.adapter.UserDocument
import com.example.config.MongoConfig
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped

internal fun Application.userDao(mongoConfig: MongoConfig): MongoCollection<UserDocument> {
    val mongoClient =
        MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(mongoConfig.uri))
            .build()
            .let { MongoClients.create(it) }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return mongoClient
        .getDatabase(mongoConfig.database)
        .getCollection(mongoConfig.collection, UserDocument::class.java)
}

internal fun Application.refreshTokenDao(mongoConfig: MongoConfig): MongoCollection<RefreshTokenDocument> {
    val mongoClient =
        MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(mongoConfig.uri))
            .build()
            .let { MongoClients.create(it) }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return mongoClient
        .getDatabase(mongoConfig.database)
        .getCollection(mongoConfig.collection, RefreshTokenDocument::class.java)
}
