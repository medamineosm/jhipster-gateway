package com.monetoring.v2.Actors;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import com.monetoring.v2.Actors.Builder.ActorBuilder;
import com.monetoring.v2.Actors.Messages.Message;
import com.monetoring.v2.Model.DataUrl;
import com.monetoring.v2.Service.DataUrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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

    private long startTime;
    private long numVisited;
    private Set<String> urlToScrap;
    private Map<String, Integer> scrapCount;
    private Map<String, ActorRef> host2Actor;


    @Autowired
    private ActorBuilder actorBuilder;
    @Autowired
    private DataUrlService dataUrlService;
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
                    startTime = System.nanoTime();
                    log.debug("Message recieved : " + ((Message) message).getMsg());
                    //indexerActor = actorBuilder.getIndexer(dataUrlService);
                    scrap((String)(((Message) message).getObject()));
                    break;
                case "scrapFinished":
                    log.debug("scraping finished "+ ((Message) message).getObject());

                    addNewUrls((DataUrl) ((Message) message).getObject());
                    checkAndShutdown(((DataUrl) ((Message) message).getObject()).getUrl());
                    break;
                case "indexFinished":
                    log.info("Finish Indexing : " + ((Message) message).getObject());
                    if(numVisited < maxPages){
                        for (String url: scrapCount.keySet()) {
                            if(scrapCount.get(url) == 1)
                                scrap(url);
                        }
                    }
                    checkAndShutdown(((String)((Message) message).getObject()));
                    break;
                case "scrapFailure":
                    int retries = scrapCount.get(((Message) message).getObject());
                    log.warn("scraping failed "+((Message) message).getObject()+", "+retries+", reason = "+((Message) message).getReason()+"");
                    if (retries < maxRetries){
                        countVisit((String) ((Message) message).getObject());
                        scrap((String) ((Message) message).getObject());
                    }else
                        checkAndShutdown((String) ((Message) message).getObject());
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
            if(numVisited <maxPages) {
                uri = new URL(url);
                log.info("NumberVisited Page ["+numVisited+"]");
                if(!uri.getHost().isEmpty()){
                    ActorRef siteCrawler = addOrGetActor(uri.getHost());
                    numVisited++;
                    urlToScrap.add(url);
                    countVisit(url);
                    siteCrawler.tell(new Message("scrap", url), this.getSelf());
                }else
                    log.error("host is empty" + uri.getHost());
            }

        } catch (MalformedURLException e) {
            log.error(e.getMessage() + " -> " + e.getCause());
            e.printStackTrace();
        }

    }

    private ActorRef addOrGetActor(String host){
        log.debug(host2Actor.toString());
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
        log.info("Number of Url on Queue : " + urlToScrap.size());
        urlToScrap.remove(url);
        // if nothing to visit
        if (urlToScrap.isEmpty()) {
            long endTime = System.nanoTime();
            double duration = (double)(endTime - startTime) / 1000000000.0 ;
            log.info("Execution time " + duration +" seconds");
            this.getSelf().tell(PoisonPill.getInstance(), ActorRef.noSender());
        }
    }

    private void addNewUrls(DataUrl data){
        for(String url : data.getLinks()) {
            log.debug("addNewUrls" + url + ":" + isInternalLink(url));
            if (!this.scrapCount.containsKey(url) && isInternalLink(url)) {
                this.urlToScrap.add(url);
                scrap(url);
            }else
                log.warn("Limit Max Pages ["+maxPages+"] reached !");

        }
    }

    private boolean isInternalLink(String url){
        log.debug("isInternalLink" + url + " ++> " + getHostOfUrl(url));
        return getHostOfUrl(url);
    }

    private boolean getHostOfUrl(String url){
        String host = null;
        try {
            host = new URL(url).getHost();
            log.debug("getHostOfUrl "+"host2Actor.containsKey(host)" + url +" ++>" +host2Actor.containsKey(host));
            return host2Actor.containsKey(host);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
