package com.trackforever.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="projects")
data class TrackForeverProject(
        var hash: String,
        var prevHash: String,
        @Id
        var id: String,
        var ownerName: String,
        var name: String,
        var description: String,
        var source: String,
        var issues: MutableMap<String, TrackForeverIssue> = mutableMapOf()
)