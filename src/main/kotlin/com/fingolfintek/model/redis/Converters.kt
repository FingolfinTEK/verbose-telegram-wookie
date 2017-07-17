package com.fingolfintek.model.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.stereotype.Component

@Component
@ReadingConverter
open class BytesToDamagesByUserConverter(objectMapper: ObjectMapper) : Converter<ByteArray, List<RedisDamagesByUser>> {
  private val serializer = damagesSerializer(objectMapper)

  override fun convert(value: ByteArray): List<RedisDamagesByUser> {
    return serializer.deserialize(value)
  }
}

private fun damagesSerializer(objectMapper: ObjectMapper): Jackson2JsonRedisSerializer<List<RedisDamagesByUser>> {
  val type = TypeFactory.defaultInstance().constructCollectionType(List::class.java, RedisDamagesByUser::class.java)
  val serializer = Jackson2JsonRedisSerializer<List<RedisDamagesByUser>>(type)
  serializer.setObjectMapper(objectMapper)
  return serializer
}

@Component
@WritingConverter
class DamagesByUserToBytesConverter(objectMapper: ObjectMapper) : Converter<List<RedisDamagesByUser>, ByteArray> {
  private val serializer = damagesSerializer(objectMapper)

  override fun convert(value: List<RedisDamagesByUser>): ByteArray {
    return serializer.serialize(value)
  }
}
