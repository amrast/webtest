package de.unisaarland.st.webtest.nlp.languagedetect.xml

import java.io.FileNotFoundException
import javax.xml.parsers.SAXParserFactory

import de.unisaarland.st.webtest.base.{Logging, TryThat}
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import org.xml.sax.helpers.DefaultHandler

import scala.collection.mutable
import scalaz.{Failure, Success}

abstract class ScalaResourceHandler extends DefaultHandler { self: Logging =>

  private val mapping = mutable.HashMap.empty[String, String]

  protected def addItem(key: String, value: String): Unit = mapping.put(key, value)

  def parseResource(resourceFile: scala.tools.nsc.io.File): TValidation[Map[String, String]] = {
    if (!resourceFile.exists)
      Failure(new FileNotFoundException(s"Couldn't find file for parsing at ${resourceFile.path}"))
    else {
      val factory = SAXParserFactory.newInstance()
      TryThat.protect  {
        val parser = factory.newSAXParser()
        parser.parse(resourceFile.jfile, this)
        Success(mapping.toMap)
      }
    }
  }

}
