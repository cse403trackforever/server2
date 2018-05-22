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
    fun getProjects() = TrackForeverProject("", "", "", "", "", "", "", mutableMapOf())

    @GetMapping("/projects/{projectKey}")
    fun getProject(@PathVariable projectKey: String) =
        if (projectKey == "fakeProj")
            TrackForeverProject("fakeProj", "", "specific project", "", "", "", "", mutableMapOf())
        else
            TrackForeverProject("Invalid", "", "unknown project", "", "", "", "", mutableMapOf())

    @GetMapping("/hashes")
    fun getHashes(): MutableMap<String, String> {
        val temp: MutableMap<String, String> = mutableMapOf()
        temp.put("specific project", "fakeProj")
        temp.put("unknown project", "Invalid")
        return temp
    }

    // TODO: Handle Array of Pairs instead of Maps. :(
    // TODO: Replace 404 with 410

    // Add or update existing issues
    @PutMapping("/issues")
    fun setIssues(@RequestBody issues: Map<String, Array<TrackForeverIssue>>): ResponseEntity<Unit> {
        issues.keys.forEach {
            // TODO: Check if the projectKeys actually exists. If not, return a 404 to webapp.
            val tFProj = projRepo.findById(it)
            if (tFProj.isPresent) { // The projectKey does exist
                issues[it]?.forEach {
                    tFProj.get().issues[it.id] = it
                }
                projRepo.save(tFProj.get()) // Update the entry in the database
            } else { // The projectKey doesn't exist in the database, return an error...
                return ResponseEntity.notFound().build()
            }
        }
        return ResponseEntity.ok().build()
    }

    @PutMapping("/projects")
    fun setProjects(@RequestBody projects: Array<TrackForeverProject>): ResponseEntity<Unit> {
        // Add to the database
        projects.forEach {
            projRepo.save(it)
        }
        return ResponseEntity.ok().build()
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
                ResponseEntity.notFound().build()
            }
        } else {
            ResponseEntity.notFound().build()
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
                return ResponseEntity.notFound().build()
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