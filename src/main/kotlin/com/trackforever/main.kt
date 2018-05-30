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
                "1234",
                "",
                "testproj1",
                "Will",
                "Test Project 1",
                "This is a very complex project 1.",
                "Google Code",
                mutableMapOf()
        )
        val issueNoComments = TrackForeverIssue(
                "issueHash123",
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
                "issueHash123comments",
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
                "1337",
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