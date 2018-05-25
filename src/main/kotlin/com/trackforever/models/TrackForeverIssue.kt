package com.trackforever.models

data class TrackForeverIssue(
        val hash: String,
        val prevHash: String,
        val id: String,
        val projectId: String,
        val status: String,
        val labels: List<String>,
        val comments: List<TrackForeverComment>,
        val submitterName: String,
        val assignees: List<String>,
        val timeCreated: Long?,
        val timeUpdated: Long?,
        val timeClosed: Long?
)