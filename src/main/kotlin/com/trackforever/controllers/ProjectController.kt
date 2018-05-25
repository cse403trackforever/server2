package com.trackforever.controllers

import com.trackforever.repositories.ProjectRepository
import com.trackforever.models.TrackForeverIssue
import com.trackforever.models.TrackForeverProject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

typealias ProjectId = String
typealias IssueId = String

@RestController
class ProjectController {

    @Autowired
    lateinit var projectRepository: ProjectRepository


    data class HashResponse(val project: String, val issues: Map<IssueId, String>)

    /**
     * Gets all projects from the database.
     * Possible Responses: HTTP 200 OK with a List of all projects.
     *                     HTTP 410 GONE if no projects exist in the database.
     */
    @GetMapping("/projects")
    fun getProjects(): ResponseEntity<List<TrackForeverProject>> {
        val projectList = ArrayList<TrackForeverProject>(projectRepository.findAll())
        return when {
            projectList.isEmpty() -> ResponseEntity(HttpStatus.GONE)
            else -> ResponseEntity(projectList, HttpStatus.OK)
        }
    }

    /**
     * Gets the specified project.
     * Possible Responses: HTTP 200 OK with the specified project.
     *                     HTTP 410 GONE if the specified project does not exist.
     */
    @GetMapping("/projects/{projectKey}")
    fun getProject(@PathVariable projectKey: ProjectId): ResponseEntity<TrackForeverProject> {
        val specifiedProject = projectRepository.findById(projectKey)
        return if (specifiedProject.isPresent) ResponseEntity(specifiedProject.get(), HttpStatus.OK) else ResponseEntity(HttpStatus.GONE)
    }

    /**
     * Gets the specified issue within the specified project.
     * Possible Responses: HTTP 200 OK with the issue.
     *                     HTTP 410 GONE if the specified project or the specified issue doesn't exist.
     */
    @GetMapping("/issues/{projectId}/{issueId}")
    fun getIssue(@PathVariable projectId: ProjectId, @PathVariable issueId: IssueId): ResponseEntity<TrackForeverIssue> {
        val specifiedProject = projectRepository.findById(projectId)
        return when {
            specifiedProject.isPresent -> {
                val issue = specifiedProject.get().issues[issueId]
                if (issue != null) {
                    ResponseEntity(issue, HttpStatus.OK) // Return the TrackForeverIssue with an OK response
                } else {
                    ResponseEntity(HttpStatus.GONE)
                }
            }
            else -> ResponseEntity(HttpStatus.GONE)
        }
    }

    /**
     * Retrieves a Map of all the projectIds and their hashes.
     * Possible Responses: HTTP 200 OK along with a Map containing K-V pairs such that K = projectId and V = hash
     *                     HTTP 410 GONE if no projects exist in the database
     */
    @GetMapping("/hashes")
    fun getHashes(): ResponseEntity<Map<ProjectId, HashResponse>> {
        val projectList = ArrayList<TrackForeverProject>(projectRepository.findAll())
        if (projectList.isEmpty()) return ResponseEntity(HttpStatus.GONE)
        val projectHashes: MutableMap<ProjectId, HashResponse> = mutableMapOf()
        projectList.forEach {
            val issuesMap: MutableMap<IssueId, String> = mutableMapOf()
            it.issues.forEach {
                issuesMap[it.key] = it.value.hash
            }
            projectHashes[it.id] = HashResponse(it.hash, issuesMap)
        }
        return ResponseEntity(projectHashes, HttpStatus.OK)
    }

