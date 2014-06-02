package fr.xebia.xke.akka.airport;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import fr.xebia.xke.akka.airport.command.Contact;
import fr.xebia.xke.akka.airport.command.Land;

import java.util.*;

public class AirTrafficControl extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private Map<ActorRef, ActorRef> planeRunwayMap;
    private List<ActorRef> waitingPlanes;

    public AirTrafficControl() {
        log.info("ATC created");
    }

    private Procedure<Object> ready(final ActorRef groundControl,
                                    final Set<ActorRef> runways,
                                    final Integer ackMaxTimeout) {
        return new Procedure<Object>() {
            @Override
            public void apply(Object message) throws Exception {
                //Plane incomes from the sky
                if (message instanceof PlaneEvent.Incoming$) {
                    //it requests to land
                    //you should tell the sender (the plane)
                    //to land on a free runway

                    //and stores in this actor
                    //that the targeted runway is allocated to this plane

                    //if there is no runway available
                    //you should not reply now
                    //but stashing the request
                    //to call him back when a runway will be free

                    ActorRef plane = sender();

                    Set<ActorRef> freeRunways = new HashSet<>(runways);
                    freeRunways.removeAll(planeRunwayMap.values());

                    if(!freeRunways.isEmpty()) {

                        ActorRef firstFreeRunway = freeRunways.iterator().next();
                        planeRunwayMap.put(plane, firstFreeRunway);

                        plane.tell(new Land(firstFreeRunway), self());

                    } else {

                        waitingPlanes.add(plane);

                    }

                }
                //A plane has landed
                else if (message instanceof PlaneEvent.HasLanded$) {

                    //It does not know yet the ground control
                    //You reply with the reference to the ground control

                    ActorRef plane = sender();

                    plane.tell(new Contact(groundControl), self());

                }
                //The plane has left the runway
                else if (message instanceof PlaneEvent.HasLeft$) {
                    //It's now free to accept a new plane
                    //and if the actor has stashed request
                    //it's time to reply to  them

                    ActorRef plane = getSender();

                    ActorRef freeRunway = planeRunwayMap.remove(plane);

                    if(!waitingPlanes.isEmpty()) {

                        ActorRef firstWaitingPlane = waitingPlanes.get(0);
                        waitingPlanes.remove(0);

                        planeRunwayMap.put(firstWaitingPlane, freeRunway);

                        firstWaitingPlane.tell(new Land(freeRunway), self());

                    }

                }
            }
        };
    }

    private Procedure<Object> uninitialized = new Procedure<Object>() {
        @Override
        public void apply(Object message) {
            if (message instanceof InitAirTrafficControl) {
                sender().tell(AirTrafficControlReady$.MODULE$, self());

                planeRunwayMap = new HashMap<>();
                waitingPlanes = new ArrayList<>();

                log.info("ATC ready");

                InitAirTrafficControl initMessage = (InitAirTrafficControl) message;

                getContext().become(ready(initMessage.groundControl(), initMessage.runways(), initMessage.ackMaxDuration()));
            }
        }
    };

    @Override
    public void onReceive(Object message) throws Exception {
        uninitialized.apply(message);
    }
}
