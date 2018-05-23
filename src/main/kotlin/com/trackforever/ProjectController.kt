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
    lateinit var projectRepository: ProjectRepository

    /**
     * Gets all projects from the database.
     * Possible Responses: HTTP 200 OK with an Array of all projects.
     *                     HTTP 410 GONE if no projects exist in the database.
     */
    @GetMapping("/projects")
    fun getProjects(): ResponseEntity<Array<TrackForeverProject>> {
        val projectList = ArrayList<TrackForeverProject>(projectRepository.findAll())
        val projectArray = projectList.toTypedArray()
        return if (projectList.isEmpty()) ResponseEntity(HttpStatus.GONE) else ResponseEntity(projectArray, HttpStatus.OK)
    }

    /**
     * Gets the specified project.
     * Possible Responses: HTTP 200 OK with the specified project.
     *                     HTTP 410 GONE if the specified project does not exist.
     */
    @GetMapping("/projects/{projectKey}")
    fun getProject(@PathVariable projectKey: String): ResponseEntity<TrackForeverProject> {
        val specifiedProject = projectRepository.findById(projectKey)
        return if (specifiedProject.isPresent) ResponseEntity(specifiedProject.get(), HttpStatus.OK) else ResponseEntity(HttpStatus.GONE)
    }

    /**
     * Retrieves a Map of all the projectIds and their hashes.
     * Possible Responses: HTTP 200 OK along with a Map containing K-V pairs such that K = projectId and V = hash
     *                     HTTP 410 GONE if no projects exist in the database
     */
    @GetMapping("/hashes")
    fun getHashes(): ResponseEntity<Map<String, String>> {
        val projectList = ArrayList<TrackForeverProject>(projectRepository.findAll())
        if (projectList.isEmpty()) return ResponseEntity(HttpStatus.GONE)
        val projectHashes: MutableMap<String, String> = mutableMapOf()
        projectList.forEach {
            projectHashes[it.id] = it.hash
        }
        return ResponseEntity(projectHashes, HttpStatus.OK)
    }

    // TODO: Handle Array of Pairs instead of Maps. :(

    /**
     * Adds or updates a project based on the the given data.
     * Expects a mapping of K-V pairs such that K = projectKey and V = Array of Issues
     * Possible Responses: HTTP 200 OK if all projects exist
     *                     HTTP 410 GONE if at least one project in the key set does not exist..
     * TODO: Add partial failure
     */
    @PutMapping("/issues")
    fun setIssues(@RequestBody issues: Map<String, Array<TrackForeverIssue>>): ResponseEntity<Unit> {
        issues.keys.forEach {
            val tFProj = projectRepository.findById(it)
            if (tFProj.isPresent) { // The projectKey does exist
                issues[it]?.forEach {
                    tFProj.get().issues[it.id] = it
                }
                projectRepository.save(tFProj.get()) // Update the entry in the database
            } else { // The projectKey doesn't exist in the database, return an error...
                return ResponseEntity(HttpStatus.GONE)
            }
        }
        return ResponseEntity(HttpStatus.OK)
    }

    /**
     * Adds the given projects to the database.
     * Possible Responses: HTTP 200 OK upon completion.
     * TODO: Verify the project data?
     */
    @PutMapping("/projects")
    fun setProjects(@RequestBody projects: Array<TrackForeverProject>): ResponseEntity<Unit> {
        // Add to the database
        projects.forEach {
            projectRepository.save(it)
        }
        return ResponseEntity(HttpStatus.OK)
    }

    // TODO: Check if using Pair is okay.
    /**
     * Retrieves the specified issue given the projectKey and the issueId.
     * Possible Responses: HTTP 200 OK along with the specified issue if such a projectKey and issueId exist.
     *                     HTTP 410 GONE if either the project or issue does not exist.
     */
    @PostMapping("/issues")
    fun getIssue(@RequestBody projectAndId: Pair<String, String>): ResponseEntity<TrackForeverIssue> {
        val specifiedProject = projectRepository.findById(projectAndId.first)
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

    /**
     * Retrieves the requested issues.
     * Expects a Map containing K-V pairs such that K = projectKey and V = Array of issueIds.
     * TODO: Add partial failure
     * Possible Responses: HTTP 200 OK with a Map where K-V pairs such that K = projectKey and V = Array of issues
     *                     HTTP 410 GONE if a project or an issue does not exist.
     */
    @PostMapping("/issues")
    fun getRequestedIssues(@RequestBody issueIds: Map<String, Array<String>>): ResponseEntity<Map<String, Array<TrackForeverIssue>>> {
        val requestedIssues: MutableMap<String, Array<TrackForeverIssue>> = mutableMapOf()
        issueIds.keys.forEach {
            val specifiedProject = projectRepository.findById(it)
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

    /**
     * Gets an Array of all requested projects.
     * Expects an Array of projectIds that are requested.
     * TODO: Add partial failure
     * Possible Responses: HTTP 200 OK with an Array of all requested projects.
     *                     HTTP 410 GONE if at least one requested project does not exist.
     */
    @PostMapping("/projects")
    fun getRequestedProjects(@RequestBody projectIds: Array<String>): ResponseEntity<Array<TrackForeverProject>> {
        val requestedProjects: Array<TrackForeverProject> = emptyArray()
        projectIds.forEach {
            val specifiedProject = projectRepository.findById(it)
            if (specifiedProject.isPresent) {
                requestedProjects[requestedProjects.size] = specifiedProject.get()
            } else {
                return ResponseEntity.notFound().build()
            }
        }
        return ResponseEntity(requestedProjects, HttpStatus.OK)
    }


}