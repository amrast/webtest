//package de.unisaarland.st.webtest
//
//import com.sun.nio.sctp.IllegalReceiveException
////import de.unisaarland.st.webtest.api.{ComputeWordSimilarityCmd, _}
//import de.unisaarland.st.webtest.base.{Logging, UtilityTypes}
//import de.unisaarland.st.webtest.test.TestUtils
//import org.junit.runner.RunWith
//import org.specs2.runner.JUnitRunner
//
//import scalaz.Validation.FlatMap.ValidationFlatMapRequested
//import scalaz.{Failure, Success}
//
////@RunWith(classOf[JUnitRunner])
//class LanguageAnalyzerSystemTest extends TestUtils with TestRemoteEnvironment with Logging {
//
//  sequential
//
//  "The language-analyzer" >> {
//
//    "run a thousand similarities" >> {
//
//      var s = 0
//      var f = 0
//
//      val r = for (_ <- 0 to 1000) yield {
////        remoteExecuteCommand(ComputeSemanticSimilarityCommand("word", "word")) match {
//          case Success(a) => s +=1
//            1
//          case Failure(e) => f += 1
//            0
//        }
//      }
//
//      println(r.mkString(""))
//      println(s"$s $f")
//
//      s mustEqual 1001
//
//    }
//
//    "be able to extract the topic of a text" >>  {
//
//      remoteExecuteCommand(ExtractTopicOnStringCommand("This sentence does not make sense.")).flatMap({
//        case ExtractTopicResponse(Success(result)) =>
//          println(result.mkString(System.lineSeparator()))
//          Success(())
//        case ExtractTopicResponse(Failure(e)) =>
//          e.printStackTrace()
//          Failure(new RuntimeException(e.getMessage))
//      })
//    }
//
//    "be able to find text similarities" >> {
//      val target = "Password"
//      remoteExecuteCommand(ComputeSemanticSimilaritiesCommand(Seq(target, "Forgot your password", "Forgot your password?", "password"), target)).flatMap({
//        case ComputeSemanticSimilaritiesResponse(result) =>
//          result.flatMap{similarities =>
//            logger.debug(similarities.mkString(s"Similarity to $target${System.lineSeparator()}", System.lineSeparator(), ""))
//            Success(similarities)
//          }
//      })
//    }
//
//    "be able to find more complex text similarities" >> {
//      val target = "Forgot your password"
//      remoteExecuteCommand(ComputeSemanticSimilaritiesCommand(Seq(target, "Forgot your Password", "Forgot your password?", "Reset Password", "Reset your password"), target)).flatMap({
//        case ComputeSemanticSimilaritiesResponse(result) =>
//          result.flatMap{similarities =>
//            logger.debug(similarities.mkString(s"Similarity to $target${System.lineSeparator()}", System.lineSeparator(), ""))
//            Success(similarities)
//          }
//      })
//    }
//
//    "be able to compute word similarities" >> {
//
//      val commands = convertToCommands(Seq(("password", "Password"),
//        ("password", "login"),
//        ("password", "account"),
//        ("password", "password"),
//        ("password", "authenticate"),
//        ("password", "something"),
//        ("password", "weird"),
//        ("Sponsored", "Macedonia"),
//        ("Amazoncom","rescontentglobalinflowinflowcomponentneedsomehelp")
//      ))
//
//      commands.map(c => (c, remoteExecuteCommand(c))).forall {
//        case (c, Success(ComputeWordSimilarityResponse(Success(r)))) =>
//          logger.debug(s"'${c.word1}' matches '${c.word2}' to $r")
//          true
//        case (c, Failure(r)) => throw r
//        case a @ _ => throw new RuntimeException(s"Illegal response $a")
//      }
//    }
//
//    "be able to compute semantic similarities" >> {
//       val commands = convertToCommands(Seq(("Shoes Clothing","Introduction"), ("Sponsored", "Macedonia"), ("Safety", "Hungary"), ("Price", "Privacy") , ("Price", "Notice"), ("Privacy", "Price")))
//
//
//      UtilityTypes.aggregate(commands.map(c => remoteExecute(c))).map { _.foreach {
//        case (ComputeWordSimilarityCmd(x, y), ComputeWordSimilarityResponse(Success(r))) =>
//          println(s"'$x' '$y' $r")
//        case _ => throw new IllegalReceiveException("")
//      }}
//
//
//    }
//  }
//
//  private def convertToCommands(a: Seq[(String, String)]): Seq[ComputeWordSimilarityCmd] = {
//    a.map{ case (x,y) => ComputeWordSimilarityCmd(x,y)}
//  }
//}
