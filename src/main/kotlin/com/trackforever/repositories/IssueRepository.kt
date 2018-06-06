package com.trackforever.repositories

import com.trackforever.IssueKey
import com.trackforever.ProjectId
import com.trackforever.models.TrackForeverIssue
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IssueRepository : MongoRepository<TrackForeverIssue, IssueKey> {
    fun findByProjectId(id: ProjectId): List<TrackForeverIssue>
}