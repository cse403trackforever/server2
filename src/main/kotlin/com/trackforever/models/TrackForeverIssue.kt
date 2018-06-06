package com.trackforever.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="issues")
data class TrackForeverIssue(
        var hash: String,
        var prevHash: String,
        @Id
        var id: String,
        var projectId: String,
        var status: String,
        var summary: String,
        var labels: List<String>,
        var comments: List<TrackForeverComment>,
        var submitterName: String,
        var assignees: List<String>,
        var timeCreated: Long?,
        var timeUpdated: Long?,
        var timeClosed: Long?
)