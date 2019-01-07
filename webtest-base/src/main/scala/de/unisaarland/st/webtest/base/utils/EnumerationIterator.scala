package de.unisaarland.st.webtest.base.utils

case class EnumerationIterator[T](enum: java.util.Enumeration[T]) extends Iterator[T] {
  override def hasNext = enum.hasMoreElements

  override def next() = enum.nextElement()
}
