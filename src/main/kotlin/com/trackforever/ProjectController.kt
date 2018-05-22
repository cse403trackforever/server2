package com.trackforever

import com.trackforever.models.TrackForeverIssue
import com.trackforever.models.TrackForeverProject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ProjectController {

    @Autowired
    lateinit var projectRep: ProjectRepository

    @GetMapping("/projects")
    fun getProjects() = TrackForeverProject("", "", "", "", "", "", "", emptyMap())

    @GetMapping("/projects/{projectKey}")
    fun getProject(@PathVariable projectKey: String) =
        if (projectKey == "fakeProj")
            TrackForeverProject("fakeProj", "", "specific project", "", "", "", "", emptyMap())
        else
            TrackForeverProject("Invalid", "", "unknown project", "", "", "", "", emptyMap())

    @GetMapping("/hashes")
    fun getHashes(): MutableMap<String, String> {
        val temp: MutableMap<String, String> = mutableMapOf()
        temp.put("specific project", "fakeProj")
        temp.put("unknown project", "Invalid")
        return temp
    }

    // TODO: Handle Array of Pairs instead of Maps. :(

    @PutMapping("/issues")
    fun setIssues(@RequestBody issues: Map<String, Array<TrackForeverIssue>>): ResponseEntity<Unit> {
        issues.keys.forEach {
            // TODO: Check if the projectKeys actually exists. If not, return a 404 to webapp.
            if (it == "test") { // Please change this.
                return ResponseEntity.notFound().build()
            }
        }
        return ResponseEntity.ok().build()
    }

    @PutMapping("/projects")
    fun setProjects(@RequestBody projects: Array<TrackForeverProject>): ResponseEntity<Unit> {
        // Add to the database
        projects.forEach {
            projectRep.save(it)
        }
        return ResponseEntity.ok().build()
    }

    // Request Body looks like { projectKey, issueId } where both projectKey and issueId are Strings
    @PostMapping("/issues")
    fun getIssue(): TrackForeverIssue {

    }

    // Request Body looks like Map<String, Array<String>> where keys are projectKey and values are an array of issueIds
    @PostMapping("/issues")
    fun getRequestedIssues(): Map<String, Array<TrackForeverIssue>> {

    }

    // Request Body looks like Array<String> where each String is a projectKey
    @PostMapping("/projects")
    fun getRequestedProjects(): Array<TrackForeverProject> {

    }


}