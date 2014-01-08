package fr.xebia.xke.akka.airport;

import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.UntypedActor;
import scala.collection.Iterator;
import scala.collection.Seq;

import java.util.*;

public class GroundControlJava extends UntypedActor {

    private final Seq<LocalActorRef> taxiways;
    private final Seq<LocalActorRef> gates;
    private final Map<ActorRef, ActorRef> busyGatesToPlanes = new HashMap<>();

    private final Set<ActorRef> planesWaitingToPark = new HashSet<>();

    public GroundControlJava(scala.collection.immutable.Vector<LocalActorRef> taxiways, scala.collection.immutable.Vector<LocalActorRef> gates) {
        this.taxiways = taxiways;
        this.gates = gates;
    }

    private int globalCounter = 0;

    @Override
    public void onReceive(Object message) throws Exception {
        if (PlaneEvent.Incoming$.MODULE$.equals(message)) {
            List<ActorRef> taxis = new ArrayList<>();
            for(Iterator<LocalActorRef> i = taxiways.iterator(); i.hasNext();) {
                LocalActorRef taxiWay = i.next();
                taxis.add(taxiWay);
            }
            globalCounter++;
            sender().tell(new Command.Taxi(taxis.get(globalCounter%taxis.size())), self());
        }

        if (PlaneEvent.EndOfTaxi$.MODULE$.equals(message)) {
            tryParking(sender());
        }

        if (PlaneEvent.Taxiing$.MODULE$.equals(message)) {
            //TODO IMPLEMENTS ME
        }

        if (PlaneEvent.HasParked$.MODULE$.equals(message)) {
            //TODO IMPLEMENTS ME
        }

        if (PlaneEvent.HasLeft$.MODULE$.equals(message)) {
            for (Map.Entry<ActorRef, ActorRef> gateToPlane : busyGatesToPlanes.entrySet()) {
                ActorRef gate = gateToPlane.getKey();
                ActorRef plane = gateToPlane.getValue();

                if (sender().equals(plane)) {
                    busyGatesToPlanes.remove(gate);

                    for (java.util.Iterator<ActorRef> i = planesWaitingToPark.iterator(); i.hasNext();) {
                        ActorRef waitingPlane = i.next();
                        if (tryParking(waitingPlane)) {
                            i.remove();
                        }
                    }
                    break;
                }
            }
        }

    }

    private boolean tryParking(ActorRef plane) {
        for(Iterator<LocalActorRef> i = gates.iterator(); i.hasNext();) {
            LocalActorRef gate = i.next();
            if (busyGatesToPlanes.get(gate) == null) {
                busyGatesToPlanes.put(gate, plane);
                sender().tell(new Command.ParkAt(gate), self());
                return true;
            }
        }

        planesWaitingToPark.add(plane);
        return false;
    }
}
