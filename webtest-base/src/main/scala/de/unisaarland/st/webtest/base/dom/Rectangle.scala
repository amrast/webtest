package de.unisaarland.st.webtest.base.dom

import play.api.libs.json._


/**
  * Utility class to represent rectangles
  * @param x x-axis coordinate
  * @param y y-axis coordinate
  * @param width non-negative width of the element (x-axis), provide negative value if you don't know it
  * @param height non-negative height of the element (y-axis)
  * Note: negative values for width and height are accepted, but treated as 0 in all the computations (eg: x2, y2, getArea)
  */
case class Rectangle(x: Int, y: Int, width: Int, height: Int) {

  /**
    * x coordinate of right bottom corner
    */
  val x2 = if (width > 0) x + width else x

  /**
    * y coordinate of right bottom corner
    */
  val y2 = if (height > 0) y + height else y

  lazy val middle: (Double, Double) = (if (width > 0) x.toDouble + (width.toDouble / 2.0) else x.toDouble, if (height > 0) y.toDouble + (height.toDouble / 2) else y.toDouble)

  def contains(other: Rectangle): Boolean = other.x >= x && other.y >= y && other.x2 <= x2 && other.y2 <= y2
  def contains(xo: Int, yo: Int): Boolean = xo >= x && yo >= y && xo <= x2 && yo <= y2

  def distance(other: Rectangle): Double = math.sqrt(math.pow(x.toDouble - other.x.toDouble, 2) + math.pow(y.toDouble - other.y.toDouble, 2))

  def getCoordinates: String = s"($x,$y)"

  def getArea: Int = if (width > 0 && height > 0) width * height else 0

  /**
    * Helper-Method to check if two Rectangles overlap, *NOTE THAT* only actual area overlapping is counted. If only points or edges are shared,
    * it is not counted as an overlap. This makes sense because HTML object naturally share edges and corners and we do not want neighbours to
    * be counted as overlapping all the time...
    *
    * @param other: another rectangle
    * @return 'true' if this rectangle overlaps with the other
    */
  def isOverlappingOther(other: Rectangle): Boolean = x < other.x2 && x2 > other.x && y < other.y2 && y2 > other.y

  /**
    * Computes the absolute distance of the upper left corner of a given rectangle
    * @param other another rectangle
    * @return Tuple, containing the x and y distance
    */
  def getLinearDistance(other: Rectangle): (Int, Int) = (getLinearXDistance(other), getLinearYDistance(other))

  def getLinearXDistance(other: Rectangle): Int = math.abs(x - other.x)

  def getLinearYDistance(other: Rectangle): Int = math.abs(y - other.y)

  def getDimensionDistance(other: Rectangle) : (Int, Int) = (getWidthDifference(other), getHeightDifference(other))

  def getWidthDifference(other: Rectangle): Int = math.abs(width - other.width)

  def getHeightDifference(other: Rectangle) : Int = math.abs(height - other.height)
}

object Rectangle {

  implicit val rectangleFormat = Json.format[Rectangle]
  implicit val rectangleRead = Json.reads[Rectangle]
  implicit val rectangleWrite = Json.writes[Rectangle]

}
