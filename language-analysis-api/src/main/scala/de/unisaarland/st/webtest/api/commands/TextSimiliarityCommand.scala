package de.unisaarland.st.webtest.api.commands

import play.api.libs.json.{JsValue, Json}

object TextSimiliarityCommand {
  implicit val textsimilaritycommandformat = Json.format[TextSimiliarityCommand]
}

case class TextSimiliarityCommand(text1: String, text2: String) {
  def toJson: JsValue = Json.toJson(this)
}

