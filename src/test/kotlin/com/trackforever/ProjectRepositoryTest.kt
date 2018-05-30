package com.trackforever

import com.trackforever.models.TrackForeverComment
import com.trackforever.models.TrackForeverIssue
import com.trackforever.models.TrackForeverProject
import com.trackforever.repositories.ProjectRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest
class ProjectRepositoryTest {
    @Autowired
    lateinit var projectRepository: ProjectRepository

    @Before
    fun setup() {
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

    @Test
    fun projectFindAllTest() {
        assert(projectRepository.findAll().size == 2) { "There should be two projects." }
        assert(projectRepository.findById("testproj1").isPresent) { "Missing testproj1" }
        assert(projectRepository.findById("testproj2").isPresent) { "Missing testproj2" }
    }

    @Test
    fun issuesExistTest() {
        assert(projectRepository.findById("testproj1").get().issues.isEmpty()) { "testproj1 should contain 0 issues" }
        assert(projectRepository.findById("testproj2").get().issues.size == 2) { "testproj2 should contain 2 issues" }
        assert(projectRepository.findById("testproj2").get().issues["issue123comments"] != null) { "Issue: \"issue123comments\" missing from testproj2" }
    }

    @Test
    fun issuesCorrectTest() {
        assert(projectRepository.findById("testproj2").get().issues["issue123"] != null) { "Issue: \"issue123\" missing from testproj2" }
        assert(projectRepository.findById("testproj2").get().issues["issue123comments"] == TrackForeverIssue(
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
        )) { "Issue: \"issue123comments\" is stored incorrectly." }
        assert(projectRepository.findById("testproj2").get().issues["issue123"] == TrackForeverIssue(
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
        )) { "Issue: \"issue123\" is stored incorrectly." }
    }

    @Test
    fun commentsExistTest() {
        assert(projectRepository.findById("testproj2").get().issues.get("issue123comments")!!.comments.size == 2) { "Issue: \"issue123comments\" should have two comments." }
    }

    @Test
    fun commentsCorrectTest() {
        val issueWithComments = projectRepository.findById("testproj2").get().issues.get("issue123comments")!!
        assert(issueWithComments.comments[0] == TrackForeverComment("Will", "Why won't you fix this?")) { "Comment mismatch in issue." }
        assert(issueWithComments.comments[1] == TrackForeverComment("Not Will", "Because I said so.")) { "Comment mismatch in issue." }
    }

    @Test
    fun projectRemoveProjectTest() {
        projectRepository.deleteById("testproj1")
        assert(projectRepository.findAll().size == 1) { "Size incorrect after deletion." }
        projectRepository.deleteById("testproj2")
        assert(projectRepository.findAll().size == 0) { "Size incorrect after deletion." }
    }


    @After
    fun cleanUp() {
        projectRepository.deleteAll()
        assert(projectRepository.findAll().size == 0)
    }
}