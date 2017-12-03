/*
 * Copyright (C) 2009-2016 Lightbend Inc. <http://www.lightbend.com>
 */
package controllers;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.AsPublisher;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.reactivestreams.Publisher;
import play.libs.EventSource;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JavaEventSourceController extends Controller {
    private final ActorRef userManager;
    private final Materializer materializer;

    @Inject
    public JavaEventSourceController(Materializer materializer, ActorSystem actorSystem) {
        this.materializer = materializer;
        this.userManager = actorSystem.actorOf(Props.create(UserManagerActor.class));
    }

    public Result postMessage(Integer uid, String msg) {
        userManager.tell(new UserManagerActor.UserMessage(uid, msg), ActorRef.noSender());
        return ok();
    }

    public Result stream(Integer uid) {
        Pair<ActorRef, Publisher<String>> pair = Source.<String>actorRef(1000, OverflowStrategy.dropHead())
                .toMat(Sink.asPublisher(AsPublisher.WITH_FANOUT), Keep.both()).run(materializer);
        ActorRef actor = pair.first();
        userManager.tell(new UserManagerActor.RegisterUser(uid, actor), ActorRef.noSender());
        Source<String, NotUsed> actorSource = Source.fromPublisher(pair.second()).log("actorPublisher");

        return ok().chunked(actorSource.map(EventSource.Event::event).via(EventSource.flow())).as(Http.MimeTypes.EVENT_STREAM);
    }

}
