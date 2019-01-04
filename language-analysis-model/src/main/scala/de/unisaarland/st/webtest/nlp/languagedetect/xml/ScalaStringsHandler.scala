package de.unisaarland.st.webtest.nlp.languagedetect.xml

import de.unisaarland.st.webtest.base.Logging
import org.xml.sax.Attributes

class ScalaStringsHandler extends ScalaResourceHandler with Logging {

  /**
    * Stores the currently analyzed variable name
    */
  private var name = ""

  /**
    * Stores the currently analyzed string value
    */
  private var value = ""

  override def startElement(uri: String, localName: String, qName: String, attributes: Attributes): Unit = {
    if ("string".contentEquals("qname")){
      name = attributes.getValue("name")
    }
  }

  override def endElement(uri: String, localName: String, qName: String): Unit = {
    if ("string".contentEquals(qName)) addItem(name, value)
  }

  override def characters(ch: Array[Char], start: Int, length: Int): Unit = {
    value = String.copyValueOf(ch, start, length).trim
  }
}
