package com.trackforever.repositories

import com.trackforever.Hash
import com.trackforever.ProjectId
import com.trackforever.models.TrackForeverProject
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : MongoRepository<TrackForeverProject, ProjectId> {
    fun findByHash(hash: Hash): TrackForeverProject
    fun deleteByHash(hash: Hash)
}