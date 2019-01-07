package de.unisaarland.st.webtest.base.screenshot

import java.io.ByteArrayInputStream
import java.util.UUID
import javax.imageio.ImageIO

import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import de.unisaarland.st.webtest.base.screenshot.ImageFormat.ImageFormat
import org.joda.time.DateTime
import play.api.libs.json._

import scalaz.{Failure, Success}

case class Screenshot(id: UUID, created: DateTime, data: Array[Byte], width: Int, height: Int, format: ImageFormat)

object ImageFormat extends Enumeration {

  type ImageFormat = Value
  val PNG = Value
  val JPG = Value

  implicit val imageFormatFormat = new Format[ImageFormat] {
    override def writes(o: ImageFormat): JsValue = new JsString("image/" + o.toString.toLowerCase)

    override def reads(json: JsValue): JsResult[ImageFormat] = Json.fromJson[String](json).asOpt match {
      case Some(form) => JsSuccess(ImageFormat.withName(form.split("/")(1).toUpperCase))
      case None => JsError(s"Input value is not conform to specification ${json.toString()}")
    }
  }

  def computeScreenShotDimensions(data: Array[Byte], format: ImageFormat): TValidation[(Int, Int)] = format match {
    case ImageFormat.PNG => val image = ImageIO.read(new ByteArrayInputStream(data))
      Success((image.getWidth, image.getHeight))
    case _ => Failure(new RuntimeException(s"No decoder known for type $format"))
  }

  def getString(format: ImageFormat): String = "image/" + format.toString.toLowerCase

}