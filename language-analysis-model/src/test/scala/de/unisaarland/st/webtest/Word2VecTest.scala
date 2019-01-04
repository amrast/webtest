package de.unisaarland.st.webtest

import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.nlp.deep4j.Deep4jLanguageAnalyzer
import de.unisaarland.st.webtest.base.CollectionUtils._
import de.unisaarland.st.webtest.nlp.SimilarityMatrix
import de.unisaarland.st.webtest.test.TestUtils
//import org.junit.runner.RunWith
//import org.specs2.runner.JUnitRunner

import scalaz.Validation.FlatMap.ValidationFlatMapRequested
import scalaz.{Failure, Success}

//@RunWith(classOf[JUnitRunner])
class Word2VecTest extends TestUtils with Logging {

  "The language analysis" >> {

    val analyzer = BaseSetup().analyzer

//    "be able to read the google news model" >> {
//      analyzer.flatMap { vec: LanguageAnalyzer =>
//        vec.computeSemanticSimilarity("The cat kills the dog", "The dog kills the cat")
//        vec.computeSemanticSimilarity("cat", "dog")
//        vec.computeSemanticSimilarity("This is a dog", "This is a cat")
//        vec.computeSemanticSimilarity("Sign in", "Sign Up")
//        vec.computeSemanticSimilarity(loadTextFile("./new_york_citizendium.txt"), loadTextFile("./new_york_wikipedia.txt"))
//        vec.computeSemanticSimilarity(loadTextFile("./new_york_citizendium.txt"), loadTextFile("./new_york_citizendium.txt"))
//      }
//    }
//
//    "be able to compute a semantic similarity between words" >> {
//      analyzer.flatMap{ vec: Deep4jLanguageAnalyzer =>
//        vec.computeSemanticWordSimilarity("cat", "dog")
//        vec.computeSemanticWordSimilarity("sign up", "sign in")
//        vec.computeSemanticWordSimilarity("sign in", "login")
//      }
//    }
//
//    "be able to compute a semantic similarity between words" >> {
//
//      logRuntime{
//        val target = Seq("Forgot your password", "23 people worked the den")
//        val test = target ++ Seq("Forgot your Password", "Forgot your password?", "Reset Password", "Reset your password", "99 people were freaking out.")
//
//        analyzer.flatMap{ vec: LanguageAnalyzer=>
//          val t = for ( i <- target;  j <-test  ) yield (i,j)
//          if (t.forall{ case (a: String, b: String) =>
//            vec.computeSemanticSimilarity(a, b) match {
//              case Success(value) =>
//                logger.debug(s"$a -> $b: $value")
//                true
//              case Failure(e) =>
//                e.printStackTrace()
//                false
//            }
//          }) scalaz.Success(())
//          else Failure(new RuntimeException("Failed"))
//        }
//      }
//    }


    "be able to compute the semantic similarity between two short sentences" >> {
      val str1 = "Enter username email address"
      val str2 = "Insert username here"

      analyzer.flatMap{ vec: Deep4jLanguageAnalyzer =>
        val tokens1 = str1.split(" ").toSeq

        val tokens2 = str2.split(" ").toSeq

        val tuples = tokens1 cross tokens2

        val results = tuples.toSeq.map{case (a,b) =>
          val c = vec.computeSemanticWordSimilarity(a, b).toOption.get
          println(s"$a vs $b is $c")
          c
        }

        val matrix = for (i <- tokens1.indices) yield {
          val r = results.slice(i * tokens2.size, (i + 1) * tokens2.size)
          println(r.mkString(" & "))

          (Seq(tokens1(i)) ++ r).mkString(" & ")
        }


        val toPrint = Seq((Seq("") ++ tokens2).mkString(" & ")) ++ matrix

        println(toPrint.mkString(" \\cr" + System.lineSeparator()))








        Success(())


      }

    }


//    "be able to compute a semantic similarity between words" >> {
//
//      logRuntime{
//        val target = Seq("Forgot your password", "23 people worked the den")
//        val test = target ++ Seq("Forgot your Password", "Forgot your password?", "Reset Password", "Reset your password", "99 people were freaking out.")
//
//        analyzer.flatMap{ vec: LanguageAnalyzer=>
//          val t = for ( i <- target;  j <-test  ) yield (i,j)
//          if (t.forall{ case (a: String, b: String) =>
//            val matrix = SimilarityMatrix(a.split("\\s+"), b.split("\\s+"), vec)
//            val similarityPath = matrix.getMaxPath
//            println(s"Max Path is $similarityPath")
//            println(matrix.toString)
//            println(s"Similarity between '$a' and '$b' is ${matrix.getNormalizedSum(similarityPath)}")
//
//            true
//          }) scalaz.Success(())
//          else Failure(new RuntimeException("Failed"))
//        }
//      }
//    }


//    "be able to compute semantic text similarity" >> {
//      val target = Seq("Forgot your password")
//      val test = target ++ Seq("Forgot your Password", "Forgot your password?", "Reset Password", "Reset your password")
//
//      val t = for ( i <- target;  j <-test  ) yield (i,j)
//
//
//
//      val vec = logRuntime(Deep4jLanguageAnalyzer(File(
//        "/Users/arau/TestData/enwiki-210117-model_gt_4.txt.wordvec.bin.zip"), true), "Loading Paragraph2Vec Model")
//
//      logRuntime(vec.generateParagraph2Vec, "Generating model").flatMap { vec =>
//        t.foreach{ case (a: String,b: String) =>
//          vec.computeSentenceSimilarity(a,b) match {
//            case Success(r)  => println(s"Semantic similarity between '$a' and '$b' is $r")
//            case Failure(e) =>
//              e.printStackTrace(e)
//          }
//        }
//        scalaz.Success(())
//      }
//    }
  }

  def loadTextFile(path: String): String = scala.io.Source.fromFile(getClass.getResource(path).toURI).mkString
}