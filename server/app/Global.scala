import controllers.Application
import play.api.Application
import play.api.{Application, GlobalSettings}
import scala.concurrent.duration._
object Global extends GlobalSettings {
  override def onStop(app: Application): Unit = {
    super.onStop(app)
    Application.airportActorSystem.shutdown()
    Application.airportActorSystem.awaitTermination(1 second)
  }
}

