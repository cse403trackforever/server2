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
        )) { "Issue: \"issue123comments\" is stored incorrectly." }
        assert(projectRepository.findById("testproj2").get().issues["issue123"] == TrackForeverIssue(
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
    fun findByHashTest() {
        val testproj1 = projectRepository.findByHash("a681f69cd2ebbb668a82989a6f594b4c81e8107134315bd1e9c06a13e7e1f9" +
                "944b841a7fafe21e3a144f11fc1991981f6a2ea50447e5e7d1d3561542354b3a2f")
        assert(testproj1 == TrackForeverProject(
                "a681f69cd2ebbb668a82989a6f594b4c81e8107134315bd1e9c06a13e7e1f9" +
                        "944b841a7fafe21e3a144f11fc1991981f6a2ea50447e5e7d1d3561542354b3a2f",
                "",
                "testproj1",
                "Will",
                "Test Project 1",
                "This is a very complex project 1.",
                "Google Code",
                mutableMapOf()
        ))
        val testproj2 = projectRepository.findByHash("74553d277cf8bd7e57c47ef79ab1c6c75bfd465e830bd71c90d6f8117fd174" +
                "b007245c35b97fbe5f0bb2244eea80e397b35844c11253bff50dba379cb4f65de0")
        assert(testproj2.prevHash == "1222")
        assert(testproj2.id == "testproj2")
        assert(testproj2.ownerName == "Will")
        assert(testproj2.description == "This is a simple project 2.")
        assert(testproj2.source == "GitHub")
        assert(testproj2.issues.size == 2)
        assert(testproj2.issues["issue123comments"] != null)
        assert(testproj2.issues["issue123comments"] == TrackForeverIssue(
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
        ))
        assert(testproj2.issues["issue123"] != null)
        assert(testproj2.issues["issue123"] == TrackForeverIssue(
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
        ))
    }

    @Test
    fun deleteByHashTest() {
        projectRepository.deleteByHash("a681f69cd2ebbb668a82989a6f594b4c81e8107134315bd1e9c06a13e7e1f9" +
                "944b841a7fafe21e3a144f11fc1991981f6a2ea50447e5e7d1d3561542354b3a2f")
        assert(projectRepository.findAll().size == 1)
        projectRepository.deleteByHash("74553d277cf8bd7e57c47ef79ab1c6c75bfd465e830bd71c90d6f8117fd174" +
                "b007245c35b97fbe5f0bb2244eea80e397b35844c11253bff50dba379cb4f65de0")
        assert(projectRepository.findAll().size == 0)
    }

    @Test
    fun removeProjectTest() {
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