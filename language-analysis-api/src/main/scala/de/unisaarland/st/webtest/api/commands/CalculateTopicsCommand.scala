package de.unisaarland.st.webtest.api.commands

import play.api.libs.json.{JsValue, Json}

object CalculateTopicsCommand {
  implicit val calculateTopicsCommandFormat = Json.format[CalculateTopicsCommand]
}

case class CalculateTopicsCommand(str: String, numTopics: Int, numWords: Int) {
  def toJson: JsValue = Json.toJson(this)
}
