package com.trackforever

import com.trackforever.models.TrackForeverProject
import org.springframework.web.bind.annotation.*

@RestController
class ProjectController {
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
        var temp: MutableMap<String, String> = mutableMapOf()
        temp.put("specific project", "fakeProj")
        temp.put("unknown project", "Invalid")
        return temp
    }

    @PostMapping("/issues")
    fun getIssue() {
    }

}