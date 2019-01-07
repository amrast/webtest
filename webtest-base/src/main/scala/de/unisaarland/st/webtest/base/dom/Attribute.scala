package de.unisaarland.st.webtest.base.dom


import play.api.libs.json.Json

case class Attribute(name: String, value: String)
object Attribute {
  implicit val attributeFormat = Json.format[Attribute]
}
