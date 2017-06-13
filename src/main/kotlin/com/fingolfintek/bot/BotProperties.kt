package com.fingolfintek.bot

@org.springframework.boot.context.properties.ConfigurationProperties("discord.bot")
open class BotProperties {
  var token = ""
  var officerRegexPattern = "officer|leader"
  private lateinit var officerRegex: Regex

  @javax.annotation.PostConstruct
  private fun initialize() {
    officerRegex = officerRegexPattern.toRegex(RegexOption.IGNORE_CASE)
  }

  fun isAuthorAnOfficer(message: net.dv8tion.jda.core.entities.Message): Boolean {
    return message.member.roles
        .filter { it.name.contains(officerRegex) }
        .isNotEmpty()
  }
}