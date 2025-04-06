package com.example.adapter.mongodb

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GenerateTabGroupId(
    private val dao: MongoCollection<CounterDocument>,
) {
    operator fun invoke(): Long =
        dao
            .findOneAndUpdate(
                Filters.eq(CounterDocument.FIELD_NAME, "tab_group"),
                Updates.inc(CounterDocument.FIELD_SEQUENCE, 1L),
                FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER),
            )!!
            .sequence
}

@Serializable
data class CounterDocument(
    @SerialName(FIELD_SEQUENCE)
    val sequence: Long,
) {
    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_SEQUENCE = "sequence"
    }
}
