package fr.xebia.xke.akka.airport;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import fr.xebia.xke.akka.airport.command.ParkAt;
import fr.xebia.xke.akka.airport.command.Taxi;

import java.util.Set;

public class GroundControl extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    public GroundControl() {
        log.info("GroundControl created");
    }

    private Procedure<Object> ready(final Set<ActorRef> taxiways,
                                           final Set<ActorRef> gates,
                                           final Integer taxiwayCapacity,
                                           final Integer ackMaxDuration) {
        return new Procedure<Object>() {
            @Override
            public void apply(Object message) throws Exception {
                //A new plane is on a taxiway and requests to taxi
                if (message instanceof PlaneEvent.Incoming$) {
                    ActorRef plane = getSender();
                    //we should find him a free taxiway
                    //tell him to taxy on it
                    plane.tell(new Taxi(taxiways.iterator().next()), getSelf());
                    //and stores in this actor
                    //that the targeted taxiway has on free slot less than before

                    //If all the taxiway are full
                    //we should not answer yet
                    //and stashing its request to taxi
                    //and reply when a free slot on a taxiway is gained
                }
                //A plane is at the end of a taxiway
                //It requests a gate
                else if (message instanceof PlaneEvent.EndOfTaxi$) {
                    ActorRef plane = getSender();

                    //We should find him a free gate
                    //tell the plane to park
                    plane.tell(new ParkAt(gates.iterator().next()), getSelf());

                    //and stores in this actor
                    //that the targeted gate is occupied

                    //if all gates are occupied
                    //we should not answer yet
                    //and stashing its request to park

                    //When a gate is free once again
                    //we should unstash the request

                }

                //A plane has parked
                else if (message instanceof PlaneEvent.HasParked$) {
                    ActorRef plane = getSender();

                    //The plane is no longer on the taxiway
                    //The taxiway has gained one slot free

                    //If there is stashed requests to taxi
                    //We should unstash one
                }

                //A plane has left a gate
                else if (message instanceof PlaneEvent.HasLeft$) {
                    ActorRef plane = getSender();
                    //The plane has ended the game
                    //The gate is now free

                    //If there is stashed requests to park
                    //We should unstash one

                }

            }
        };
    }

    private Procedure<Object> uninitialized = new Procedure<Object>() {
        @Override
        public void apply(Object message) {
            if (message instanceof InitGroundControl) {
                sender().tell(GroundControlReady$.MODULE$, self());

                log.info("GroundControl ready");

                InitGroundControl initMessage = (InitGroundControl) message;

                getContext().become(ready(
                        initMessage.taxiways(),
                        initMessage.gates(),
                        initMessage.taxiwayCapacity(),
                        initMessage.ackMaxDuration()));
            }
        }
    };

    @Override
    public void onReceive(Object message) throws Exception {
        uninitialized.apply(message);
    }

}
