package de.unisaarland.st.webtest.base.dom

import play.api.libs.json.Json

case class EventHandler(eventType: String, functionCode: String, library: String)

object EventHandler {

  implicit val eventHandlerFormat = Json.format[EventHandler]

}
