package de.unisaarland.st.webtest.nlp.languagedetect

import java.io.{BufferedWriter, File, FileOutputStream}
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

import com.cybozu.labs.langdetect.DetectorFactory
import com.google.common.io.Files
import de.unisaarland.st.webtest.base.{Logging, TryThat}
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation
import de.unisaarland.st.webtest.api.Language

import scala.collection.JavaConverters.asScalaBufferConverter
import scalaz.Success

class LanguageDetector extends Logging {

  private final val enRatio = 0.9
  private final val langRatio = 0.2

  LanguageDetector.loadProfiles()

  def detect(text: String): TValidation[Language] = TryThat.protect {
    val detector = DetectorFactory.create()
    detector.append(text)
    Success(Language(detector.detect()))
  }

  def detectLanguageProbabilities(text: String): TValidation[Seq[(Language, Double)]] = TryThat.protect {
    val detector = DetectorFactory.create()
    detector.append(text)
    Success(detector.getProbabilities.asScala.map(l => Language(l.lang) -> l.prob).sortWith((a, b) => a._2 < b._2))
  }

}

object LanguageDetector {

  val jarFile = new File(getClass.getProtectionDomain.getCodeSource.getLocation.getPath)
  private final val PROFILE_FOLDER = "/de/unisaarland/st/webtest/nlp/languagedetect/res/langProfiles"

  import java.util.jar.JarFile

  private val resource = if (jarFile.isFile) { // Run with JAR file
    val jar = new JarFile(jarFile)
    val entries = jar.entries //gives ALL entries in jar
    val tempDir = Files.createTempDir()

    while ( {
      entries.hasMoreElements
    }) {
      val name = entries.nextElement.getName
      if (name.startsWith(PROFILE_FOLDER + "/")) { //filter according to the path
        Option(getClass.getResourceAsStream(name)) match {
          case None => println(s"ERROR: Resource $name not found")
          case Some(x) =>
            val output = new File(s"$tempDir/${name.split("/").last}")
            println(s"Writing $name to $output")
            val stream = new FileOutputStream(output, false)
            try {
              stream.write(
                Stream.continually(x.read).takeWhile(-1 !=).map(_.toByte).toArray
              )
            } finally {
              stream.close()
            }
        }

      }
    }
    jar.close()
    Some(tempDir)
  }
  else { // Run with IDE
    Option(getClass.getResource(PROFILE_FOLDER)).map { url =>
      val apps = new File(url.toURI)
      apps
//      for (app <- apps.listFiles) {
//        System.out.println(app)
//      }
    }
  }



  private final val lock = new ReentrantLock()

//  private final val resource = {
//    val resource1 = getClass.getResource(PROFILE_FOLDER).toURI
//    println(s"${getClass.getName}: Resource loaded is $resource1")
//    new java.io.File(resource1)
//  }

  private final val isInit = new AtomicBoolean(false)

  def loadProfiles() = {
    lock.lock()

    try {
      if (!isInit.get) {
        println("SHIIIIIIIIIIIIIIIIT" + Thread.currentThread().getId)
        DetectorFactory.loadProfile(resource.get)
        if (!isInit.compareAndSet(false, true)) {
          throw new RuntimeException("This should not happen")
        }
      }
    } finally {
      lock.unlock()
    }
  }



}
