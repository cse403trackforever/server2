package com.trackforever.models

data class TrackForeverIssue(
        var hash: String,
        var prevHash: String,
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