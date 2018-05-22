package com.trackforever.models

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="projects")
data class TrackForeverProject(
        val hash: String,
        val prevHash: String,
        @Id
        val id: String,
        val ownerName: String,
        val name: String,
        val description: String,
        val source: String,
        val issues: Map<String, TrackForeverIssue>
)