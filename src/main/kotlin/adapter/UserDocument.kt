package com.example.adapter

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDocument(
    @SerialName(FIELD_UUID)
    val uuid: String,
    @SerialName(FIELD_SIGNED_UP_AT)
    val signedUpAt: Long,
) {
    companion object {
        const val FIELD_UUID = "uuid"
        const val FIELD_SIGNED_UP_AT = "signed_up_at"
    }
}
