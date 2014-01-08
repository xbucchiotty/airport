package fr.xebia.xke.akka.airport;

import akka.actor.ActorRef;
import akka.actor.LocalActorRef;
import akka.actor.UntypedActor;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.collection.immutable.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AirTrafficControlJava extends UntypedActor {

    private final Seq<LocalActorRef> runways;
    private final ActorRef groundControl;
    private final Map<ActorRef, ActorRef> busyRunWaysToPlanes = new HashMap<>();
    private final Set<ActorRef> waitingPlanes = new HashSet<>();

    public AirTrafficControlJava(LocalActorRef groundControl, Vector<LocalActorRef> runways) {
        this.runways = runways;
        this.groundControl = groundControl;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (PlaneEvent.Incoming$.MODULE$.equals(message)) {
            ActorRef plane = sender();
            tryLanding(plane);
        }

        if (PlaneEvent.HasLanded$.MODULE$.equals(message)) {
            for (Map.Entry<ActorRef, ActorRef> runWayToPlane : busyRunWaysToPlanes.entrySet()) {
                ActorRef plane = runWayToPlane.getValue();

                if (sender().equals(plane)) {
                    sender().tell(new Command.Contact(groundControl), self());
                    break;
                }
            }
        }

        if (PlaneEvent.HasLeft$.MODULE$.equals(message)) {
            for (Map.Entry<ActorRef, ActorRef> runWayToPlane : busyRunWaysToPlanes.entrySet()) {
                ActorRef runWay = runWayToPlane.getKey();
                ActorRef plane = runWayToPlane.getValue();

                if (sender().equals(plane)) {
                    busyRunWaysToPlanes.remove(runWay);

                    for (java.util.Iterator<ActorRef> i = waitingPlanes.iterator(); i.hasNext();) {
                        ActorRef waitingPlane = i.next();
                        if (tryLanding(waitingPlane)) {
                            i.remove();
                        }
                    }

                    break;
                }
            }
        }
    }

    private boolean tryLanding(ActorRef plane) {
        for(Iterator<LocalActorRef> i = runways.iterator(); i.hasNext();) {
            LocalActorRef runWay = i.next();
            if (busyRunWaysToPlanes.get(runWay) == null) {
                busyRunWaysToPlanes.put(runWay, plane);
                plane.tell(new Command.Land(runWay), self());
                return true;
            }
        }

        waitingPlanes.add(plane);
        return false;
    }
}
