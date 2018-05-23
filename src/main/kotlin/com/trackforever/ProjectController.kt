package com.trackforever

import com.trackforever.models.TrackForeverIssue
import com.trackforever.models.TrackForeverProject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ProjectController {

    @Autowired
    lateinit var projRepo: ProjectRepository

    @GetMapping("/projects")
    fun getProjects(): ResponseEntity<Array<TrackForeverProject>> {
        val projectList = ArrayList<TrackForeverProject>(projRepo.findAll())
        val projectArray = projectList.toTypedArray()
        return if (projectList.isEmpty()) ResponseEntity(HttpStatus.GONE) else ResponseEntity(projectArray, HttpStatus.OK)
    }

    @GetMapping("/projects/{projectKey}")
    fun getProject(@PathVariable projectKey: String): ResponseEntity<TrackForeverProject> {
        val specifiedProject = projRepo.findById(projectKey)
        return if (specifiedProject.isPresent) ResponseEntity(specifiedProject.get(), HttpStatus.OK) else ResponseEntity(HttpStatus.GONE)
    }

    @GetMapping("/hashes")
    fun getHashes(): ResponseEntity<Map<String, String>> {
        val projectList = ArrayList<TrackForeverProject>(projRepo.findAll())
        if (projectList.isEmpty()) return ResponseEntity(HttpStatus.GONE)
        val projectHashes: MutableMap<String, String> = mutableMapOf()
        projectList.forEach {
            projectHashes[it.id] = it.hash
        }
        return ResponseEntity(projectHashes, HttpStatus.OK)
    }

    // TODO: Handle Array of Pairs instead of Maps. :(

    // Add or update existing issues
    @PutMapping("/issues")
    fun setIssues(@RequestBody issues: Map<String, Array<TrackForeverIssue>>): ResponseEntity<Unit> {
        issues.keys.forEach {
            val tFProj = projRepo.findById(it)
            if (tFProj.isPresent) { // The projectKey does exist
                issues[it]?.forEach {
                    tFProj.get().issues[it.id] = it
                }
                projRepo.save(tFProj.get()) // Update the entry in the database
            } else { // The projectKey doesn't exist in the database, return an error...
                return ResponseEntity(HttpStatus.GONE)
            }
        }
        return ResponseEntity(HttpStatus.OK)
    }

    @PutMapping("/projects")
    fun setProjects(@RequestBody projects: Array<TrackForeverProject>): ResponseEntity<Unit> {
        // Add to the database
        projects.forEach {
            projRepo.save(it)
        }
        return ResponseEntity(HttpStatus.OK)
    }

    // Request Body looks like { projectKey, issueId } where both projectKey and issueId are Strings
    // TODO: Check if using Pair is okay.
    @PostMapping("/issues")
    fun getIssue(@RequestBody projectAndId: Pair<String, String>): ResponseEntity<TrackForeverIssue> {
        val specifiedProject = projRepo.findById(projectAndId.first)
        return if (specifiedProject.isPresent) {
            val issue = specifiedProject.get().issues[projectAndId.second]
            if (issue != null) {
                ResponseEntity(issue, HttpStatus.OK) // Return the TrackForeverIssue with an OK response
            } else {
                ResponseEntity(HttpStatus.GONE)
            }
        } else {
            ResponseEntity(HttpStatus.GONE)
        }
    }

    // Request Body looks like Map<String, Array<String>> where keys are projectKey and values are an array of issueIds
    @PostMapping("/issues")
    fun getRequestedIssues(@RequestBody issueIds: Map<String, Array<String>>): ResponseEntity<Map<String, Array<TrackForeverIssue>>> {
        val requestedIssues: MutableMap<String, Array<TrackForeverIssue>> = mutableMapOf()
        issueIds.keys.forEach {
            val specifiedProject = projRepo.findById(it)
            // Ensure project exists based on the projectKey
            if (specifiedProject.isPresent) issueIds[it]?.forEach { // Go through Array of issueIds
                val issue = specifiedProject.get().issues[it]
                if (issue != null) { // Check to make sure issueID isn't null.
                    requestedIssues.putIfAbsent(issue.projectId, emptyArray())
                    val issuesArray = requestedIssues[issue.projectId]
                    issuesArray?.set(issuesArray.size, issue)
                }
            } else {
                return ResponseEntity(HttpStatus.GONE)
            }
        }
        return ResponseEntity(requestedIssues, HttpStatus.OK)
    }

    // Request Body looks like Array<String> where each String is a projectKey
    @PostMapping("/projects")
    fun getRequestedProjects(@RequestBody projectIds: Array<String>): ResponseEntity<Array<TrackForeverProject>> {
        val requestedProjects: Array<TrackForeverProject> = emptyArray()
        projectIds.forEach {
            val specifiedProject = projRepo.findById(it)
            if (specifiedProject.isPresent) {
                requestedProjects[requestedProjects.size] = specifiedProject.get()
            } else {
                return ResponseEntity.notFound().build()
            }
        }
        return ResponseEntity(requestedProjects, HttpStatus.OK)
    }


}