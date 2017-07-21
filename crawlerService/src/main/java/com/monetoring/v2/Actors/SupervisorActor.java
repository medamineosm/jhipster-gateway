package com.monetoring.v2.Actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import com.monetoring.v2.Actors.Builder.ActorBuilder;
import com.monetoring.v2.Actors.Messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.monetoring.v2.CrawlerApplication.MAX_PAGES;
import static com.monetoring.v2.CrawlerApplication.MAX_RETRIES;

/**
 * Created by Ouasmine on 20/07/2017.
 */
@Named("SuperVisorActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SupervisorActor extends UntypedActor implements ActorTemplate {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private ActorRef indexerActor;

    private long numVisited;
    private Set<String> urlToScrap;
    private Map<String, Integer> scrapCount;
    private Map<String, ActorRef> host2Actor;


    @Autowired
    private ActorBuilder actorBuilder;
    private int maxPages;
    private int maxRetries;

    public SupervisorActor(){
        log.info(this.name() + "created");
        this.numVisited = 0;
        this.urlToScrap = new LinkedHashSet<>();
        this.scrapCount = new HashMap<>();
        this.host2Actor = new HashMap<>();
        this.maxPages = MAX_PAGES;
        this.maxRetries = MAX_RETRIES;

    }

    @Override
    public String name() {
        return this.getSelf().path().name()+" ";
    }

    @Override
    public void shutdown() {
        this.getContext().stop(this.getSelf());
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if(message instanceof Message){
            switch (((Message) message).getMsg()){
                case "start":
                    log.debug("Message recieved : " + ((Message) message).getMsg());
                    indexerActor = actorBuilder.getIndexer();
                    scrap((String)(((Message) message).getObject()));
                    break;
                case "scrapFinished":
                    log.info("scraping finished "+ ((Message) message).getObject());
                    break;
                case "indexFinished":
                    if(numVisited < maxPages){
                        for (String url: scrapCount.keySet()) {
                            if(scrapCount.get(url) == 1)
                                scrap(url);
                        }
                    }
                    checkAndShutdown(((String)((Message) message).getObject()));
                    break;
                case "scrapFailure":
                    int retries = scrapCount.get((String)((Message) message).getObject());
                    log.error("scraping failed "+((Message) message).getObject()+", "+retries+", reason = "+((Message) message).getReason()+"");
                    break;

            }
        }else{
            unhandled(message);
            log.error("Unhandled Message : " + ((Message)message).getMsg());
        }
    }

    private void scrap(String url){
        URL uri = null;
        try {
            uri = new URL(url);
            log.debug("get SiteCrawlerActor for host : "+uri.getHost());
            if(!uri.getHost().isEmpty()){
                ActorRef siteCrawler = addOrGetActor(uri.getHost());
                numVisited++;
                urlToScrap.add(url);
                siteCrawler.tell(new Message("scrap", url), this.getSelf());
            }else
                log.error("host is empty" + uri.getHost());
        } catch (MalformedURLException e) {
            log.error(e.getMessage() + " -> " + e.getCause());
            e.printStackTrace();
        }

    }

    private ActorRef addOrGetActor(String host){
        if(this.host2Actor.keySet().contains(host))
            return this.host2Actor.get(host);
        else{
            ActorRef actor = actorBuilder.buildActor("SiteCrawlerActor", "sitecrawleractor-"+host);
            this.host2Actor.put(host, actor);
            return actor;
        }

    }

    private void countVisit(String url){
        if(scrapCount.containsKey(url)){
            scrapCount.put(url, scrapCount.get(url)+1);
        }else {
            scrapCount.put(url, 1);
        }
    }

    private void checkAndShutdown(String url){
        urlToScrap.iterator().next();
        // if nothing to visit
        if (urlToScrap.isEmpty()) {
            this.getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
        }
        urlToScrap.iterator().remove();
    }
}
