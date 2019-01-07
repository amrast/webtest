package de.unisaarland.st.webtest.base.dom

import javax.xml.xpath.{XPathConstants, XPathFactory}

import org.w3c.dom.{Node, NodeList}

case class XPathExpression(expression: String) extends Comparable[XPathExpression] {

  /** emulate behavior of original Java Class */
  if (expression == null || expression.length <= 0)
    throw new IllegalArgumentException("Given String argument must not be null nor empty.")
  if (expression.length > XPathUtilities.XPATH_MAX_LENGTH)
    throw new IllegalArgumentException(s"Given String argument must not exceed maximal length ${XPathUtilities.XPATH_MAX_LENGTH}")


  override def toString(): String = s"XPathExpression [xPathExpression=$expression]"
  override def compareTo(o: XPathExpression): Int = expression.compareTo(o.expression)
}

object XPathUtilities {

  val XPATH_MAX_LENGTH: Int = 2048

  def executeXPathQuery(node : Node, xPath : XPathExpression) : NodeList = executeXPathQuery(node, xPath.expression)

  /**
    * Execute XPath Query on a JAXP node.
    *
    * @param node
    *            The node to execute the query on
    * @param xPath
    *            The xpath query
    * @return A list of nodes for all expressions that match the query
    * @throws javax.xml.xpath.XPathExpressionException
    */
  def executeXPathQuery(node : Node, xPath : String) : NodeList = {
    // XXX Can we cache XPathFactory instance?
    val xpath = XPathFactory.newInstance().newXPath();
    val expr = xpath.compile(xPath);
    expr.evaluate(node, XPathConstants.NODESET).asInstanceOf[NodeList]
  }

}