package com.monetoring.v2.Actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.UntypedActor;
import com.monetoring.v2.Actors.Builder.ActorBuilder;
import com.monetoring.v2.Actors.Messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

import java.util.HashSet;
import java.util.Set;

import static com.monetoring.v2.CrawlerApplication.MAX_PAGES;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Named("SiteCrawlerActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SiteCrawlerActor extends UntypedActor implements ActorTemplate {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ActorBuilder actorBuilder;
    private Set<ActorRef> Scrappers;


    public SiteCrawlerActor(){
        log.info(name() + " created");
    }

    @Override
    public String name() {
        return this.getSelf().path().name();
    }

    @Override
    public void forceShutDown() {
        this.context().stop(this.self());
    }
    @Override
    public void shutdown() {
        this.self().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof Message){
            switch (((Message) message).getMsg()){
                case "scrap":
                    getActorScrapper((String) ((Message) message).getObject()).tell(new Message((Message) message, "scrapUrl"), self());
                    break;
                case "scrapFailure":
                case "scrapFinished":
                    sender().tell("die", self());
                    actorBuilder.getSuperVisor().tell(message, sender());
                    break;
            }
        }else{
            unhandled(message);
            log.error("Unhandled Message : " + ((Message)message).getMsg());
        }
    }

    @Override
    public void preStart() throws Exception {
        this.Scrappers = new HashSet<>();
        //actorBuilder.getScrapper(this);
    }

    private ActorRef getActorScrapper(String url){
        //if(Scrapper != null)
        //    return Scrapper;
        //else {
            return actorBuilder.getScrapper(this);
        //}
    }
}
