package controllers

import play.api.libs.iteratee.Enumerator.TreatCont1
import play.api.libs.iteratee.{Input, Iteratee, Enumerator}
import scala.concurrent.{ExecutionContext, Future}

object Enumerator2 {
  /**
   * Like [[play.api.libs.iteratee.Enumerator.unfold]], but allows the unfolding to be done asynchronously.
   *
   * @param s The value to unfold
   * @param f The unfolding function. This will take the value, and return a future for some tuple of the next value
   *          to unfold and the next input, or none if the value is completely unfolded.
   *          $paramEcSingle
   */
  def infiniteUnfold[S, E](s: S)(f: S => Future[Option[(S, E)]])(implicit ec: ExecutionContext): Enumerator[E] = Enumerator.checkContinue1(s)(new TreatCont1[E, S] {
    val pec = ec.prepare()

    def apply[A](loop: (Iteratee[E, A], S) => Future[Iteratee[E, A]], s: S, k: Input[E] => Iteratee[E, A]): Future[Iteratee[E, A]] = {
      f(s).flatMap {
        case Some((newS, e)) => loop(k(Input.El(e)), newS)
        case None => Thread.sleep(50); loop(k(Input.Empty), s)
      }(ExecutionContext.global)
    }
  })

}
