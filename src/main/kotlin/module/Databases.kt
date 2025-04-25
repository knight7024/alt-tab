package com.example.module

import com.example.adapter.mongodb.CounterDocument
import com.example.adapter.mongodb.RefreshTokenDocument
import com.example.adapter.mongodb.StashSettingDocument
import com.example.adapter.mongodb.TabGroupDocument
import com.example.adapter.mongodb.UserDocument
import com.example.config.MongoConfig
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import java.util.concurrent.TimeUnit

internal fun Application.userDao(mongoConfig: MongoConfig): MongoCollection<UserDocument> {
    val mongoClient =
        MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(mongoConfig.uri))
            .applyToConnectionPoolSettings {
                it.maxWaitTime(5, TimeUnit.SECONDS)
                it.maxConnectionIdleTime(10, TimeUnit.SECONDS)
            }.build()
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
            .applyToConnectionPoolSettings {
                it.maxWaitTime(5, TimeUnit.SECONDS)
                it.maxConnectionIdleTime(10, TimeUnit.SECONDS)
            }.build()
            .let { MongoClients.create(it) }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return mongoClient
        .getDatabase(mongoConfig.database)
        .getCollection(mongoConfig.collection, RefreshTokenDocument::class.java)
}

internal fun Application.tabGroupDao(mongoConfig: MongoConfig): MongoCollection<TabGroupDocument> {
    val mongoClient =
        MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(mongoConfig.uri))
            .applyToConnectionPoolSettings {
                it.maxWaitTime(5, TimeUnit.SECONDS)
                it.maxConnectionIdleTime(10, TimeUnit.SECONDS)
            }.build()
            .let { MongoClients.create(it) }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return mongoClient
        .getDatabase(mongoConfig.database)
        .getCollection(mongoConfig.collection, TabGroupDocument::class.java)
}

internal fun Application.stashSettingDao(mongoConfig: MongoConfig): MongoCollection<StashSettingDocument> {
    val mongoClient =
        MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(mongoConfig.uri))
            .applyToConnectionPoolSettings {
                it.maxWaitTime(5, TimeUnit.SECONDS)
                it.maxConnectionIdleTime(10, TimeUnit.SECONDS)
            }.build()
            .let { MongoClients.create(it) }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return mongoClient
        .getDatabase(mongoConfig.database)
        .getCollection(mongoConfig.collection, StashSettingDocument::class.java)
}

internal fun Application.counterDao(mongoConfig: MongoConfig): MongoCollection<CounterDocument> {
    val mongoClient =
        MongoClientSettings
            .builder()
            .applyConnectionString(ConnectionString(mongoConfig.uri))
            .applyToConnectionPoolSettings {
                it.maxWaitTime(5, TimeUnit.SECONDS)
                it.maxConnectionIdleTime(10, TimeUnit.SECONDS)
            }.build()
            .let { MongoClients.create(it) }

    monitor.subscribe(ApplicationStopped) {
        mongoClient.close()
    }

    return mongoClient
        .getDatabase(mongoConfig.database)
        .getCollection(mongoConfig.collection, CounterDocument::class.java)
}
