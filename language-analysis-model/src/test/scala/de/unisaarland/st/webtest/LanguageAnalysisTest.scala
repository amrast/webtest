package de.unisaarland.st.webtest

import de.unisaarland.st.webtest.api.{English_US, German}
import de.unisaarland.st.webtest.base.{Logging, TryThat}
import de.unisaarland.st.webtest.nlp.languagedetect.LanguageDetector
import de.unisaarland.st.webtest.nlp.mallet.TopicModelExtractor
import de.unisaarland.st.webtest.test.TestUtils

import scalaz.{Failure, Success}
import scalaz.Validation.FlatMap.ValidationFlatMapRequested
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

//@RunWith(classOf[JUnitRunner])
/**
  * Tests the langdetect capabilities.
  *
  * @note This class can not be executed with maven (JunitRunner). There seems to be a weird synchronization regarding the profile loading. Even with Thread Save access it does not work.
  *
  */
class LanguageAnalysisTest extends TestUtils with Logging {

  final val detector: LanguageDetector = new LanguageDetector


  "The language analysis should be able to detect the correct language" >> {


    "be able to detect english as the main language" >> TryThat.protect {


      detector.detect("The cat kills the dog and eats food.") flatMap {
        case English_US => Success("Test correctly identified english")
        case _ => Failure(new RuntimeException("The text should be english"))
      }

    }

    "be able to detect german as the main language" >> TryThat.protect {

      detector.detect("Hund frisst Katze und entscheidet sich gegen Mäuse.") flatMap {
        case German => Success("Test correctly identified german")
        case a => Failure(new RuntimeException(s"The text should be $German, but was $a"))
      }
    }

//    "be able to extract the topics" >> {
//      val docs = Seq(loadTextFile("./new_york_citizendium.txt"), loadTextFile("./new_york_wikipedia.txt"))
//
//      val topicModel = TopicModel(Seq(), true)
//      topicModel.createTopicModel(docs, 5, 5, numThreads = 2)
//
//      done
//    }

    "be able to extract the topics" >> {
      val docs = Seq(loadTextFile("./new_york_citizendium.txt"), loadTextFile("./new_york_wikipedia.txt"))

      val topicModel = TopicModelExtractor(Seq(), true)
      println(topicModel.createTopicModel(docs.slice(0,1), 5, 5, numThreads = 2).head)

      println(topicModel.createTopicModel(docs.slice(1,2), 5, 5, numThreads = 2).head)


      done
    }


  }


  def loadTextFile(path: String): String = scala.io.Source.fromFile(getClass.getResource(path).toURI).mkString

}