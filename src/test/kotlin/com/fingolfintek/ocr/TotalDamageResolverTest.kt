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
    expectDamageForImage("/screenshot.png", 359668)
    expectDamageForImage("/screenshot_2.png", 9590)
    expectDamageForImage("/screenshot_3.png", 9456)
    expectDamageForImage("/screenshot_4.jpeg", 8579)
    expectDamageForImage("/screenshot_5.png", 16040)
  }

  private fun expectDamageForImage(path: String, expectedDamage: Int) {
    val resource = TotalDamageResolver::class.java.getResource(path)
    val resolvedDamage = resolver.resolveDamageFrom(resource.file)
    Assertions.assertThat(resolvedDamage).isEqualTo(expectedDamage)
  }

}
