package com.fingolfintek.ocr

import com.fingolfintek.util.using
import org.bytedeco.javacpp.lept
import org.bytedeco.javacpp.tesseract.TessBaseAPI
import org.springframework.stereotype.Component
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import java.net.URL
import javax.annotation.PostConstruct

@Component
open class TotalDamageResolver {
  private val DATA_LOCATION =
      "https://github.com/tesseract-ocr/tessdata/raw/master/eng.traineddata"

  lateinit var tessData: File

  @PostConstruct
  fun initialize() {
    val tempDir = File(createTempDir(), "tessdata")
    tempDir.mkdirs()
    tessData = File(tempDir, "eng.traineddata")
    URL(DATA_LOCATION).openStream().copyTo(FileOutputStream(tessData))
    tessData.deleteOnExit()
  }

  fun lept.PIX.cleanup() = lept.pixDestroy(this)

  fun lept.PIX.imageWithoutContinueButton(): lept.PIX =
      lept.pixRemoveBorderToSize(this, this.w(), this.h() / 3)

  fun lept.PIX.scaledForOCR(): lept.PIX = when {
    this.w() < 1000 -> lept.pixScaleColor4xLI(this)
    this.w() < 2000 -> lept.pixScaleColor2xLI(this)
    else -> this
  }

  fun lept.PIX.asBlackAndWhite(): lept.PIX =
      lept.pixConvertTo1(this, 128)

  fun resolveDamageFrom(imagePath: String): Int {
    return using {
      val image = lept.pixRead(imagePath).autoClose { cleanup() }
      val croppedImage = image.imageWithoutContinueButton().autoClose { cleanup() }
      val scaledImage = croppedImage.scaledForOCR().autoClose { cleanup() }
      val bitwiseImage = scaledImage.asBlackAndWhite().autoClose { cleanup() }
      val damage = parseDamageFrom(bitwiseImage)
      Integer.parseInt(damage)
    }
  }

  private fun parseDamageFrom(image: lept.PIX?): String {
    val tesseract = initializeTesseract()
    tesseract.SetImage(image)
    val outText = tesseract.GetUTF8Text()
    try {
      return outText.string.trim()
          .splitToSequence(" ", "\n").last()
          .trim().replace(Regex("[,.]"), "")
    } finally {
      outText.deallocate()
      tesseract.End()
    }
  }

  private fun initializeTesseract(): TessBaseAPI {
    val tesseract = TessBaseAPI()

    if (tesseract.Init(tessData.parentFile.absolutePath, "eng") != 0)
      throw IllegalArgumentException("Could not initialize tesseract.")

    tesseract.SetVariable("tessedit_char_whitelist", "0123456789,.")
    return tesseract
  }
}
