package de.unisaarland.st.webtest

import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.base.math.{Matrix, Path}
import de.unisaarland.st.webtest.nlp.SimilarityMatrix
import de.unisaarland.st.webtest.test.TestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner


//noinspection ScalaDeprecation
@RunWith(classOf[JUnitRunner])
@SuppressWarnings(Array("deprecation"))
class MatrixAnalysisTest extends TestUtils with Logging {

  "The mathematic analysis" >> {
      "make correct matrix manipulations" >> {
        val tabulate = Matrix(Array.tabulate(3, 4)((a, b) => (a + b).toDouble))
        tabulate.cutOutRow(0).get(0, 0).get mustEqual 1.0
        tabulate.cutOutRow(2).get(1, 0).get mustEqual 1.0
        tabulate.cutOutRow(-1) mustEqual tabulate.cutOutRow(4)
        tabulate.cutOutColumn(0).get(0,0).get mustEqual 1.0

        tabulate.cutOutColumn(0).cutOutColumn(1).cutOutRow(1).get(1,1).get mustEqual 5.0

        tabulate.cutOutColumnAndRow(0,0).get(0,0).get mustEqual   2.0

        done
      }

      "generate the correct matrix" >> {
        val matrix = new Matrix(Seq(Seq(1,2,3), Seq(4,5,6), Seq(7,8 ,9)))

        matrix.getRow(0) mustEqual Seq(1,2,3)
        matrix.getColumn(1) mustEqual Seq(2,5,8)
        matrix.get(2,2).get mustEqual 9

      }

    "find max value in matrix" >> {
      val tabulate = new SimilarityMatrix(new Matrix(Matrix.transform(Array.tabulate(3, 4)((a, b) => (a + b).toDouble))), new Matrix(Matrix.transform(Array.tabulate(3, 4)((a, b) => (a.toString,b.toString)))))
      tabulate.getMaxEntryIndex mustEqual (2,3)
      tabulate.get(2,3).get mustEqual 5.0

    }

    "find max value in matrix" >> {
      val tabulate = new SimilarityMatrix(new Matrix(Matrix.transform(Array.tabulate(2, 3)((a, b) => (a + b).toDouble))), new Matrix(Matrix.transform(Array.tabulate(2, 3)((a, b) => (a.toString, b.toString)))))
      val paths = tabulate.getAllPaths()
      paths.size mustEqual 6
    }

    "find all paths in matrix" >> {
      val size = 5
      val matrix = Matrix(Array.tabulate(size, size)((a, b) => (a,b)))
      val paths = matrix.getAllPaths().toSeq.map(_.toSeq)
      paths.size.toLong mustEqual fac(size)
    }

    "calculate the corect sum for a path" >> {
      val path = Path(Seq((0,0), (0,1), (0,2)))

      val m = new SimilarityMatrix(new Matrix(Seq(Seq(1,2,3).map(_.toDouble))), null)

      m.getSum(path) mustEqual 6.0

      done

    }


    "identify cols and rows correctly" >> {
      val size = 5
      val matrix = Matrix(Array.tabulate(size, size)((a, b) => (a,b)))
      matrix.getRow(0) mustEqual Seq((0,0), (0,1), (0,2), (0,3), (0,4))
      matrix.getColumn(0) mustEqual Seq((0,0), (1,0), (2,0), (3,0), (4,0))
      matrix.get(0, 1).get mustEqual (0,1)
    }


    "identify the maximum path" >> {
      val val1 = new Matrix(Seq(Seq(7, 1, 2), Seq(0, 0, 10), Seq(10, 2, 2)).map(_.map(_.toDouble / 10.0)))
      val matrix = new SimilarityMatrix(val1, null)
      val1.getValues(matrix.getMaxPath).sum mustEqual 2.1

      val1.getValues(matrix.getMaxSimilarityPath).sum mustEqual 2.1


      //Example 2

      val val3 = new Matrix(Seq(Seq(8,6,5), Seq(10,9,5), Seq(1,3,5)).map(_.map(_.toDouble / 10.0)))
      val matrix3 = new SimilarityMatrix(val3, null)
      val maxPath2 = matrix3.getMaxPath


      val3.getValues(matrix3.getMaxSimilarityPath).sum mustEqual 2.2
      val3.getValues(maxPath2).sum mustEqual 2.2


      val val2 = new Matrix(Seq(Seq(5,6,8), Seq(5,9,10), Seq(5,3,1)).map(_.map(_.toDouble / 10.0)))
      val matrix2 = new SimilarityMatrix(val2, null)
      val maxPath = matrix2.getMaxPath
      val2.getValues(maxPath).sum mustEqual 2.2
    }

//    "check the runtime pretty hardhore" >> {
//      val matrix = Matrix(Array.tabulate(6, 6)((a, b) => math.random))
//      val sm = new SimilarityMatrix(matrix, null)
//
//      val start1 = System.currentTimeMillis()
//
//      val maxPathOptimized = sm.getMaxPath
//
//      val end1 = System.currentTimeMillis()
//
//
//      val start2 = System.currentTimeMillis()
//
//      val maxPathCorrect = sm.getMaxSimilarityPath
//
//      val end2 = System.currentTimeMillis()
//
//      println(s"Execution of optimized needed ${end1 - start1} ms vs. naive ${end2 - start2}")
//
//     round(sm.getSum(maxPathOptimized), 5) mustEqual round(sm.getSum(maxPathCorrect), 5)
//
//    }

    "check the runtime for asynchronous" >> {
      val matrix = Matrix(Array.tabulate(8, 4)((a, b) => math.random))
      val sm = new SimilarityMatrix(matrix, null)

      val start1 = System.currentTimeMillis()

      val maxPathOptimized = sm.getMaxPath

      val end1 = System.currentTimeMillis()


      val start2 = System.currentTimeMillis()

      val maxPathCorrect = sm.getMaxSimilarityPath

      val end2 = System.currentTimeMillis()

      println(s"Execution of optimized asynchronous needed ${end1 - start1} ms vs. naive ${end2 - start2}")


      val sm2 = new SimilarityMatrix(sm.transpose, null)

      val transposedSum = sm2.getSum(sm2.getMaxPath)


      round(sm.getSum(maxPathOptimized), 5) mustEqual round(sm.getSum(maxPathCorrect), 5)

    }

    "check the runtime for large Arrays" >> {


      measurePerformance(10, 10, sm => sm.getMaxPath)("10x10 maxpath calculation")
//      measurePerformance(11, 11, sm => sm.getMaxPath)("11x11 maxpath calculation")
//      measurePerformance(12, 12, sm => sm.getMaxPath)("12x12 maxpath calculation")
//      measurePerformance(13, 13, sm => sm.getMaxPath)("13x13 maxpath calculation")
//      measurePerformance(14, 14, sm => sm.getMaxPath)("14x14 maxpath calculation")
//      measurePerformance(15, 15, sm => sm.getMaxPath)("15x15 maxpath calculation")
//      measurePerformance(20, 20, sm => sm.getMaxPath)("20x20 maxpath calculation")



      done


    }


    "append two matrices" >> {
      val m1: Matrix[Int] = new Matrix(Seq(Seq(1,2,3)))
      val m2: Matrix[Int] = new Matrix(Seq(Seq(4,5,6)))

      m1.colAppend(m2).rows.head mustEqual Seq(1,2,3,4,5,6)

      val m3: Matrix[Int] = new Matrix(Seq(Seq()))

      m3.colAppend(m1).rows.head mustEqual Seq(1,2,3)
    }


  }

  def measurePerformance[T](rows: Int, cols: Int, act: SimilarityMatrix => T)(str: String = ""): T = {
    val sim = new SimilarityMatrix(Matrix(Array.tabulate(rows, cols)((a, b) => math.random)), null)
    val start1 = System.currentTimeMillis()
    val result = act(sim)
    val end1 = System.currentTimeMillis()

    println(if (str.isEmpty) s"Execution needed ${end1 - start1} ms" else s"Execution of '$str' needed ${end1 - start1} ms")

    result
  }

  def round(double: Double, scale: Int): Double = {
    BigDecimal(double).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def fac(value: Int): Long = {
    (2 to value).view.product
  }

  def hitmap(input: Seq[Seq[(Int, Int)]]): Seq[Seq[Int]] = {
    val tabulate = Array.tabulate(input.size, input.head.size)((a, b) => 0)

    for (i <- 0 to tabulate.length; j <- 0 to tabulate.head.length) {
      val point = input(i)(j)
      val t = tabulate(point._1)(point._2)
      tabulate(point._1)(point._2) = t + 1
    }

    tabulate.map(_.toSeq).toSeq
  }
}