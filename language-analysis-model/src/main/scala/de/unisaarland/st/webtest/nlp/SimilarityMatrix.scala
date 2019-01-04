package de.unisaarland.st.webtest.nlp

import de.unisaarland.st.webtest.api.LanguageAnalyzer
import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.base.math.{Matrix, Path}

import scala.collection.mutable


class SimilarityMatrix(normalizedMatrix: Matrix[Double], words: Matrix[(String, String)]) extends Matrix[Double](normalizedMatrix.rows) with Logging {

  /**
    * Value defines what similarity treshold leads to the similarity to be ignored
    */
  private final val minSimilarity = 0.01
  private def ignorePathBasedOnMinSimilarity = { (a: Double) => a < minSimilarity}



  /**
    * Calculates the average value in the matrix
    */
  def normalizedSum: Double = normalizedMatrix.flatten.sum / (normalizedMatrix.width * normalizedMatrix.height)

  /**
    * Returns the path with the maximum similarity value by calculating all possible paths and summing the values up. The best is returned.
    *
    * Although perfectly correct, this method is slow and memory intensive.
    *
    * @see getMaxPath - use this method instead
    *
    * @return the optimal path through the matrix regarding maximal sum of all components
    */
  @deprecated("No good performance. Present for testing only. Use getMaxPath instead", "April 6th 2017")
  def getMaxSimilarityPath: Path = {
    val map1 = normalizedMatrix.getAllPaths(Set(ignorePathBasedOnMinSimilarity)).map(Path.apply)
    logger.debug{
      val ignoredElements = normalizedMatrix.flatten.count(_ < minSimilarity)
      if (ignoredElements > 0)
        s"$ignoredElements / ${normalizedMatrix.width * normalizedMatrix.height} word pairs ignored. ${map1.size} paths are analyzed."
      else
        s"${normalizedMatrix.width} x ${normalizedMatrix.height}. ${map1.size} paths are analyzed."
    }
    map1.max(pathOrdering)
  }

  /**
    * Calculates the similarity index indicated by the path, normalized by its length.
    *
    * @return value between 0 to 1.
    **/
  def getNormalizedSum(p: Path): Double = if (p.isEmpty) 0.0 else getSum(p) / p.length

  /**
    * Calculates the sum of the given path in the similarity matrix. 0.0 if path has length 0.
    */
  def getSum(path: Path): Double = {
    if (path.isEmpty)
      0.0
    else {
      path.get.map { case (i, j) => normalizedMatrix.get(i, j).get }.sum
    }
  }

  val pathOrdering = new Ordering[Path] {
    def compare(x: Path, y: Path): Int = getNormalizedSum(x).compare(getNormalizedSum(y))
  }

  private def getMaxPathCandidate(path: Path = new Path()): Path = {
    val sorted = normalizedMatrix.getIterator.toSeq.sortBy(_.value)

    val targetSize = math.min(width, height)

    var i = sorted.length - 1
    while (path.length < targetSize && i >= 0) {
      val entry = sorted(i)
      if (!path.containsConflicting(entry.row, entry.col)) {
        path.append(entry.row, entry.col)
      }

      i = i-1

    }
    path.sortByColumn

  }

  /**
    * Returns the path with the maximal sum of values in the double matrix. E.g. Useful for word similarities, where
    * each word can exactly match one word.
    */
  def getMaxPath: Path = {
    if (width > height) {
      new SimilarityMatrix(transpose, null).getMaxPath.transpose
    } else {
      val pathCandidate = getMaxPathCandidate()

      val ref = pathCandidate.get.head

      val col = ref._2

      pathCandidateSum = getValues(pathCandidate).sum

      val entriesInCol = getEntriesInColumn(col)

      val maxCalValues = maxColumnSumToTheRight(col)

      val potentialAdverseries = entriesInCol.filter { e => e.value + maxCalValues > pathCandidateSum }.sortBy(a => a.value).reverse

      potentialAdverseries.flatMap { pa =>
        val ap = Path(Seq((pa.row, pa.col)))
        recCallNr = 0
        val subP = getSubPaths(ap, pa.col+1)
        subP
      } match {
        case Seq() => pathCandidate
        case a => a.maxBy(getSum)
      }
    }
  }

