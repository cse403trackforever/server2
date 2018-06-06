package com.trackforever.repositories

import com.trackforever.models.TrackForeverIssue
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

typealias ProjectId = String
typealias IssueId = String
typealias IssueKey = Pair<ProjectId, IssueId>

@Repository
interface IssueRepository : MongoRepository<TrackForeverIssue, IssueKey> {
    fun findByProjectId(id: ProjectId): List<TrackForeverIssue>
}