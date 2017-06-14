package com.fingolfintek.ocr

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

    fun resolveDamageFrom(imagePath: String): Int {
        val originalImage = lept.pixRead(imagePath)
        val scaledImage = lept.pixScaleColor4xLI(originalImage)
        val bitwiseImage = lept.pixConvertTo1(scaledImage, 128)
        try {
            val damage = parseDamageFrom(bitwiseImage)
            return Integer.parseInt(damage)
        } finally {
            lept.pixDestroy(bitwiseImage)
            lept.pixDestroy(scaledImage)
            lept.pixDestroy(originalImage)
        }
    }

    private fun parseDamageFrom(image: lept.PIX?): String {
        val tesseract = initializeTesseract()
        tesseract.SetImage(image)
        val outText = tesseract.GetUTF8Text()
        try {
            return outText.string
                    .split(" ").last()
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
