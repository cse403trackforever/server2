package com.trackforever
import com.trackforever.models.TrackForeverComment
import com.trackforever.models.TrackForeverIssue
import com.trackforever.models.TrackForeverProject
import com.trackforever.repositories.ProjectRepository
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.boot.CommandLineRunner

@SpringBootApplication
class Application {
    @Bean
    fun init(projectRepository: ProjectRepository) = CommandLineRunner {
        val projWithNoIssues = TrackForeverProject(
                "a681f69cd2ebbb668a82989a6f594b4c81e8107134315bd1e9c06a13e7e1f9" +
                        "944b841a7fafe21e3a144f11fc1991981f6a2ea50447e5e7d1d3561542354b3a2f",
                "",
                "testproj1",
                "Will",
                "Test Project 1",
                "This is a very complex project 1.",
                "Google Code",
                mutableMapOf()
        )
        val issueNoComments = TrackForeverIssue(
                "59981dd809c5acd57515bef28acd7ac8e0ce17341e2ef7c2a46fe32a09f22b" +
                        "5334b12cdd72bc1822eac792201007c015223805ab48bfb1ef8e2dada1acf9ab21",
                "",
                "issue123",
                "testproj2",
                "fixed",
                "", listOf("defect", "bug"),
                listOf(),
                "Not Will",
                listOf("Will"),
                1234,
                1235,
                1337
        )
        val issueWithComments = TrackForeverIssue(
                "62666e72e0335a6f6a5cd9c655411009590e62af825a062662660f201d9202" +
                        "d973431d55016838dcf77aea079a5d6efe6940f715defb9e6da91259e5a7d7e2cc",
                "",
                "issue123comments",
                "testproj2",
                "wontfix",
                "",
                listOf("defect", "bug"),
                listOf(
                        TrackForeverComment("Will", "Why won't you fix this?"),
                        TrackForeverComment("Not Will", "Because I said so.")
                ),
                "Will",
                listOf(),
                null,
                null,
                null
        )
        val projWithIssues = TrackForeverProject(
                "74553d277cf8bd7e57c47ef79ab1c6c75bfd465e830bd71c90d6f8117fd174" +
                        "b007245c35b97fbe5f0bb2244eea80e397b35844c11253bff50dba379cb4f65de0",
                "1222",
                "testproj2",
                "Will",
                "Test Project 2",
                "This is a simple project 2.",
                "GitHub",
                mutableMapOf("issue123comments" to issueWithComments, "issue123" to issueNoComments)
        )

        projectRepository.save(projWithNoIssues)
        projectRepository.save(projWithIssues)
    }
}


fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}