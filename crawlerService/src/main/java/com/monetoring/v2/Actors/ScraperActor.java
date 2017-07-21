package com.monetoring.v2.Actors;

import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Named("ScraperActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ScraperActor extends UntypedActor implements ActorTemplate {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ScraperActor(){
        log.info(name() + " constructor");
    }


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

    }
}
