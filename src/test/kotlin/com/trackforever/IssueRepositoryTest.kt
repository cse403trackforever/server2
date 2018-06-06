package com.trackforever

import com.trackforever.models.TrackForeverComment
import com.trackforever.models.TrackForeverIssue
import com.trackforever.repositories.IssueRepository
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest
class IssueRepositoryTest {
    @Autowired
    lateinit var issueRepository: IssueRepository

    @Before
    fun setup() {
        val issueNoComments = TrackForeverIssue(
                "59981dd809c5acd57515bef28acd7ac8e0ce17341e2ef7c2a46fe32a09f22b" +
                        "5334b12cdd72bc1822eac792201007c015223805ab48bfb1ef8e2dada1acf9ab21",
                "",
                "issue123",
                "testproj1",
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
        val anotherIssueWithComments = TrackForeverIssue(
                "fakeHash",
                "",
                "issue1234comments",
                "testproj2",
                "wontfix",
                "",
                listOf("error", "bug"),
                listOf(
                        TrackForeverComment("Will", "Why won't you fix this?"),
                        TrackForeverComment("Not Will", "Because I said so.")
                ),
                "Will",
                listOf(),
                1337L,
                null,
                null
        )
        issueRepository.save(issueNoComments)
        issueRepository.save(issueWithComments)
        issueRepository.save(anotherIssueWithComments)
    }

    @Test
    fun issuesExistTest() {
        assert(!issueRepository.findAll().isEmpty())
        assert(issueRepository.findAll().size == 3)
    }

    @Test
    fun findByIdTest() {
        assert(issueRepository.findById(IssueKey("testproj1", "issue123")).get() == TrackForeverIssue(
                "59981dd809c5acd57515bef28acd7ac8e0ce17341e2ef7c2a46fe32a09f22b" +
                        "5334b12cdd72bc1822eac792201007c015223805ab48bfb1ef8e2dada1acf9ab21",
                "",
                "issue123",
                "testproj1",
                "fixed",
                "", listOf("defect", "bug"),
                listOf(),
                "Not Will",
                listOf("Will"),
                1234,
                1235,
                1337
        ))
        assert(issueRepository.findById(IssueKey("testproj2", "issue123comments")).get() == TrackForeverIssue(
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
        assert(issueRepository.findById(IssueKey("testproj2", "issue123comments")).get() == TrackForeverIssue(
                "fakeHash",
                "",
                "issue1234comments",
                "testproj2",
                "wontfix",
                "",
                listOf("error", "bug"),
                listOf(
                        TrackForeverComment("Will", "Why won't you fix this?"),
                        TrackForeverComment("Not Will", "Because I said so.")
                ),
                "Will",
                listOf(),
                1337L,
                null,
                null
        ))
    }

    @Test
    fun findByProjectIdTest() {
        assert(!issueRepository.findByProjectId("testproj1").isEmpty())
        assert(issueRepository.findByProjectId("testproj1").size == 1)
        assert(issueRepository.findByProjectId("testproj1")[0] == TrackForeverIssue(
                "59981dd809c5acd57515bef28acd7ac8e0ce17341e2ef7c2a46fe32a09f22b" +
                        "5334b12cdd72bc1822eac792201007c015223805ab48bfb1ef8e2dada1acf9ab21",
                "",
                "issue123",
                "testproj1",
                "fixed",
                "", listOf("defect", "bug"),
                listOf(),
                "Not Will",
                listOf("Will"),
                1234,
                1235,
                1337
        ))
        assert(!issueRepository.findByProjectId("testproj2").isEmpty())
        assert(issueRepository.findByProjectId("testproj2").size == 2)
        assert(issueRepository.findByProjectId("testproj2").contains(TrackForeverIssue(
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
        )))
        assert(issueRepository.findByProjectId("testproj2").contains(TrackForeverIssue(
                "fakeHash",
                "",
                "issue1234comments",
                "testproj2",
                "wontfix",
                "",
                listOf("error", "bug"),
                listOf(
                        TrackForeverComment("Will", "Why won't you fix this?"),
                        TrackForeverComment("Not Will", "Because I said so.")
                ),
                "Will",
                listOf(),
                1337L,
                null,
                null
        )))
    }

    @Test
    fun deleteByIdTest() {
        assert(issueRepository.findAll().size == 3)
        issueRepository.deleteById(IssueKey("testproj2", "issue123comments"))
        assert(issueRepository.findAll().size == 2)
        assert(!issueRepository.findByProjectId("testproj2").contains(TrackForeverIssue(
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
        )))
        issueRepository.deleteById(IssueKey("testproj2", "issue1234comments"))
        assert(!issueRepository.findByProjectId("testproj2").isEmpty())
        assert(issueRepository.findAll().size == 1)
        issueRepository.deleteById(IssueKey("testproj1", "issue123"))
        assert(issueRepository.findAll().size == 0)
    }

    @Test
    fun deleteAllTest() {
        issueRepository.deleteAll()
        assert(issueRepository.findAll().isEmpty())
    }

    @After
    fun cleanUp() {
        issueRepository.deleteAll()
        assert(issueRepository.findAll().size == 0)
    }
}