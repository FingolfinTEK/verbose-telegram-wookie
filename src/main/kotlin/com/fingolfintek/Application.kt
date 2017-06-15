package com.fingolfintek

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fingolfintek.bot.BotProperties
import io.vavr.jackson.datatype.VavrModule
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories


@SpringBootApplication
@EnableRedisRepositories
@EnableConfigurationProperties(BotProperties::class)
open class Application {
  @Bean open fun kotlinModule() = KotlinModule()

  @Bean open fun objectMapper(): ObjectMapper =
      ObjectMapper()
          .registerModule(KotlinModule())
          .registerModule(JavaTimeModule())
          .registerModule(VavrModule())

  @Bean open fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<*, *> {
    val template = RedisTemplate<ByteArray, ByteArray>()
    template.connectionFactory = connectionFactory
    return template
  }
}

fun main(args: Array<String>) {
  SpringApplication.run(Application::class.java, *args)
}
