package de.unisaarland.st.webtest.base.math

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Path(p: mutable.ListBuffer[(Int, Int)] = ListBuffer.empty[(Int, Int)]) {

  def length: Double = p.length.toDouble
  //  private lazy val width = p.map(_._1).max
  //  private lazy val height = p.map(_._2).max

  def contains(point: (Int, Int)): Boolean = p.exists{case (x,y) => x == point._1 && y == point._2}

  def containsConflicting(point: (Int, Int)): Boolean = p.exists{case (x,y) => x == point._1 || y == point._2}

  def append(point: (Int, Int)): Unit = p.append(point)

  def appendAll(t: Traversable[(Int, Int)]): Unit = p.appendAll(t)

  def get: Seq[(Int, Int)] = p

  def transpose: Path = Path(p.map{ case (a,b) => (b,a)})


  def sortByColumn: Path = {
    Path(p.sortBy(_._2))
  }

  def visitedRows: Set[Int] = p.map(_._1).toSet

  def visitedCols: Set[Int] = p.map(_._2).toSet

  override def toString: String = p.mkString(",")

  def isEmpty: Boolean = p.isEmpty
}

object Path {
  def apply(p: Traversable[(Int, Int)]): Path = {
    val path = new Path()
    path.appendAll(p)
    path
  }
}