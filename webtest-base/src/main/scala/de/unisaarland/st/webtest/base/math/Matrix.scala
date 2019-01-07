package de.unisaarland.st.webtest.base.math

import java.io.OutputStream

import de.unisaarland.st.webtest.base.CollectionUtils

import scala.collection.immutable.IndexedSeq
import scala.collection.{SeqView, mutable}
import scala.io.Codec
import scala.tools.nsc.io.File

class Matrix[@specialized(Double) T](val rows: Seq[Seq[T]]) {

  val width: Int = rows.headOption.map(_.length).getOrElse(0)
  val height: Int = rows.length

  private lazy val rowHolder = CollectionUtils.Crossable((0 until height).view)
  private lazy val colHolder: SeqView[Int, IndexedSeq[Int]] = (0 until width).view

  protected lazy val cols = rows.transpose //(0 until width).map(ri => rows.map(_(ri)))

  def flatten: Seq[T] = rows.flatten

  def getValues: Iterator[T] = rows.iterator.flatMap(_.iterator)

  def findMatchingColumnIndices(by: Seq[T] => Boolean): Seq[Int] = {
    cols.zipWithIndex.filter{case (c,index) => by(c)}.map(_._2)
  }


  /**
    * Calculates all unique paths through the matrix. Unique paths are paths were points in the same
    * row and column only occur one.
    *
    * It produces fac(n) many paths for symmetric matrixes and n*m paths for asymetric matrixes
    * of length n and width m
    * @param filter Filters are applied to remove paths while creating the iterable and applied
    *               to the values in the matrix
    * @return a set of unique paths.
    */
  @deprecated("Although this method works perfectly fine, the number of produced paths is fac(n) for symmetric matrixes.", "April 6th 2017")
  def getAllPaths(filter: Set[T => Boolean] = Set()): Set[Set[(Int, Int)]] = {
    getPaths(Set(), filter)
  }

  /**
    * Calculates a new Seq of the column entries as identified by the index.
    *
    * @throws ArrayIndexOutOfBoundsException if the index is smaller 0 or > width
    * @return a new seq with the values in this column
    */
  def getColumn(index: Int): Seq[T] = {
//    rows.map(_(index))
    cols(index)
  }

  def getCols: Seq[Seq[T]] = cols//(0 until width).map(getColumn)


  /**
    * Returns all cols which match the given list of indices
    */
  def getCols(seq: Iterable[Int]): Seq[Seq[T]] = {
    val set = seq.toSet
    for ((c, i) <- cols.zipWithIndex; if set.contains(i)) yield c
  }

  /**
    * Gets the row entries as identified by the index.
    *
    * @throws ArrayIndexOutOfBoundsException if the index is smaller 0 or > height
    * @return a seq with the values in this row
    */
  def getRow(index: Int): Seq[T] = rows(index)


  /**
    * Returns all unique paths through the system, meaning that there are not two entries from either the same row nor column.
    * @return a set of paths
    */
  private def getPaths(points: Set[(Int, Int)] = Set(), filter: Set[T => Boolean]): Set[Set[(Int, Int)]] = {
    val tuples = rowHolder.crossWithFilter[Int](colHolder) ({case (x, y: Int) =>
      get(x, y).exists { case (l: T @unchecked) => !filter.exists { f: ((T) => Boolean) => f(l) } } && //checks that no filter holds
        !points.exists { case (xi, yi) => x == xi || y == yi } //checks that no row and column occurs double

    })

    if (tuples.isEmpty) {
      Set(points)
    } else {
      val addedPaths = tuples.map(points + _).map(t => getPaths(t, filter))
      addedPaths.toSet.flatten
    }
  }

  def getDimensions: (Int, Int) = (width, height)

  /**
    * Returns the entry at the given index, if it exists
    *
    * @param row the row index
    * @param col the column index
    * @return the value present at the posistion or nothing
    */
  def get(row: Int, col: Int): Option[T] = if (col < width && row < height)
    Some(rows(row)(col))
  else None

  /**
    * Creates a copy of the matrix without the row indicated by the given index
    * @param row row index
    * @return a new matrix
    */
  def cutOutRow(row: Int): Matrix[T] = {
    if (row < 0 || row >= height) this
    else new Matrix(rows.slice(0, row) ++ rows.slice(row + 1, height))
  }

  /**
    * Creates a copy of the matrix without the column indicated by the given index
    * @param col col index
    * @return a new matrix
    */
  def cutOutColumn(col: Int): Matrix[T] = {
    if (col < 0 || col >= width) this
    else new Matrix(rows.map(c => c.slice(0, col) ++ c.slice(col + 1, width)))
  }

  def cutOutColumnAndRow(col: Int, row: Int): Matrix[T] = cutOutColumn(col).cutOutRow(row)

  /**
    * Applied the given function on all entries in the matrix and returns a new matrix
    * @param act function applied on all values in the matrix
    * @return a new matrix
    */
  def map[S](act: T => S): Matrix[S] = new Matrix(rows.map(_.map(act)))

