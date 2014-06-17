package fr.xebia.xke.akka.player;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import fr.xebia.xke.akka.airport.message.PlaneEvent.Ack$;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class ActorRepeater extends UntypedActor {

    ActorRef target;
    Object message;
    Integer ackMaxTimeout;

    public static final String TIMEOUT_MESSAGE = "Timeout";

    public ActorRepeater(Object message, ActorRef target, Integer ackMaxTimeout) {
        this.message = message;
        this.target = target;
        this.ackMaxTimeout = ackMaxTimeout;
    }

    public static Props props(final Object message, final ActorRef target, final Integer ackMaxTimeout) {
        return Props.create(new Creator<ActorRepeater>() {

            @Override
            public ActorRepeater create() throws Exception {
                return new ActorRepeater(message, target, ackMaxTimeout);
            }
        });
    }

    @Override
    public void preStart() throws Exception {
        target.tell(message, self());
        FiniteDuration duration = new FiniteDuration(ackMaxTimeout, TimeUnit.MILLISECONDS);
        context().system().scheduler().scheduleOnce(duration, self(), TIMEOUT_MESSAGE, context().system().dispatcher(), self());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Ack$) {
            context().stop(self());

        } else if (message.equals(TIMEOUT_MESSAGE)) {
            preStart();
        }
    }
}