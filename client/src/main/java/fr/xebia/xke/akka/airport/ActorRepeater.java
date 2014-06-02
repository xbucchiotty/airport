package fr.xebia.xke.akka.airport;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import fr.xebia.xke.akka.airport.command.Ack;
import fr.xebia.xke.akka.airport.command.Ack$;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class ActorRepeater extends UntypedActor {

    public static final String TIMEOUT_MESSAGE = "Timeout";
    private final Object message;
    private final ActorRef target;
    private final Integer ackMaxTimeout;

    public static Props props(final Object message, final ActorRef target, final Integer ackMaxTimeout) {
        return Props.create(new Creator<ActorRepeater>() {

            private static final long serialVersionUID = 1L;

            @Override
            public ActorRepeater create() throws Exception {
                return new ActorRepeater(message, target, ackMaxTimeout);
            }
        });
    }

    public ActorRepeater(Object message, ActorRef target, Integer ackMaxTimeout) {

        this.message = message;
        this.target = target;
        this.ackMaxTimeout = ackMaxTimeout;
    }

    /**
     * User overridable callback.
     * <p/>
     * Is called when an Actor is started.
     * Actor are automatically started asynchronously when created.
     * Empty default implementation.
     */
    @Override
    public void preStart() throws Exception {

        FiniteDuration duration = new FiniteDuration(ackMaxTimeout, TimeUnit.MILLISECONDS);
        context().system().scheduler().scheduleOnce(duration, self(), TIMEOUT_MESSAGE, context().system().dispatcher(), self());
        target.tell(message, self());

    }

    /**
     * To be implemented by concrete UntypedActor, this defines the behavior of the
     * UntypedActor.
     */
    @Override
    public void onReceive(Object message) throws Exception {

        if(message instanceof Ack$) {

            context().stop(self());

        } else if (message.equals(TIMEOUT_MESSAGE)) {

            preStart();

        }

    }

}
