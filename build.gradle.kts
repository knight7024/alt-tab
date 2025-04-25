val logback_version: String by project
val mongo_version: String by project
val kotest_version: String by project
val arrow_version: String by project
val mockk_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.0.3"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("com.google.cloud.tools.jib") version "3.4.5"
    id("java-test-fixtures")
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-apache5")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-swagger")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("org.mongodb:mongodb-driver-core:$mongo_version")
    implementation("org.mongodb:mongodb-driver-sync:$mongo_version")
    implementation("org.mongodb:bson:$mongo_version")
    implementation("org.mongodb:bson-kotlinx:$mongo_version")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.arrow-kt:arrow-core:$arrow_version")
    implementation("org.sqids:sqids-kotlin:0.1.1")
    implementation("com.rabbitmq:amqp-client:5.25.0")
    implementation("org.apache.commons:commons-pool2:2.12.1")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.kotest:kotest-runner-junit5:$kotest_version")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest_version")
    testImplementation("io.mockk:mockk:${mockk_version}")

    testFixturesImplementation("org.mongodb:bson:$mongo_version")
    testFixturesImplementation("io.arrow-kt:arrow-core:$arrow_version")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

jib {
    from {
        image = "eclipse-temurin:21-jre-alpine"
    }
    to {
        image = "knight7024/alt-tab"
        tags = setOf("latest", "jre-21")
    }
    container {
        format = com.google.cloud.tools.jib.api.buildplan.ImageFormat.OCI
        creationTime = "USE_CURRENT_TIMESTAMP"
        ports = listOf("8080")
        jvmFlags =
            listOf(
                "-server",
                "-Dfile.encoding=UTF-8",
                "-Dsun.net.inetaddr.ttl=0",
                "-Dapp.id=alt-tab",
            )
    }
}
