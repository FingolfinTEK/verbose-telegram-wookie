package com.fingolfintek.session.redis

import com.fasterxml.jackson.databind.ObjectMapper
import io.vavr.Tuple
import io.vavr.Tuple2
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.stereotype.Component

open class DamagesByUser(val username: String, val damages: List<Int>) {
  fun toTuple(): Tuple2<String, List<Int>> = Tuple.of(username, damages)
}

fun fromSessionDamages(damagesByUsers: LinkedHashMap<String, List<Int>>): List<DamagesByUser> =
    damagesByUsers.map { DamagesByUser(it.key, it.value) }.toList()

@Component @WritingConverter
open class DamagesToBytesConverter(objectMapper: ObjectMapper) : Converter<DamagesByUser, ByteArray> {
  private val serializer = GenericJackson2JsonRedisSerializer(objectMapper)
  override fun convert(damages: DamagesByUser): ByteArray = serializer.serialize(damages)
}

@Component @ReadingConverter
open class BytesToDamagesConverter(objectMapper: ObjectMapper) : Converter<ByteArray, DamagesByUser> {
  private val serializer = GenericJackson2JsonRedisSerializer(objectMapper)
  override fun convert(damages: ByteArray): DamagesByUser =
      serializer.deserialize(damages, DamagesByUser::class.java)
}