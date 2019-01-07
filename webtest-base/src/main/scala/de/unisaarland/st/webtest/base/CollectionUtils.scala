package de.unisaarland.st.webtest.base

import scala.collection.mutable.ListBuffer

object CollectionUtils {

  implicit class Crossable[X](xs: Traversable[X]) {


    def crossActionIgnoreEquals[Y,That](ys: Traversable[Y])(act: (X,Y) => That): Traversable[That] = for {x <- xs; y<-ys; if x != y} yield act(x,y)

    def cross[Y](ys: Traversable[Y]): Traversable[(X, Y)] = for {x <- xs; y <- ys} yield (x, y)

    def crossIgnoreEquals[Y](ys: Traversable[Y]): Traversable[(X, Y)] = for {x <- xs; y <- ys; if x != y} yield (x, y)

    //    def crossWithFilter[Y](ys: Traversable[Y], filter: (X, Y) => Boolean): Traversable[(X, Y)] = for (x <- xs; y <- ys; if filter(x, y)) yield (x, y)

    def multiply[Y](ys: Traversable[Y]): Traversable[(X, Traversable[Y])] = {
      for (x <- xs) yield (x, ys)
    }

    def multiplyFiltered[Y](ys: Traversable[Y])(filter: (X, Y) => Boolean): Traversable[(X, Traversable[Y])] = {
      for (x <- xs) yield (x, ys.filter(y => filter(x, y)))
    }

    def crossWithFilter[Y](ys: Traversable[Y])(filter: (X, Y) => Boolean): Traversable[(X, Y)] = for (x <- xs; y <- ys; if filter(x, y)) yield (x, y)

    /**
      * Returns a seq containing the cross product of two given traversables. The result does <b>not</b> contain elements of
      * <ol>
      *   <li> (x,x) </li>
      *   <li> (x,y), if list contains (y,x) </li>
      * </ol>
      */
    def crossIgnoreEqualsUnpair(ys: Traversable[X]): Seq[(X, X)] = {
      val tuples = xs crossIgnoreEquals ys

      tuples.foldLeft(ListBuffer.empty[(X, X)]) {
        case (tmp, (l, r)) if !tmp.contains((r, l)) =>
          tmp.append((l, r))
          tmp
        case (tmp, _) => tmp

      }
    }
  }

  def splitBySeparator[T](l: Seq[T], sep: T, removeSeperator: Boolean): Seq[Seq[T]] = splitBySeparator(l, (e: T) => e == sep, removeSeperator)

  def splitBySeparator[T](l: Seq[T], sep: T => Boolean, removeSeperator: Boolean): Seq[Seq[T]] = {
    val b = ListBuffer(ListBuffer[T]())
    l foreach {
      case e if sep(e) =>
        if (!removeSeperator) b.last += e
        if (b.last.nonEmpty) b += ListBuffer[T]()

      case e => b.last += e
    }
    b.filter(_.nonEmpty).map(_.toSeq)
  }

}
