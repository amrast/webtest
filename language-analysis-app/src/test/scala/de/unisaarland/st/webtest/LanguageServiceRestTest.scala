package de.unisaarland.st.webtest

import de.unisaarland.st.webtest.api.{English_US, LanguageProbability}
import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.client.LanguageAnalysisClient
import de.unisaarland.st.webtest.server.LanguageServiceServer
import de.unisaarland.st.webtest.test.TestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.AfterAll

import scalaz.Success

@RunWith(classOf[JUnitRunner])
class LanguageServiceRestTest extends TestUtils with Logging with AfterAll {


  private val server_analyzer = new NoopLanguageAnalyzer

  private val server = LanguageServiceServer(19992, server_analyzer)
  private val client = new LanguageAnalysisClient(server.uri)

 "The REST API server" >> {
   "checks language probabilities" >> {
     client.getLanguageProbabilities("test") mustEqual  Success(LanguageProbability(English_US, 1.0))
   }

   "check topic models" >> {
     client.getTopicsForString("something", 1, 1)
   }

   "check word similarity" >> {
     client.computeSemanticWordSimilarity("word", "something")
   }

   "check text similarity" >> {
     client.computeSemanticWordSimilarity("This is the word you are looking for", "This word is not nice")
   }

   "check if words are in corpus" >> {
     client.validWord("test") mustEqual server_analyzer.validWord("test")
     client.validWord("dahjsdha") mustEqual Success(false)
   }

   "get the words in the corpus" >> {
     client.getCorpus mustEqual server_analyzer.getCorpus
   }

   "get word vector" >> {
     client.getWordVector("test")
   }
 }

  override def afterAll(): Unit = {
    server.close()
    client.close()
  }
}
