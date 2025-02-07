package com.example

import com.example.module.configureDatabases
import com.example.module.configureHTTP
import com.example.module.configureRouting
import com.example.module.configureSecurity
import com.example.module.configureSerialization
import io.ktor.server.application.Application

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureRouting()
}
