package com.trackforever.repositories

import com.trackforever.models.TrackForeverIssue
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface IssueRepository : MongoRepository<TrackForeverIssue, String> {
    fun findByprojectId(projectId: String): List<TrackForeverIssue>
}