  var pathCandidateSum: Double =0.0

  private lazy val colsToRight = mutable.HashMap.empty[Int, Double]

  def maxColumnSumToTheRight(i: Int): Double = {
    colsToRight.get(i) match {
      case Some(sum) => sum
      case None =>
        val sum = (i + 1 until width).view.map(getMaxValueInCol).sum
        colsToRight.put(i, sum)
        sum
    }
  }

  private var recCallNr=0

  private def getSubPaths(initialPath: Path, colIndexNew: Int): Option[Path] = {
    recCallNr+=1
    if (Thread.currentThread().isInterrupted) throw new InterruptedException(s"Thread was interrupted while computing max similarity path. $getDimensions")

    if (initialPath.length < width && initialPath.length < height) {
      val maxColIndex = initialPath.visitedCols.max

      val feasableEntries = getColumn(colIndexNew).zipWithIndex.filterNot{ case (_, index) =>  initialPath.visitedRows.contains(index)}

      val maxEntry = feasableEntries.maxBy(_._1)

      val visitedPathSum = getSum(initialPath)
      val potentialNodes = feasableEntries.filter { case (entryValue, index) =>
        val potentialSum: Double = visitedPathSum+entryValue + maxColumnSumToTheRight(colIndexNew)
        potentialSum > pathCandidateSum && (index==maxEntry._2 || potentialSum > maxEntry._1)
      }

      val sortedPotentialNodes = if (potentialNodes.size < 5) potentialNodes else potentialNodes.sortBy(_._1).reverse

      val path = sortedPotentialNodes.map(elem => Path(initialPath.get ++ Seq((elem._2, maxColIndex + 1)))).flatMap{ newPathCandidate =>
        getSubPaths(newPathCandidate, colIndexNew+1)

      } match {
        case Seq() => initialPath
        case pathCandidates =>
          pathCandidates.maxBy(getSum)
      }

      val sum = getSum(path)
      if (sum >= pathCandidateSum) {
        pathCandidateSum = sum
        Some(path)
      }  else None

    } else {
      Some(initialPath)
    }
  }





}




object SimilarityMatrix {

  def apply(s1: Seq[String], s2: Seq[String], languageAnalyzer: LanguageAnalyzer): SimilarityMatrix = {
    val tabulate = Matrix[(String, String)](Array.tabulate(s1.length, s2.length)((a, b) => (s1(a), s2(b))))

    val doubleMatrix = tabulate.map{ case (a,b) =>
      if (Thread.currentThread().isInterrupted) throw new InterruptedException(s"Thread was interrupted while computing word similarities in matrix, ${tabulate.getDimensions}")
      languageAnalyzer.computeSemanticWordSimilarity(a,b) match {
        case scalaz.Success(r) => r
        case scalaz.Failure(e) => throw e
      }}

    if (doubleMatrix.height > doubleMatrix.width) {
      val m = doubleMatrix.transpose//.transform(SimilarityMatrix.penalyzeFunction(doubleMatrix.width, doubleMatrix.height))
      new SimilarityMatrix(m, tabulate.transpose)
    } else {

      new SimilarityMatrix(doubleMatrix//.transform(SimilarityMatrix.penalyzeFunction(doubleMatrix.height, doubleMatrix.width))
       , tabulate)
    }
  }

  def penalyzeFunction(height: Int, width: Int): (Int, Int, Double) => Double = { (row, col, value) =>
    val lengthPenalty = 1.0 / (math.abs(width - height).toDouble + 1.0)

    val distancePenalty = 1.0 / (1.0 + math.abs(row - col).toDouble)

    value * lengthPenalty * distancePenalty
  }
}