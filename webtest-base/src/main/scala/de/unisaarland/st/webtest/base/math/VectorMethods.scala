package de.unisaarland.st.webtest.base.math

object VectorMethods {

  /**
   * This method takes 2 equal length Seqs of integers
   * It returns a double representing similarity of the 2 Seqs
   * 0.9925 would be 99.25% similar
   * (x dot y)/||X|| ||Y||
   */
  def intCosineSimilarity(x: Seq[Int], y: Seq[Int]): Double = {
    require(x.lengthCompare(y.size) == 0)
    dotProduct(x, y)/(magnitude(x) * magnitude(y))
  }

//  @deprecated("DELETED FOR TESTING, BEWARE")
  def cosineSimilarity(x: Seq[Double], y: Seq[Double]): Double = {

//    euclideanDistance(x,y)
    require(x.lengthCompare(y.size) == 0)
    dotProduct(x, y)/(magnitude(x) * magnitude(y))

  }

  /**
   * Return the dot product of the 2 Seqs
   * e.g. (a[0]*b[0])+(a[1]*a[2])
   */
  def dotProduct[T](x: Seq[T], y: Seq[T])(implicit num: Numeric[T]): T = {
    (for((a, b) <- x zip y) yield num.times(a,b)).sum
  }

  /**
   * Return the magnitude of an Seq
   * We multiply each element, sum it, then square root the result.
   */
  def magnitude[T](x: Seq[T])(implicit num: Numeric[T]): Double = {
    math.sqrt(num.toDouble(x.foldLeft(num.zero)((tmp, t) => num.plus(tmp, num.times(t,t)))))
  }

  def euclideanDistance(x: Seq[Double], y: Seq[Double]): Double = {
    require(x.lengthCompare(y.length) == 0,  "Vectors need to have same dimension")
    math.sqrt((x zip y).map{ p => math.pow(p._1 - p._2, 2.0)}.sum)
  }

  def getDistanceOrdering(cut: Seq[Double]) = new Ordering[Seq[Double]] {
    override def compare(x: Seq[Double], y: Seq[Double]): Int = {
      require(x.lengthCompare(y.length) == 0 && x.lengthCompare(cut.length) == 0, "Vectors need to have same dimension")
      euclideanDistance(cut, x).compareTo(euclideanDistance(cut, y))
    }
  }

}
