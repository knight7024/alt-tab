package com.example.config

data class MongoConfig(
    val uri: String,
    val database: String,
    val collection: String,
)
