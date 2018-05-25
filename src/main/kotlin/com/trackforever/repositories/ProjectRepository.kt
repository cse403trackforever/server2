package com.trackforever.repositories

import com.trackforever.models.TrackForeverProject
import org.springframework.data.mongodb.repository.MongoRepository

interface ProjectRepository : MongoRepository<TrackForeverProject, String> {
    fun findByHash(hash: String): TrackForeverProject
}