package com.trackforever.repositories

import com.trackforever.models.TrackForeverProject
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : MongoRepository<TrackForeverProject, String> {
    fun findByHash(hash: String) : TrackForeverProject
    fun deleteByHash(hash: String)
}