package de.unisaarland.st.webtest.base

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scalaz.{Failure, Success, Validation}
import scalaz.syntax.validation._
import scalaz.syntax.applicative._

object UtilityTypes {

  /**
   * TValidation is a scalaz Validation with the left hand type fixed to Throwable
   */
  type TValidation[A] = Validation[Throwable, A]

  /**
   * FutureTValidation is a Future bearing a scalaz Validation with the left hand type fixed to Throwable
   */
  type FutureTValidation[A] = Future[Validation[Throwable, A]]


  def aggregate[A](r: Traversable[TValidation[A]]): TValidation[Seq[A]] = TryThat.protectSilent {
    r.map(_.toValidationNel).foldLeft(Seq.empty[A].successNel[Throwable]) {
      case (acc, v) => (acc |@| v)(_ :+ _)
    } match {
      case a: Seq[A] @unchecked => Success(a)
      case Success(a) => Success(a)
      case scalaz.Failure(e) => Failure(e.head)
      case a @ _ => throw new IllegalArgumentException(s"I suck at coping with ${a.getClass}")
    }
  }

//  @unchecked
//  def aggregate[A](r: Iterator[TValidation[A]]): TValidation[Seq[A]] = {
//    aggregate(r.toTraversable)
//  }

  def aggregate[A](r: Traversable[FutureTValidation[A]])(implicit ex: ExecutionContext): FutureTValidation[Seq[A]] = {
    Future.sequence(r).map { a: Traversable[TValidation[A]] => aggregate(a) }
  }

  def aggregateLeft[A](r: Traversable[() => TValidation[A]]): TValidation[Seq[A]] = {
    r.headOption match {
      case None =>
        Success(Seq.empty[A])
      case Some(act) => act() match {
        case Failure(e) => Failure(e)
        case Success(a) =>
          aggregateLeft(r.tail) match {
            case Success(rest) => Success(Seq(a) ++ rest)
            case Failure(e) => Failure(e)
          }
      }
    }
  }

}

