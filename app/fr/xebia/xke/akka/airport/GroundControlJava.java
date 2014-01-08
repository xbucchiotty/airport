package fr.xebia.xke.akka.airport;

import akka.actor.LocalActorRef;
import akka.actor.UntypedActor;
import scala.collection.Seq;

public class GroundControlJava extends UntypedActor {

    private final Seq<LocalActorRef> taxiways;
    private final Seq<LocalActorRef> gates;

    public GroundControlJava(scala.collection.immutable.Vector<LocalActorRef> taxiways, scala.collection.immutable.Vector<LocalActorRef> gates) {
        this.taxiways = taxiways;
        this.gates = gates;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (PlaneEvent.Incoming$.MODULE$.equals(message)) {
            sender().tell(new Command.Taxi(taxiways.iterator().next()), self());
        }

        if (PlaneEvent.EndOfTaxi$.MODULE$.equals(message)) {
            sender().tell(new Command.ParkAt(gates.iterator().next()), self());
        }

        if (PlaneEvent.Taxiing$.MODULE$.equals(message)) {
            //TODO IMPLEMENTS ME
        }

        if (PlaneEvent.HasParked$.MODULE$.equals(message)) {
            //TODO IMPLEMENTS ME
        }

        if (PlaneEvent.HasLeft$.MODULE$.equals(message)) {
            //TODO IMPLEMENTS ME
        }

    }
}
