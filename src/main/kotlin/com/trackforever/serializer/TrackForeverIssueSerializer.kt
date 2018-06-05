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
        if (value.timeCreated != null) {
            gen.writeNumberField("timeCreated", value.timeCreated!!)
        } else {
            gen.writeNumberField("timeCreated", null)
        }
        if (value.timeUpdated != null) {
            gen.writeNumberField("timeUpdated", value.timeUpdated!!)
        } else {
            gen.writeNumberField("timeUpdated", null)
        }
        if (value.timeClosed != null) {
            gen.writeNumberField("timeClosed", value.timeClosed!!)
        } else {
            gen.writeNumberField("timeClosed", null)
        }
        gen.writeEndObject()
    }

}