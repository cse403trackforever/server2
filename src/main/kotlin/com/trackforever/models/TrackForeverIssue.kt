package com.trackforever.models

import com.trackforever.Hash
import com.trackforever.IssueId
import com.trackforever.ProjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection="issues")
data class TrackForeverIssue(
        var hash: Hash,
        var prevHash: Hash,
        @Id
        var id: IssueId,
        var projectId: ProjectId,
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