package fr.xebia.xke.akka.player;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Procedure;
import akka.persistence.UntypedEventsourcedProcessor;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.FluentIterable;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import fr.xebia.xke.akka.airport.message.AirTrafficControlReady$;
import fr.xebia.xke.akka.airport.message.InitAirTrafficControl;
import fr.xebia.xke.akka.airport.message.ChaosMonkey;
import fr.xebia.xke.akka.airport.message.PlaneEvent.HasLeft$;
import fr.xebia.xke.akka.airport.message.PlaneEvent.HasLanded$;
import fr.xebia.xke.akka.airport.message.PlaneEvent.Incoming$;
import fr.xebia.xke.akka.airport.message.command.Contact;
import fr.xebia.xke.akka.airport.message.command.Land;

import java.util.*;

public class AirTrafficControl extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    ActorRef groundControl = null;
    Set<ActorRef> runways = new HashSet<>();
    int ackMaxTimeout = 0;

    Map<ActorRef, ActorRef> planeRunwayMap;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Incoming$) {
            ActorRef plane = sender();

        } else if (message instanceof HasLanded$) {
            ActorRef plane = sender();


        } else if (message instanceof HasLeft$) {
            ActorRef plane = getSender();

        } else if (message instanceof ChaosMonkey) {
            throw new ChaosMonkeyException();


        } else if (message instanceof InitAirTrafficControl) {
            InitAirTrafficControl initMessage = (InitAirTrafficControl) message;

            this.groundControl = initMessage.groundControl();
            this.runways = initMessage.runways();
            this.ackMaxTimeout = initMessage.ackMaxDuration();

            sender().tell(AirTrafficControlReady$.MODULE$, self());
        }


    }
}
