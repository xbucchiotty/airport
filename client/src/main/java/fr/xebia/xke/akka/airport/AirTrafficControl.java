package fr.xebia.xke.akka.airport;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import fr.xebia.xke.akka.airport.command.Contact;
import fr.xebia.xke.akka.airport.command.Land;

import java.util.Set;

public class AirTrafficControl extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

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
                    ActorRef plane = sender();

                    //and stores in this actor
                    //that the targeted runway is allocated to this plane

                    //if there is no runway available
                    //you should not reply now
                    //but stashing the request
                    //to call him back when a runway will be free
                    plane.tell(new Land((runways.iterator().next())), getSelf());

                }
                //A plane has landed
                else if (message instanceof PlaneEvent.HasLanded$) {
                    ActorRef plane = sender();

                    //It does not know yet the ground control
                    //You reply with the reference to the ground control
                    //Nothing very useful to add there
                    plane.tell(new Contact(groundControl), getSelf());

                }
                //The plane has left the runway
                else if (message instanceof PlaneEvent.HasLeft$) {
                    ActorRef plane = getSender();
                    //It's now free to accept a new plane
                    //and if the actor has stashed request
                    //it's time to reply to  them
                }
            }
        };
    }

    private Procedure<Object> uninitialized = new Procedure<Object>() {
        @Override
        public void apply(Object message) {
            if (message instanceof InitAirTrafficControl) {
                sender().tell(AirTrafficControlReady$.MODULE$, self());

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
