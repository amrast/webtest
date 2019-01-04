package de.unisaarland.st.webtest.api

import play.api.libs.json._

import scala.language.postfixOps

/**
  * A generic trait for supported languages.
  */
sealed trait Language {
  val name: String
}

object Language {

  implicit val languageFormat = new Format[Language] {
    override def reads(json: JsValue): JsResult[Language] =
      try {
        JsSuccess(Language(json.as[String]))
      } catch {
        case m: MatchError => JsError(s"Unsupported Language String ${Json.prettyPrint(json)}")
        case result: JsResultException => JsError(s"Unable to parse given value ${Json.prettyPrint(json)} into a language string")
      }


    override def writes(o: Language): JsValue = JsString(o.name)

  }

  def apply(str: String): Language = {
    str toLowerCase match {
      case English_US.name => English_US
      case German.name => German
    }
  }
}

case object English_US extends Language {
  override val name: String = "en"
}

case object German extends Language {
  override val name: String = "de"
}
