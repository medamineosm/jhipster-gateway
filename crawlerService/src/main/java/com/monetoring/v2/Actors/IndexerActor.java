package com.monetoring.v2.Actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import com.monetoring.v2.Actors.Messages.Message;
import com.monetoring.v2.Model.DataUrl;
import com.monetoring.v2.Service.DataUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Named("IndexerActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IndexerActor extends UntypedActor implements ActorTemplate {
    private Set<DataUrl> urls = new HashSet<>();
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private DataUrlService dataUrlService;

    public IndexerActor(DataUrlService dataUrlService){
        log.debug(name() + " constructor");
        this.dataUrlService = dataUrlService;
    }



    @Override
    public String name() {
        return this.getSelf().path().name();
    }

    @Override
    public void shutdown() {
        this.self().tell(PoisonPill.getInstance(), ActorRef.noSender());
    }

    @Override
    public void forceShutDown() {
        this.context().stop(this.self());
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof Message){
            switch (((Message) message).getMsg()){
                case "index":
                    log.debug("Indexing : " + (((DataUrl)((Message) message).getObject()).getUrl()));
                    urls.add((DataUrl) ((Message) message).getObject());
                    dataUrlService.save((DataUrl) ((Message) message).getObject());
                    //forceShutDown();
                    break;
            }
        }else{
            unhandled(message);
            log.error("Unhandled Message : " + ((Message)message).getMsg());
        }
    }

    @Override
    public void postStop() throws Exception {
        log.debug("Finish Indexing the urls Number of indexed urls " + urls.size());
        sender().tell("indexFinished", self());
    }
}
