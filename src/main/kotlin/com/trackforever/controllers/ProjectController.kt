package com.trackforever.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
class ProjectController (@Autowired private var projectRepository: ProjectRepository) {

    data class HashResponse(val project: String, val issues: Map<IssueId, String>)

    /**
     * Gets all projects from the database.
     * Possible Responses: HTTP 200 OK with a List of all projects.
     *                     HTTP 410 GONE if no projects exist in the database.
     */
    @CrossOrigin
    @GetMapping("/projects")
    fun getProjects(): ResponseEntity<List<TrackForeverProject>> {
        println("get projects")
        val projectList = ArrayList<TrackForeverProject>(projectRepository.findAll())
        return when {
            projectList.isEmpty() -> ResponseEntity(projectList, HttpStatus.OK)
            else -> ResponseEntity(projectList, HttpStatus.OK)
        }
    }

    /**
     * Gets the specified project.
     * Possible Responses: HTTP 200 OK with the specified project.
     *                     HTTP 410 GONE if the specified project does not exist.
     */
    @CrossOrigin
    @GetMapping("/projects/{projectKey}")
    fun getProject(@PathVariable projectKey: ProjectId): ResponseEntity<TrackForeverProject> {
        println("get project")
        println(projectKey)
        val specifiedProject = projectRepository.findById(projectKey)
        return if (specifiedProject.isPresent) ResponseEntity(specifiedProject.get(), HttpStatus.OK) else ResponseEntity(HttpStatus.GONE)
    }

    /**
     * Gets the specified issue within the specified project.
     * Possible Responses: HTTP 200 OK with the issue.
     *                     HTTP 410 GONE if the specified project or the specified issue doesn't exist.
     */
    @CrossOrigin
    @GetMapping("/issues/{projectId}/{issueId}")
    fun getIssue(@PathVariable projectId: ProjectId, @PathVariable issueId: IssueId): ResponseEntity<TrackForeverIssue> {
        println("get issues")
        println("$projectId : $issueId")
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
     */
    @CrossOrigin
    @GetMapping("/hashes")
    fun getHashes(): ResponseEntity<Map<ProjectId, HashResponse>> {
        println("get hashes")
        val projectList = ArrayList<TrackForeverProject>(projectRepository.findAll())
        if (projectList.isEmpty()) return ResponseEntity(emptyMap(), HttpStatus.OK)
        val projectHashes: MutableMap<ProjectId, HashResponse> = mutableMapOf()
        projectList.forEach {
            val issueHashes: MutableMap<IssueId, String> = mutableMapOf()
            it.issues.forEach {
                issueHashes[it.key] = it.value.hash
            }
            projectHashes[it.id] = HashResponse(it.hash, issueHashes)
        }
        return ResponseEntity(projectHashes, HttpStatus.OK)
    }

    /**
     * Adds or updates a project based on the the given data.
     * Expects a mapping of K-V pairs such that K = projectKey and V = List of Issues as a JSON String
     * Possible Responses: HTTP 200 OK if all projects exist
     *                     HTTP 207 MULTI_STATUS with projectKeys mapped to an List of issues that failed to be set.
     */
    @CrossOrigin
    @PutMapping("/issues")
    fun setIssues(@RequestBody issues: String): ResponseEntity<Map<ProjectId, List<TrackForeverIssue>>> {
        println("set issues")
        println(issues)
        val failedProjects: MutableMap<ProjectId, List<TrackForeverIssue>> = mutableMapOf()
        val mapper = jacksonObjectMapper()
        val content: Map<ProjectId, List<TrackForeverIssue>> = mapper.readValue(issues)
        content.keys.forEach {
            val tFProj = projectRepository.findById(it)
            if (tFProj.isPresent) { // The projectKey does exist
                content[it]?.forEach { // For non-null Lists
                    tFProj.get().issues[it.id] = it
                }
                projectRepository.save(tFProj.get()) // Update the entry in the database
            } else { // The projectKey doesn't exist in the database, then add it to the failed list with an empty list as its value
                val failedIssues = content[it]
                if (failedIssues == null) {
                    failedProjects[it] = emptyList()
                } else {
                    failedProjects[it] = failedIssues
                }
            }
        }
        return when {
            failedProjects.isEmpty() -> ResponseEntity(HttpStatus.OK)
            failedProjects.keys.size == content.keys.size -> ResponseEntity(content, HttpStatus.GONE) // None of the specified projects exist.
            else -> ResponseEntity(failedProjects, HttpStatus.MULTI_STATUS)
        }
    }

    /**
     * Adds the given projects to the database.
     * Expects a List of projects in a JSON String.
     * Possible Responses: HTTP 200 OK upon completion.
     * TODO: Verify that the projects being passed in are valid
     */
    @CrossOrigin
    @PutMapping("/projects")
    fun setProjects(@RequestBody projects: String): ResponseEntity<String> {
        println("set projects")
        println(projects)
        val mapper = jacksonObjectMapper()
        val content: List<TrackForeverProject> = mapper.readValue(projects)
        // Add to the database
        content.forEach {
            projectRepository.save(it)
        }
        return ResponseEntity("Projects saved.", HttpStatus.OK)
    }

    /**
     * Retrieves the requested issues.
     * Expects a Map containing K-V pairs such that K = projectKey and V = List of issueIds as a JSON String.
     * Possible Responses: HTTP 200 OK with a Map where K-V pairs such that K = projectKey and V = List of issues
     *                     HTTP 207 MULTI_STATUS with a Map of K-V pairs such that K = projectKey and V = List of issues successfully retrieved
     *                     A 207 occurs when either a single project or an issue failed to retrieve.
     */
    @CrossOrigin
    @PostMapping("/issues")
    fun getRequestedIssues(@RequestBody issueIds: String): ResponseEntity<Map<ProjectId, List<TrackForeverIssue>>> {
        println("get req issues")
        println(issueIds)
        val requestedIssues: MutableMap<String, List<TrackForeverIssue>> = mutableMapOf()
        var partialFailure = false
        val mapper = jacksonObjectMapper()
        val content: Map<ProjectId, List<IssueId>> = mapper.readValue(issueIds)
        content.keys.forEach {
            val specifiedProject = projectRepository.findById(it)
            // Ensure project exists based on the projectKey
            if (specifiedProject.isPresent) content[it]?.forEach { // Go through list of issueIds
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
            partialFailure -> ResponseEntity(requestedIssues, HttpStatus.MULTI_STATUS)
            else -> ResponseEntity(requestedIssues, HttpStatus.OK)
        }
    }

    /**
     * Gets a List of all requested projects.
     * Expects an List of projectIds that are requested as a JSON String.
     * Possible Responses: HTTP 200 OK with a List of all requested projects.
     *                     HTTP 207 MULTI_STATUS with a List of all requested projects that were able to be retrieved.
     */
    @CrossOrigin
    @PostMapping("/projects")
    fun getRequestedProjects(@RequestBody projectIds: String): ResponseEntity<List<TrackForeverProject>> {
        println("get req projs")
        println(projectIds)
        val requestedProjects: MutableList<TrackForeverProject> = mutableListOf()
        val mapper = jacksonObjectMapper()
        val content: List<String> = mapper.readValue(projectIds)
        content.forEach {
            val specifiedProject = projectRepository.findById(it)
            if (specifiedProject.isPresent) {
                requestedProjects[requestedProjects.size] = specifiedProject.get()
            }
        }
        return when {
            requestedProjects.size != content.size -> ResponseEntity(requestedProjects, HttpStatus.MULTI_STATUS)
            else -> ResponseEntity(requestedProjects, HttpStatus.OK)
        }
    }


}