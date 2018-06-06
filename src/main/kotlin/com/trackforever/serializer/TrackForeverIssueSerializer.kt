package com.trackforever.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.trackforever.models.TrackForeverIssue

class TrackForeverIssueSerializer: StdSerializer<TrackForeverIssue>(TrackForeverIssue::class.java) {
    override fun serialize(value: TrackForeverIssue, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("prevHash", value.prevHash)
        gen.writeStringField("id", value.id)
        gen.writeStringField("projectId", value.projectId)
        gen.writeStringField("status", value.status)
        gen.writeStringField("summary", value.summary)
        gen.writeObjectField("labels", value.labels)
        gen.writeObjectField("comments", value.comments)
        gen.writeStringField("submitterName", value.submitterName)
        gen.writeObjectField("assignees", value.assignees)

        // Write nullable/optional long fields
        val timeCreated = value.timeCreated
        val timeUpdated = value.timeUpdated
        val timeClosed = value.timeClosed
        if (timeCreated != null) gen.writeNumberField("timeCreated", timeCreated) else gen.writeNullField("timeCreated")
        if (timeUpdated != null) gen.writeNumberField("timeUpdated", timeUpdated) else gen.writeNumberField("timeUpdated", null)
        if (timeClosed != null) gen.writeNumberField("timeClosed", timeClosed) else gen.writeNumberField("timeClosed", null)

        gen.writeEndObject()
    }

}