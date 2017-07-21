package com.monetoring.v2.Actors;

import akka.actor.ActorRef;
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

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Named("SiteCrawlerActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SiteCrawlerActor extends UntypedActor implements ActorTemplate {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private ActorRef Scrapper;

    public SiteCrawlerActor(){
        log.info(name() + " created");
    }

    @Autowired
    private ActorBuilder actorBuilder;

    @Override
    public String name() {
        return this.getSelf().path().name();
    }

    @Override
    public void shutdown() {
        this.getContext().stop(this.getSelf());
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof Message){
            switch (((Message) message).getMsg()){
                case "scrap":
                    log.debug("Message recieved : " + ((Message) message).getMsg());
                    this.getScrapper().tell(message, getSender());
                    break;
            }
        }else{
            unhandled(message);
            log.error("Unhandled Message : " + ((Message)message).getMsg());
        }
    }

    @Override
    public void preStart() throws Exception {
        this.Scrapper = this.getContext().actorOf(Props.create(ScraperActor.class), "scraperActor");
    }

    private ActorRef getScrapper(){
        if(Scrapper != null)
            return Scrapper;
        else {
            return getContext().actorOf(Props.create(ScraperActor.class), "scraperActor");
        }
    }
}