  override def toString: String = (for (row <- rows) yield row.mkString("(", ";", ")")).mkString(System.lineSeparator())

  def getMaxValueInRow(i: Int)(implicit ordering: Ordering[T]): T = getRow(i).max

  def getIndexOfMaxValueInRow(i: Int)(implicit ordering: Ordering[T]): (Int, Int) = (i, getRow(i).zipWithIndex.maxBy(_._1)._2)

  private lazy val maxPerCol = mutable.HashMap.empty[Int, T]

  def getMaxValueInCol(i: Int)(implicit ordering: Ordering[T]): T = {
    maxPerCol.get(i) match {
      case Some(value) => value
      case None =>
        val t = getColumn(i).max
        maxPerCol.put(i, t)
        t
    }
  }

  /**
    * Sorts the columns according to the given ordering in ascending order
    */
  def colSortedIndex(implicit ordering: Ordering[Seq[T]]): Seq[Int] = {
    val opt = new Ordering[(Seq[T], Int)] {
      override def compare(x: (Seq[T], Int), y: (Seq[T], Int)) = ordering.compare(x._1,y._1)
    }
    cols.zipWithIndex.sorted(opt).map(_._2)
  }

  def rowSortWith(by: (Seq[T], Seq[T]) => Boolean): Matrix[T] = new Matrix(rows.sortWith(by))

  def rowSorted()(implicit ordering: Ordering[Seq[T]]): Matrix[T] = new Matrix(rows.sorted)

  def colSortWith(by: (Seq[T], Seq[T]) => Boolean): Matrix[T] = transpose.rowSortWith(by).transpose

  def colSorted()(implicit ordering: Ordering[Seq[T]]): Matrix[T] = transpose.rowSorted().transpose

  def getIndexOfMaxValueInCol(i: Int)(implicit ordering: Ordering[T]): (Int, Int) = (i, getColumn(i).zipWithIndex.maxBy(_._1)._2)

  /**
    * Returns the entry with the highest value in this matrix. If two are equal, the one to the upper left is returned.
    * @return a tuple for row and column
    */
  def getMaxEntryIndex(implicit ordering: Ordering[T]): (Int, Int) = {
    val maxRows = rows.map(m => {
      val max = m.max
      (m.indexOf(max), max)
    })

    val map1 = maxRows.map(_._2)
    val col = map1.indexOf(map1.max)
    (col, maxRows(col)._1)
  }

  def transpose: Matrix[T] =  {
   new Matrix(rows.transpose)
  }

  def transform(indexAction: (Int, Int, T) => T): Matrix[T] = {
    val transformed = rows.zipWithIndex.map{
      case (row, ri) => row.zipWithIndex.map{
        case (entry, ci) => indexAction(ri, ci, entry)
      }
    }

    new Matrix(transformed)
  }

  def getIterator: Iterator[Entry[T]] = {
    (for (row <- (0 until height).view; col <- (0 until width).view) yield Entry(row, col, rows(row)(col))).toIterator
  }

  def getEntriesInColumn(i: Int): Seq[Entry[T]] = {
    getColumn(i).zipWithIndex.map{case (value, row) => Entry(row, i, value)}
  }

  def getValues(p: Path): Iterable[T] = {
    p.get.flatMap { case (a, b) => get(a, b) }
  }

  /**
    * Adds a single column of values into the matrix at the designated position.
    * @param col the column to add (must have dimension of the height of the matrix)
    * @param pos the position at which the column is added
    * @return a new matrix
    */
  def addC(col: Seq[T], pos: Int): Matrix[T] = {
    assert(col.size == height, s"The given column must have the same dimension as the old matrix. Expected $height, but was ${col.size}")
    assert(pos >= 0 && pos <= width, s"The entry position must be in valid boundaries [0, ${width -1}], but was ${pos}")

    val newRows = rows.zip(col).map {
      case (row, entry) =>
        val rest = width - pos
        val split = row.take(pos)
        split ++ Seq(entry) ++ row.drop(pos)

    }

    new Matrix(newRows)
  }

  def addR(addRows: Seq[Seq[T]], pos: Int): Matrix[T] = {
    assert(addRows.head.size == width, s"The given row must have the same dimension as the old matrix. Expected $width, but was ${addRows.head.size}")
    assert(pos > 0 && pos < height, s"The entry position must be in valid boundaries [0, ${height -1}], but was ${pos}")

    val split = rows.splitAt(pos)

    new Matrix(split._1 ++ addRows ++ split._2)
  }


  def to[S](op: T => S): Matrix[S] = Matrix(width, height, getValues.map(op).toIterable)

  /**
    * Adds the values of the given matrix to the values of this matrix.

    * @return a new matrix
    */
  def +(matrix: Matrix[T])(implicit numeric: Numeric[T]): Matrix[T] = {
    assert(matrix.width == width, "When adding two matrices, they have to have the same dimensions, but width differs")
    assert(matrix.height == height, "When adding two matrices, they have to have the same dimensions, but height differs")

    val src = getValues
    val tgt = matrix.getValues

    val result = Matrix(width, height, src.map(s => numeric.plus(s, tgt.next())).toIterable)

    assert(!src.hasNext && !tgt.hasNext, "After calculation iterator was not empty")

    result
  }

