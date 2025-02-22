package com.example

import com.example.adapter.GoogleClient
import com.example.adapter.MongoUserRepository
import com.example.domain.UserEmailRepository
import com.example.domain.UserRepository
import com.example.module.configureHTTP
import com.example.module.configureRouting
import com.example.module.configureSecurity
import com.example.module.configureSerialization
import com.example.module.userDao
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.tryGetString
import java.time.Clock

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

internal fun Application.module() {
    // dependency
    val userEmailRepository: UserEmailRepository = GoogleClient(config.tryGetString("google.baseUrl")!!)
    val userRepository: UserRepository = MongoUserRepository(userDao())
    val clock = Clock.systemDefaultZone()

    // configure
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureRouting(
        userEmailRepository = userEmailRepository,
        userRepository = userRepository,
        clock = clock
    )
}

internal val config = ApplicationConfig("application.conf")
internal val secretConfig = ApplicationConfig("secrets.conf")
