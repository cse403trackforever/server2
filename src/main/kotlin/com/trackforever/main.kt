package com.trackforever
import com.trackforever.repositories.ProjectRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {
    @Bean
    fun init(projectRepository: ProjectRepository) = CommandLineRunner {
        projectRepository.deleteAll()
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}