package com.trackforever.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.trackforever.models.TrackForeverProject

class TrackForeverProjectSerializer : StdSerializer<TrackForeverProject>(TrackForeverProject::class.java) {
    override fun serialize(value: TrackForeverProject, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("prevHash", value.prevHash)
        gen.writeStringField("id", value.id)
        gen.writeStringField("ownerName", value.ownerName)
        gen.writeStringField("name", value.name)
        gen.writeStringField("description", value.description)
        gen.writeStringField("source", value.source)
        gen.writeEndObject()
    }
}