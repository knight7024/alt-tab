package com.example.route.auth

import com.example.domain.CreateOrLogin
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Routing.authorization(
    createOrLogin: CreateOrLogin
) {
    route("/auth") {
        authenticate("auth-oauth-google") {
            get("/google") {
                val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()!!
                val user = createOrLogin.byGoogle(principal.accessToken)

                call.respondRedirect("/hello?email=${user.uuid}")
            }
        }
    }
}