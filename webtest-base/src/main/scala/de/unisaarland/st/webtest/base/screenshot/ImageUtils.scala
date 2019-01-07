package de.unisaarland.st.webtest.base.screenshot

import java.awt.image.BufferedImage
import java.awt.{Color, Font}
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import java.util.UUID
import javax.imageio.ImageIO

import de.unisaarland.st.webtest.base.TryThat
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import de.unisaarland.st.webtest.base.dom.Rectangle
import org.joda.time.DateTime

import scala.tools.nsc.io.File
import scalaz.Success

object ImageUtils {

  def arraytoPNG(array: Array[Byte]): TValidation[BufferedImage] = TryThat.protect {
    val img = ImageIO.read(new ByteArrayInputStream(array))
    Success(img)
  }

  def arrayToScreenShot(data: Array[Byte]): TValidation[Screenshot] = {
    arraytoPNG(data).map { img =>
      Screenshot(UUID.randomUUID(), DateTime.now, data, img.getWidth, img.getHeight, ImageFormat.PNG)
    }
  }

  def fileToScreenshot(f: File): TValidation[Screenshot] = {
    isToScreenshot(f.bufferedInput())
  }

  def isToScreenshot(is: InputStream): TValidation[Screenshot] = {
    val original = ImageIO.read(is)
    val baos = new ByteArrayOutputStream()
    ImageIO.write( original, "jpg", baos )
    baos.flush()
    val imageInByte = baos.toByteArray
    baos.close()
    arrayToScreenShot(imageInByte)
  }

  def imgToFile(img: BufferedImage, file: File, format: String = "png"): Unit = {
    ImageIO.write(img, format, file.jfile)
  }

  def drawRectangle(img: BufferedImage, rectangles: Map[Rectangle, Color]): Unit = {
    val graphics = img.createGraphics()

    rectangles.foreach { case (r, c) =>
      graphics.setColor(c)
      graphics.drawRect(r.x, r.y, r.width, r.height)
    }
    graphics.dispose()
  }

  def imgFromFile(file: File): BufferedImage = {
    Option(ImageIO.read(file.inputStream())) match {
      case Some(img) => img
      case None => throw new RuntimeException(s"Given img $file is not readable.")
    }
  }

  def writeTextOnImage(img: BufferedImage, x: Int, y: Int, s: String, optSize: Option[Int] = None): Unit = {
    val graphics = img.createGraphics()
    optSize.foreach { size =>
      val dfont = graphics.getFont
      graphics.setColor(Color.BLUE)
      graphics.setFont(new Font(dfont.getFontName, dfont.getStyle, size))
    }

    // Assert that the text is actually readable on the image
    val finalTextSize = graphics.getFont.getSize
    val posX = if (finalTextSize < x) x else finalTextSize + 1
    val posY = if (finalTextSize < y) y else finalTextSize + 1

    graphics.drawString(s, posX, posY)
    graphics.dispose()
  }

  def saveToFile(bufferedImage: BufferedImage, file: File, format: String = "png"): Unit = {
    val createFile = file.createFile()
    ImageIO.write(bufferedImage, format, createFile.jfile)
  }



}
