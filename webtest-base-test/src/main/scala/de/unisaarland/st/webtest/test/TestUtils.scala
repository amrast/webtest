package de.unisaarland.st.webtest.test

import java.nio.file.Files

import de.unisaarland.st.webtest.base.Logging
import de.unisaarland.st.webtest.base.UtilityTypes.TValidation

import scala.collection.mutable.ListBuffer
import scala.tools.nsc.io.Directory
import scalaz.{Failure, Success}

trait TestUtils extends org.specs2.mutable.Specification { self : Logging =>

  implicit def validationToResult[T](testResult: TValidation[T]): org.specs2.execute.Result = {
    testResult match {
      case Success(()) => done
      case Success(x: Seq[_]) =>
        logger.info(x.mkString(s"Test returned the following results: ${System.lineSeparator()}\t", s"${System.lineSeparator()}\t", System.lineSeparator()))
        done
      case Success(x) if x.toString.trim.isEmpty =>
        done
      case Success(x) =>
        logger.info(s"Test returned '$x'")
        done
      case Failure(e) =>
        logger.error(s"Test failed", e)
        failure(e.getMessage)
    }
  }

  implicit def validationResultToResult(testResult: TValidation[org.specs2.execute.Result]): org.specs2.execute.Result = {
    testResult match {
      case Success(a) => a
      case Failure(e) =>
        logger.error(s"Test failed", e)
        failure(org.specs2.execute.Failure(e.getMessage, stackTrace = e.getStackTrace.toList))
    }
  }

  def createTempDirectory(prefix: String = ""): Directory = {
    Directory(Files.createTempDirectory(prefix).toAbsolutePath.toString)
  }

  def validate[T,S](acts: Seq[T], act: T => TValidation[S]): TValidation[Seq[S]] = {
    acts.foldLeft(scalaz.Success(ListBuffer.empty[S]): TValidation[ListBuffer[S]]){
      case (Success(tmp), a) => act(a).map { r =>
          tmp.append(r)
          tmp
      }
      case (Failure(e), _) => Failure(e)
    }
  }

  def validate[T](acts: Seq[TValidation[T]]): TValidation[Seq[T]] = {
    acts.foldLeft(scalaz.Success(ListBuffer.empty[T]): TValidation[ListBuffer[T]]){
      case (Success(tmp), Success(a)) =>
        tmp.append(a)
        Success(tmp)
      case (_, Failure(e)) =>
        e.printStackTrace()
        Failure(e)
      case (Failure(e), _) =>
        e.printStackTrace()
        Failure(e)
    }
  }



}

object TestUtils extends TestUtils with Logging


