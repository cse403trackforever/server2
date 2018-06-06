package com.trackforever.models

import com.trackforever.Hash
import com.trackforever.IssueId
import com.trackforever.ProjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="projects")
data class TrackForeverProject(
        var hash: Hash,
        var prevHash: Hash,
        @Id
        val id: ProjectId,
        var ownerName: String,
        var name: String,
        var description: String,
        var source: String,
        var issues: MutableMap<IssueId, TrackForeverIssue> = mutableMapOf()
)