package com.example.adapter

import com.example.domain.UserEmailRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.name
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.apache.hc.core5.util.TimeValue

class GoogleClient(
    baseUrl: String,
) : UserEmailRepository,
    AutoCloseable {
    private val client =
        HttpClient(Apache5) {
            engine {
                connectTimeout = 2000
                socketTimeout = 3000
                connectionRequestTimeout = 5000
                customizeRequest {
                    setConnectionKeepAlive(TimeValue.ofSeconds(10))
                }
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
            install(HttpRequestRetry) {
                maxRetries = 2
                retryOnServerErrors()
                exponentialDelay()
            }
            defaultRequest {
                url(baseUrl)
                accept(ContentType.Application.Json)
                charset(Charsets.UTF_8.name)
            }
            expectSuccess = true
        }

    override suspend fun findByAccessToken(accessToken: String): String? {
        val response: GoogleUserInfo =
            client
                .get("v2/userinfo") {
                    bearerAuth(accessToken)
                }.body()

        return response.email.takeIf { response.verified }
    }

    @Serializable
    data class GoogleUserInfo(
        val email: String,
        @SerialName("verified_email") val verified: Boolean,
    )

    override fun close() {
        client.close()
    }
}
