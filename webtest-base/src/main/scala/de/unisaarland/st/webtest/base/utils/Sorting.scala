package de.unisaarland.st.webtest.base.utils

trait Sorting {

  case object AlphabeticSort extends Ordering[String] {
    override def compare(x: String, y: String): Int = {
      (x.toLowerCase zip y.toLowerCase).find(e => {
        e._1 - e._2 != 0
      }) match {
        case Some(value) => value._1 - value._2
        case None => x.lengthCompare(y.length)
      }
    }
  }

}

object Sorting extends Sorting
