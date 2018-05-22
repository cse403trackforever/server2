package com.trackforever.models

data class TrackForeverIssue(
        val hash: String,
        val prevHash: String,
        val id: String,
        val projectId: String,
        val status: String,
        val labels: Array<String>,
        val comments: Array<TrackForeverComment>,
        val submitterName: String,
        val assignees: Array<String>,
        val timeCreated: Long,
        val timeUpdated: Long,
        val timeClosed: Long
)