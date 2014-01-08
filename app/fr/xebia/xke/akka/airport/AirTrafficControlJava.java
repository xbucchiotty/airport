package fr.xebia.xke.akka.airport;

import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.UntypedActor;
import scala.collection.Seq;
import scala.collection.immutable.Vector;

public class AirTrafficControlJava extends UntypedActor {

    private final Seq<LocalActorRef> taxiways;
    private final ActorRef groundControl;

    public AirTrafficControlJava(LocalActorRef groundControl, Vector<LocalActorRef> runways) {
        this.taxiways = runways;
        this.groundControl = groundControl;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (PlaneEvent.Incoming$.MODULE$.equals(message)) {
            sender().tell(new Command.Land(taxiways.iterator().next()), self());
        }

        if (PlaneEvent.HasLanded$.MODULE$.equals(message)) {
            sender().tell(new Command.Contact(groundControl), self());
        }

        if (PlaneEvent.HasLeft$.MODULE$.equals(message)) {
            //TODO IMPLEMENTS ME
        }
    }
}
