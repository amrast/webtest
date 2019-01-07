package de.unisaarland.st.webtest.base.utils

import de.unisaarland.st.webtest.base.dom.ElementData

import scala.util.matching.Regex

trait TextAnalysisUtils {

  def hasNaturalLanguageContent(e: ElementData): Boolean = {
   val x =  TextAnalysisUtils.legalElementTypes.exists(_.pattern.matcher(e.nodeName).matches()) &&
    e.hasTextContent &&
      e.getTextContent().trim.nonEmpty

    x
  }


}

object TextAnalysisUtils extends TextAnalysisUtils {
  private val legalElementTypes: Set[Regex] = Set("div", "input", "button", "h[0-9]+", "span", "a", "form", "select", "option", "li", "label", "i", "br", "p").map(_.r)

}
