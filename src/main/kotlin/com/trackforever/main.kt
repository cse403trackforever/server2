package com.trackforever

import com.trackforever.Application.Companion.arguments
import com.trackforever.repositories.ProjectRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {
    @Bean
    fun init(projectRepository: ProjectRepository) = CommandLineRunner {
        if (arguments.contains("clearAll")) {
            logger.debug("CLEARING ALL DATA")
            projectRepository.deleteAll()
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(Application::class.java)
        lateinit var arguments: Array<String>
    }
}

fun main(args: Array<String>) {
    arguments = args
    SpringApplication.run(Application::class.java, *args)
}