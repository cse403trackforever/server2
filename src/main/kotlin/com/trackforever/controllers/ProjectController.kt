package com.trackforever.controllers

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.trackforever.Application
import com.trackforever.models.TrackForeverIssue
import com.trackforever.models.TrackForeverProject
import com.trackforever.repositories.ProjectRepository
import com.trackforever.serializer.TrackForeverIssueSerializer
import com.trackforever.serializer.TrackForeverProjectSerializer
import org.bouncycastle.jcajce.provider.digest.SHA3
import org.bouncycastle.util.encoders.Hex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

typealias ProjectId = String
typealias IssueId = String
typealias Hash = String

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
        Application.logger.debug("get projects")
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
        Application.logger.debug("get project :: ($projectKey)")
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
        Application.logger.debug("get issues :: ($projectId, $issueId)")
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
        Application.logger.debug("get hashes")
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
     * Adds or updates issues for projects.
     * Expects a mapping of K-V pairs such that K = projectKey and V = List of Issues as a JSON String
     * Possible Responses: HTTP 200 OK with a map of all requested projects to their list of issues
     *                     HTTP 207 MULTI_STATUS with all projects that could be found mapped to their list of issues
     */
    @CrossOrigin
    @PutMapping("/issues")
    fun setIssues(@RequestBody issues: String): ResponseEntity<Map<ProjectId, Map<IssueId, Hash>>> {
        // Check with the prevHash
        // prevHash matches => update that issue for that project
        // prevHash doesn't match => ignore
        // Duplicate issue => ignore
        Application.logger.debug("set issues :: ($issues)")
        val successes: MutableMap<ProjectId, MutableMap<IssueId, Hash>> = mutableMapOf()
        val mapper = jacksonObjectMapper()
        val content: Map<ProjectId, List<TrackForeverIssue>> = mapper.readValue(issues)
        content.forEach {
            // Check if project exists
            val serverProject = projectRepository.findById(it.key)
            if (serverProject.isPresent) { // the project exists on the server
                val project = serverProject.get()
                successes.putIfAbsent(it.key, mutableMapOf())
                // Go through the given list and update those issues.
                val givenIssues = it.value
                givenIssues.forEach {
                    if (project.issues.containsKey(it.id)) { // Assumes IssueId never changes
                        it.prevHash = project.issues[it.id]!!.hash // Set prevHash to the one in the server
                    }
                    if (successes[it.projectId] == null) throw error("Issue: ${it.id} needs a valid projectId")

                    // If updated issue, update hash
                    val newHash = generateHash(it)
                    if (newHash != it.hash) {
                        it.prevHash = it.hash
                        it.hash = generateHash(it)
                    }

                    // Add as success and update issue in project
                    successes[it.projectId]!![it.id] = it.hash
                    project.issues[it.id] = it
                }
                projectRepository.save(serverProject.get())
            }
        }
        return when {
            successes.keys.size == content.keys.size -> ResponseEntity(successes, HttpStatus.OK)
            else -> ResponseEntity(successes, HttpStatus.MULTI_STATUS)
        }
    }

    /**
     * Adds the given projects to the database.
     * Expects a List of projects in a JSON String.
     * Possible Responses: HTTP 200 OK upon completion with a map of projectIds to the calculated hash
     */
    @CrossOrigin
    @PutMapping("/projects")
    fun setProjects(@RequestBody projects: String): ResponseEntity<Map<ProjectId, Hash>> {
        Application.logger.debug("set projects :: ($projects)")
        val mapper = jacksonObjectMapper()
        val content: List<TrackForeverProject> = mapper.readValue(projects)
        val successes: MutableMap<ProjectId, Hash> = mutableMapOf()

        content.forEach {
            // Look in the database for the previous project
            val serverProject = projectRepository.findById(it.id)
            if (serverProject.isPresent) { // project with issue ID already exists.
                val oldProject = serverProject.get()
                it.issues = oldProject.issues

                // If updated project, update hash
                val newHash = generateHash(it)
                if (newHash != it.hash) {
                    it.prevHash = it.hash
                    it.hash = generateHash(it)
                }

                projectRepository.deleteById(it.id) // Remove the old one.
            }

            // Update project and add to successes
            projectRepository.save(it)
            successes[it.id] = it.hash
        }
        return ResponseEntity(successes, HttpStatus.OK)
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
        Application.logger.debug("get req issues :: ($issueIds)")
        val requestedIssues: MutableMap<ProjectId, MutableList<TrackForeverIssue>> = mutableMapOf()
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
                    val issuesList = requestedIssues[issue.projectId]
                    if (issuesList != null) {
                        issuesList += issue
                    }
                } else {
                    partialFailure = true
                }
            } else {
                requestedIssues[it] = mutableListOf()
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
        Application.logger.debug("get req projects :: $projectIds")
        val requestedProjects: MutableList<TrackForeverProject> = mutableListOf()
        val mapper = jacksonObjectMapper()
        val content: List<ProjectId> = mapper.readValue(projectIds)
        content.forEach {
            val specifiedProject = projectRepository.findById(it)
            if (specifiedProject.isPresent) {
                requestedProjects += specifiedProject.get()
            }
        }
        return when {
            requestedProjects.size != content.size -> ResponseEntity(requestedProjects, HttpStatus.MULTI_STATUS)
            else -> ResponseEntity(requestedProjects, HttpStatus.OK)
        }
    }

    /**
     * Generates the SHA3 hash from the given TrackForeverProject or TrackForeverIssue.
     * Ignores the current hash field when performing calculations and issues (if project).
     */
    fun generateHash(jsonData: Any): String {
        val mapper = jacksonObjectMapper()
        when (jsonData) {
            is TrackForeverProject -> {
                val module = SimpleModule("TrackForeverProjectSerializer", Version(1, 0, 0, null, null, null))
                module.addSerializer(TrackForeverProject::class.java, TrackForeverProjectSerializer())
                mapper.registerModule(module)
                val projectJson = mapper.writeValueAsString(jsonData)
                Application.logger.debug("Project JSON: $projectJson")
                val crypto = SHA3.Digest512()
                val projectByteArray = crypto.digest(projectJson.toByteArray())
                return Hex.toHexString(projectByteArray)
            }
            is TrackForeverIssue -> {
                val module = SimpleModule("TrackForeverIssueSerializer", Version(1, 0, 0, null, null, null))
                module.addSerializer(TrackForeverIssue::class.java, TrackForeverIssueSerializer())
                mapper.registerModule(module)
                val issueJson = mapper.writeValueAsString(jsonData)
                Application.logger.debug("Issue JSON: $issueJson")
                val crypto = SHA3.Digest512()
                val issueByteArray = crypto.digest(issueJson.toByteArray())
                return Hex.toHexString(issueByteArray)
            }
            else -> throw Error("Must pass in TrackForeverProject and TrackForeverIssue.")
        }
    }
}