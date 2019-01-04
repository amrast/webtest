//package de.unisaarland.st.webtest
//
//import de.unisaarland.st.webtest.api.{ComputeSemanticSimilarityCommand, ComputeWordSimilarityCmd, ComputeWordSimilarityResponse}
//import de.unisaarland.st.webtest.base.CollectionUtils.Crossable
//import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
//import de.unisaarland.st.webtest.base.{Logging, UtilityTypes}
//import de.unisaarland.st.webtest.test.TestUtils
//import org.junit.runner.RunWith
//import org.specs2.runner.JUnitRunner
//
//import scalaz.Success
//
////@RunWith(classOf[JUnitRunner])
//class StepDebuggerTest extends TestUtils with TestRemoteEnvironment with Logging {
//
//  "The word similarity" >>  {
//
//    "be independent of the order" >> {
//      implicit val tr: TestRemoteEnvironment = this
//      val pairs = convertToSingleWordPairs(Seq("Price", "Privacy Notice"))
//      val inverted = pairs.map(a => a._2 -> a._1)
//
//      val similarity = computeWordSimilarity(pairs)
//      val invertedSims = computeWordSimilarity(inverted)
//
//      (similarity, invertedSims) match {
//        case (Success(sims), Success(simsinv)) =>
//          println(sims.mkString(System.lineSeparator()))
//          logger.info("Similarities have been computed")
//          sims.map(_._2.response.toOption.get) mustEqual simsinv.map(_._2.response.toOption.get)
//          done
//        case _ => failure("Failed to evaluate")
//          //Failure(new RuntimeException("Failed to evaluate."))
//
//      }
//    }
//
//  }
//
//  "The semantic (string) similarity" >>  {
//
//    "be independent of the order" >> {
//      implicit val tr: TestRemoteEnvironment = this
//      val pairs = Seq("Price", "Privacy Notice", "Amazing", "Shop now", "Germany", "Coffee Tea Beverages")
//
//      val tuples = pairs crossIgnoreEquals pairs
//
//      UtilityTypes.aggregate(tuples.map(t => remoteExecute(ComputeSemanticSimilarityCommand(t._1, t._2)))).map { results =>
//        println(results.mkString(System.lineSeparator()))
//      }
//
////
////      (similarity, invertedSims) match {
////        case (Success(sims), Success(simsinv)) =>
////          logger.info("Similarities have been computed")
////          sims.map(_._2.response.toOption.get) mustEqual simsinv.map(_._2.response.toOption.get)
////        case _ => Failure(new RuntimeException("Failed to evaluate."))
////
////      }
//    }
//
//  }
//
//
//
//  private def convertToSingleWordPairs(iterable: Iterable[String]): Seq[(String, String)] = {
//    val map = iterable.toSeq.flatMap(_.split("\\s+"))
//    map crossIgnoreEqualsUnpair map
//  }
//
//  private def computeWordSimilarity(iterable: Iterable[(String, String)])(implicit remote: TestRemoteEnvironment): TValidation[Seq[(ComputeWordSimilarityCmd, ComputeWordSimilarityResponse)]] = {
//    val map = iterable.map(pair => remote.remoteExecute(ComputeWordSimilarityCmd(pair._1, pair._2)))
//    UtilityTypes.aggregate(map)
//  }
//
//}