  def -(matrix: Matrix[T])(implicit numeric: Numeric[T]): Matrix[T] = {
    assert(matrix.width == width, "When adding two matrices, they have to have the same dimensions, but width differs")
    assert(matrix.height == height, "When adding two matrices, they have to have the same dimensions, but height differs")

    val src = getValues
    val tgt = matrix.getValues

    val result = Matrix(width, height, src.map(s => numeric.minus(s, tgt.next())).toIterable)
    assert(!src.hasNext && !tgt.hasNext, "After calculation iterator was not empty")
    result
  }

  def colAppend(matrix: Matrix[T]): Matrix[T] = {
    if (matrix.width == 0) this
    else if (width == 0) matrix
    else {
      assert(height == 0 || matrix.height == height, "Appending takes matrices of same height")
      val tuples: Seq[Seq[T]] = rows.zip(matrix.rows).map(r => r._1 ++ r._2)
      new Matrix(tuples)
    }
  }

  def isEmpty: Boolean = { width == 0 || height == 0 }

  override def equals(obj: scala.Any) = {
    obj match {
      case a: Matrix[T] =>
        a.height == height && a.width == width && rows.zip(a.rows).par.forall {
          case (r1, r2) => r1 == r2
        }
    }
  }

  override def hashCode() = rows.hashCode()

  def writeCSV(outputStream: OutputStream, delimiter: String = Matrix.DEFAULT_DELIMITER, charset: String = Matrix.DEFAULT_CHARSET): Unit = {
    rows.foreach { r =>
      val str = r.mkString("", delimiter, System.lineSeparator())
      outputStream.write(str.getBytes(charset))
    }
    outputStream.flush()
  }

}


object Matrix {

  final val DEFAULT_DELIMITER = ";"
  final val DEFAULT_CHARSET = "UTF-8"


  def apply[T](file: File, transformer: String => T, delimiter: String = DEFAULT_DELIMITER, charset: String = DEFAULT_CHARSET): Matrix[T] = {
    Matrix(file.lines(Codec.apply(charset)).map(line => line.split(delimiter).map(transformer).toIterable).toIterable)
  }

  def apply[T](matrix: Array[Array[T]]): Matrix[T] ={
    new Matrix(transform(matrix.map(_.toIterable).toIterable))
  }

  def apply[T](matrix: Iterable[Iterable[T]]): Matrix[T] ={
    new Matrix(transform(matrix))
  }

  def apply[T](width: Int, height: Int, values: Iterable[T]): Matrix[T] = {
    val matrix = new Matrix(values.grouped(width).map(_.toSeq).toSeq)
    assert(matrix.height == height)
    assert(matrix.width == width)
    assert(matrix.rows.last.lengthCompare(width) == 0)
    matrix
  }

  def transform[T](matrix: Array[Array[T]]): Seq[Seq[T]] = matrix.map(_.toSeq).toSeq

  def transform[T](matrix: Iterable[Iterable[T]]): Seq[Seq[T]] = matrix.map(_.toSeq).toSeq
  
  def cross[U,S](a: Seq[U], b: Seq[S]): Seq[(U,S)] = for (ai <- a; bi<-b) yield (ai, bi)


  def iterableToEqualSpacedString(iterable: Seq[Seq[Any]], maxWidth: Int = 512, maxLines: Int = 128): String = {
    val height = iterable.length
    val width = iterable.headOption.map(_.size).getOrElse(0)

    val showRows = if (height > maxLines) maxLines - 1 else height

    def colWidth(col : Int) =
      if (showRows > 0) (0 until showRows).map(row => Option(iterable(row)(col)).map(_.toString.length + 2).getOrElse(3)).max else 0

    val colWidths = new scala.collection.mutable.ArrayBuffer[Int]
    var col = 0
    while (col < width && colWidths.sum < maxWidth) {
      colWidths += colWidth(col)
      col += 1
    }

    // make space for "... (K total)"
    if (colWidths.size < width) {
      while (colWidths.sum + width.toString.length + 12 >= maxWidth) {
        if (colWidths.isEmpty) {
          return "%d x %d matrix".format(height, width)
        }
        colWidths.remove(colWidths.length - 1)
      }
    }

    val newline = System.lineSeparator()

    val rv = new scala.StringBuilder
    for (row <- 0 until showRows; col <- colWidths.indices) {
      val cell = Option(iterable(row)(col)).map(_.toString).getOrElse("--")
      rv.append(cell)
      rv.append(" " * (colWidths(col) - cell.length))
      if (col == colWidths.length - 1) {
        if (col <  width - 1) {
          rv.append("...")
          if (row == 0) {
            rv.append(" (")
            rv.append(width)
            rv.append(" total)")
          }
        }
        if (row + 1 < showRows) {
          rv.append(newline)
        }
      }
    }

    if (height > showRows) {
      rv.append(newline)
      rv.append("... (")
      rv.append(height)
      rv.append(" total)")
    }

    rv.toString
  }

}
