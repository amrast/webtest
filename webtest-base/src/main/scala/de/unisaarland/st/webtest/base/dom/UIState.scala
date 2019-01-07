package de.unisaarland.st.webtest.base.dom

import java.io.FileNotFoundException
import java.nio.charset.Charset
import java.util.UUID

import de.unisaarland.st.webtest.base.TryThat
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import de.unisaarland.st.webtest.base.screenshot.{ImageUtils, Screenshot}
import de.unisaarland.st.webtest.base.utils.{TextAnalysisUtils, Tokenizer}
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.io.Codec
import scala.tools.nsc.interpreter.InputStream
import scala.tools.nsc.io.{Directory, File}
import scalaz.Success

class UIState(val id: UUID, val windowTitle: String, val elementData: ElementData, val protocolVersion: String, val screenshot: Option[Screenshot], var meta: JsObject) extends TextAnalysisUtils {

  def exportToDir(dir: Directory, createSubDir: Boolean = false, additionalProps: JsObject = Json.obj(), charset: Charset = Charset.forName("UTF-8")): TValidation[Unit] = TryThat.protect {
    val export = if (createSubDir) Directory(s"$dir/$id").createDirectory(force = true, failIfExists = false) else dir.createDirectory(force = true, failIfExists = false)
    val json = Json.toJson(elementData)(ElementData.elementDataWrites)
    val e = new String(Json.prettyPrint(json).getBytes(charset))
    File(s"$export/state-${id.toString}.json").createFile(true).writeAll(e)
    screenshot.map(_.data).flatMap(img => ImageUtils.arraytoPNG(img).toOption).foreach(img => ImageUtils.imgToFile(img, File(s"$export/$id.png").createFile(true)))
    val metaData = File(s"$export/$id-metadata.json").createFile(true)
    metaData.writeAll(new String(Json.prettyPrint(meta ++ Json.obj("windowTitle" -> windowTitle, "protocolVersion" -> protocolVersion) ++ additionalProps).getBytes(charset)))
    Success(())
  }

  def addMetaData(jsObject: JsObject): Unit = {
    meta = meta.deepMerge(jsObject)
  }

  def getTags: Seq[String] = (meta \ "tag").asOpt[Seq[String]].getOrElse(Seq("undefined"))


  /**
    * Gets all elements, which match the given string after sanitization.
    */
  def getElementsForString(string: String): Iterable[ElementData] = {
    val str = Tokenizer.sanitizeString(string)

    elementData.filter { e =>
      val s = Tokenizer.sanitizeString(e.getTextContent())
      s.equalsIgnoreCase(str)
    }
  }

  def getNLPElements: Iterable[String] = {
    elementData.filter(hasNaturalLanguageContent) flatMap { x =>
      val string = Tokenizer.sanitizeString(x.getTextContent())
      if (string.nonEmpty) Some(string) else None
    }
  }

  def getElements: Iterator[ElementData] = elementData.iterator()

  lazy val nlpElements: Iterable[String] = getNLPElements


}

object UIState {

  def apply(id: UUID, elementData: ElementData, screenshot: Option[Screenshot], meta: JsValue): UIState = {
    val protocolVersion = (meta \ "protocolVersion").as[String]
    val windowTitle = (meta \ "windowTitle").as[String]

    new UIState(id, windowTitle, elementData, protocolVersion, screenshot, meta.as[JsObject])
  }

  def apply(id: UUID, elementDataFile: File, screenshot: Option[Screenshot], meta: JsValue): UIState = {
    if (elementDataFile.exists && elementDataFile.canRead) {

      val jsValue = try {
        Json.parse(elementDataFile.inputStream())
      } catch {
        case _: com.fasterxml.jackson.core.JsonParseException  => Json.parse(elementDataFile.lines(Codec("Windows-1252")).mkString(System.lineSeparator()).getBytes("UTF-8"))
        case a: Throwable => println(s"Unable to process ${a.getClass.getSimpleName}")
          throw a
      }

      val elementData = Json.fromJson[ElementData](jsValue)(ElementData.elementDataReads).get
      UIState(id, elementData, screenshot, meta)
    } else {
      throw new FileNotFoundException(s"ElementdataFile ${elementDataFile.path} does not exists")
    }
  }

  def apply(id: UUID, elementData: InputStream, screenshot: Option[InputStream], meta: InputStream): UIState = {
    val ele = Json.fromJson[ElementData](Json.parse(elementData))(ElementData.elementDataReads).get
    val maybeScreenshot = screenshot.flatMap(a => ImageUtils.isToScreenshot(a).toOption)
    UIState(id, ele, maybeScreenshot, Json.parse(meta) )
  }

  def apply(id: UUID, elementData: File, screenshot: File, meta: JsValue): UIState = {
    val maybeFile = if (screenshot.exists) Some(ImageUtils.fileToScreenshot(screenshot).toOption).flatten else None
    UIState(id, elementData, maybeFile, meta)
  }

  def apply(id: UUID, elementData: String, screenshotOpt: Option[Array[Byte]] = None, meta: String = ""): UIState = {
    val ele = Json.fromJson[ElementData](Json.parse(elementData))(ElementData.elementDataReads).get
    val maybeScreenshot: Option[Screenshot] = screenshotOpt.flatMap(screenshot => ImageUtils.arrayToScreenShot(screenshot).toOption)
    UIState(id, ele, maybeScreenshot, if (meta.isEmpty) Json.obj() else Json.parse(meta))
  }

}
