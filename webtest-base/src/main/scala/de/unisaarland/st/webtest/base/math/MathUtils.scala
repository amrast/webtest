package de.unisaarland.st.webtest.base.math

object MathUtils {

  def round(value: Double, scale: Int): Double = BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  def div[T,S](value: T, total: S)(implicit numeric: Numeric[T], num: Numeric[S]): Double = {
    numeric.toDouble(value) / num.toDouble(total)
  }

  def percent[T,S](value: T, total: S)(implicit numeric: Numeric[T], num: Numeric[S]): Double = {
    div(value, total) * 100
  }

}
