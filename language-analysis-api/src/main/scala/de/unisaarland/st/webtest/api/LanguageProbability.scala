package de.unisaarland.st.webtest.api

import play.api.libs.json._
import Language.languageFormat

case class LanguageProbability(l: Language, probability: Double)

object LanguageProbability {

  implicit val languageProbabilityFormat = new Format[LanguageProbability] {
    override def writes(o: LanguageProbability) = Json.obj("language" -> Json.toJson(o.l), "probability" -> Json.toJsFieldJsValueWrapper(o.probability))

    override def reads(json: JsValue) = {
      json match {
        case obj: JsObject =>
          JsSuccess(LanguageProbability((obj \ "language").as[Language], (obj \ "probability").as[Double]))
        case _ => JsError(s"Given json is no JsObject, but ${json.getClass}")
      }
    }

  }
}