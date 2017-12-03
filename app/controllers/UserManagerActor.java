package controllers;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.HashMap;
import java.util.Map;


public class UserManagerActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final Map<Integer, ActorRef> users = new HashMap<>();

    static public class RegisterUser {
        private final Integer userId;
        private final ActorRef out;

        public RegisterUser(Integer userId, ActorRef out) {
            this.userId = userId;
            this.out = out;
        }

        public Integer getUserId() {
            return userId;
        }

        public ActorRef getOut() {
            return out;
        }
    }

    static public class UserMessage {
        private final Integer userId;
        private final String msg;

        public UserMessage(Integer userId, String msg) {
            this.userId = userId;
            this.msg = msg;
        }

        public Integer getUserId() {
            return userId;
        }

        public String getMsg() {
            return msg;
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterUser.class, u -> {
                    users.put(u.userId, u.out);
                })
                .match(UserMessage.class, um -> {
                    ActorRef out = users.get(um.userId);
                    if (out != null) {
                        out.tell(um.msg, ActorRef.noSender());
                    } else {
                        log.info("Skipping message as user " + um.userId + " has not connected yet");
                    }
                })
                .matchAny(o -> log.info("received unknown message"))
                .build();
    }
}