    /**
     * Adds or updates a project based on the the given data.
     * Expects a mapping of K-V pairs such that K = projectKey and V = List of Issues
     * Possible Responses: HTTP 200 OK if all projects exist
     *                     HTTP 207 MULTI_STATUS with projectKeys mapped to an List of issues that failed to be set.
     */
    @PutMapping("/issues")
    fun setIssues(@RequestBody issues: Map<ProjectId, List<TrackForeverIssue>>): ResponseEntity<Map<ProjectId, List<TrackForeverIssue>>> {
        val failedProjects: MutableMap<ProjectId, List<TrackForeverIssue>> = mutableMapOf()
        issues.keys.forEach {
            val tFProj = projectRepository.findById(it)
            if (tFProj.isPresent) { // The projectKey does exist
                issues[it]?.forEach { // For non-null Lists
                    tFProj.get().issues[it.id] = it
                }
                projectRepository.save(tFProj.get()) // Update the entry in the database
            } else { // The projectKey doesn't exist in the database, then add it to the failed list with an empty list as its value
                val failedIssues = issues[it]
                if (failedIssues == null) {
                    failedProjects[it] = emptyList()
                } else {
                    failedProjects[it] = failedIssues
                }
            }
        }
        return when {
            failedProjects.isEmpty() -> ResponseEntity(HttpStatus.OK)
            failedProjects.keys.size == issues.keys.size -> ResponseEntity(issues, HttpStatus.GONE) // None of the specified projects exist.
            else -> ResponseEntity(failedProjects, HttpStatus.MULTI_STATUS)
        }
    }

    /**
     * Adds the given projects to the database.
     * Possible Responses: HTTP 200 OK upon completion.
     * TODO: Verify that the projects being passed in are valid
     */
    @PutMapping("/projects")
    fun setProjects(@RequestBody projects: List<TrackForeverProject>): ResponseEntity<Unit> {
        // Add to the database
        projects.forEach {
            projectRepository.save(it)
        }
        return ResponseEntity(HttpStatus.OK)
    }

    /**
     * Retrieves the requested issues.
     * Expects a Map containing K-V pairs such that K = projectKey and V = List of issueIds.
     * TODO: Consistency with setIssues()
     * Possible Responses: HTTP 200 OK with a Map where K-V pairs such that K = projectKey and V = List of issues
     *                     HTTP 207 MULTI_STATUS with a Map of K-V pairs such that K = projectKey and V = List of issues successfully retrieved
     *                     A 207 occurs when either a project or an issue failed to retrieve.
     *                     HTTP 410 GONE if none of the projects or issues could be found
     */
    @PostMapping("/issues")
    fun getRequestedIssues(@RequestBody issueIds: Map<ProjectId, List<IssueId>>): ResponseEntity<Map<ProjectId, List<TrackForeverIssue>>> {
        val requestedIssues: MutableMap<String, List<TrackForeverIssue>> = mutableMapOf()
        var partialFailure = false
        issueIds.keys.forEach {
            val specifiedProject = projectRepository.findById(it)
            // Ensure project exists based on the projectKey
            if (specifiedProject.isPresent) issueIds[it]?.forEach { // Go through list of issueIds
                val issue = specifiedProject.get().issues[it]
                if (issue != null) { // Check to make sure the retrieved issue isn't null
                    requestedIssues.putIfAbsent(issue.projectId, mutableListOf())
                    val issuesList = requestedIssues[issue.projectId] as MutableList<TrackForeverIssue>
                    issuesList[issuesList.size] = issue
                } else {
                    partialFailure = true
                }
            } else {
                requestedIssues[it] = emptyList()
                partialFailure = true
            }
        }
        return when {
            requestedIssues.isEmpty() -> ResponseEntity(HttpStatus.GONE)
            partialFailure -> ResponseEntity(requestedIssues, HttpStatus.MULTI_STATUS)
            else -> ResponseEntity(requestedIssues, HttpStatus.OK)
        }
    }

    /**
     * Gets a List of all requested projects.
     * Expects an List of projectIds that are requested.
     * Possible Responses: HTTP 200 OK with a List of all requested projects.
     *                     HTTP 207 MULTI_STATUS with a List of all requested projects that were able to be retrieved.
     *                     HTTP 410 GONE if at least one requested project does not exist.
     */
    @PostMapping("/projects")
    fun getRequestedProjects(@RequestBody projectIds: List<String>): ResponseEntity<List<TrackForeverProject>> {
        val requestedProjects: MutableList<TrackForeverProject> = mutableListOf()
        projectIds.forEach {
            val specifiedProject = projectRepository.findById(it)
            if (specifiedProject.isPresent) {
                requestedProjects[requestedProjects.size] = specifiedProject.get()
            }
        }
        return when {
            requestedProjects.isEmpty() -> ResponseEntity(HttpStatus.GONE)
            requestedProjects.size != projectIds.size -> ResponseEntity(requestedProjects, HttpStatus.MULTI_STATUS)
            else -> ResponseEntity(requestedProjects, HttpStatus.OK)
        }
    }


}