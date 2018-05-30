package com.trackforever.config

import com.trackforever.Application
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc


@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {

    private val logger = LoggerFactory.getLogger(Application::class.java)

    override fun addCorsMappings(registry: CorsRegistry?) {

        registry!!.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")

        logger.debug("CORS mapping has been added!")

        // Add more mappings...
    }
}