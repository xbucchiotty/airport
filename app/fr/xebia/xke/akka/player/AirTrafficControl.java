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

    Map<ActorRef, ActorRef> planeRunwayMap = new HashMap<>();
    List<ActorRef> waitingPlanes = new ArrayList<>();


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Incoming$) {
            ActorRef plane = sender();
            
            Set<ActorRef> freeRunways = Sets.difference(runways, Sets.newHashSet(planeRunwayMap.values()));

            if(!freeRunways.isEmpty()){
                ActorRef firstFreeRunway = freeRunways.iterator().next();
                planeRunwayMap.put(plane, firstFreeRunway);
                
                context().actorOf(ActorRepeater.props(new Land(firstFreeRunway), plane, ackMaxTimeout));

            }else{
                waitingPlanes.add(plane);
            }


        } else if (message instanceof HasLanded$) {
            ActorRef plane = sender();
            context().actorOf(ActorRepeater.props(new Contact(groundControl), plane, ackMaxTimeout));

        } else if (message instanceof HasLeft$) {
            ActorRef plane = getSender();
            ActorRef freeRunway = planeRunwayMap.remove(plane);

            if(!waitingPlanes.isEmpty()){
                ActorRef firstWaitingPlane = waitingPlanes.get(0);
                waitingPlanes.remove(0);
                planeRunwayMap.put(firstWaitingPlane, freeRunway);

                context().actorOf(ActorRepeater.props(new Land(freeRunway), firstWaitingPlane, ackMaxTimeout));

            }

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
    
    public static class PlaneAffected{
        public final ActorRef plane; 
        public final ActorRef runway;
        
        public PlaneAffected(ActorRef plane,ActorRef runway ){
            this.plane = plane;
            this.runway = runway;
        }
        
    }
    
    public static class PlaneDisaffected{
        public final ActorRef plane;
        
        public PlaneDisaffected(ActorRef plane){
            this.plane = plane;
        }
    }
    public static class PlaneEnqueued{
        public final ActorRef plane;
        
        public PlaneEnqueued(ActorRef plane){
            this.plane = plane;
        }
    }
    public static class PlaneDequeued{
        
    }

    public static class Initiated{
        public final ActorRef groundControl;
        public final Set<ActorRef> runways;
        public final Integer ackMaxTimeout;
        
        public Initiated(ActorRef groundControl, Set<ActorRef> runways, Integer ackMaxTimeout){
            this.groundControl = groundControl;
            this.runways = runways;
            this.ackMaxTimeout = ackMaxTimeout;
        }
    }
}
