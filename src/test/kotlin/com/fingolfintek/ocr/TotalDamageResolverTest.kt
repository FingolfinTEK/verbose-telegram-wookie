package com.fingolfintek.ocr

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class TotalDamageResolverTest {

    lateinit var resolver: TotalDamageResolver

    @Before
    fun setUp() {
        resolver = TotalDamageResolver()
        resolver.initialize()
    }

    @Test
    fun resolveDamageFrom() {
        val resource = TotalDamageResolver::class.java.getResource("/screenshot.png")
        val damage = resolver.resolveDamageFrom(resource.file)
        Assertions.assertThat(damage).isEqualTo(359668)
    }

}
