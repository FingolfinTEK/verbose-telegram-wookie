package com.fingolfintek

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fingolfintek.bot.BotProperties
import io.vavr.jackson.datatype.VavrModule
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.convert.converter.Converter
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.convert.CustomConversions
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.scheduling.annotation.EnableScheduling


@EnableScheduling
@SpringBootApplication
@EnableRedisRepositories
@EnableConfigurationProperties(BotProperties::class)
open class Application {

  @Bean open fun objectMapper(): ObjectMapper =
      ObjectMapper()
          .registerModule(KotlinModule())
          .registerModule(JavaTimeModule())
          .registerModule(VavrModule())

  @Bean open fun redisCustomConversions(converters: List<Converter<*, *>>): CustomConversions =
      CustomConversions(converters)

  @Bean open fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<*, *> {
    val template = RedisTemplate<ByteArray, ByteArray>()
    template.connectionFactory = connectionFactory
    return template
  }

  @Bean open fun discordBotContext(properties: BotProperties): JDA =
      JDABuilder(AccountType.BOT)
          .setToken(properties.token)
          .setAutoReconnect(true)
          .buildAsync()
}

fun main(args: Array<String>) {
  SpringApplication.run(Application::class.java, *args)
}
