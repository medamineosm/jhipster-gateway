package com.monetoring.v2.Service;

import akka.actor.ActorRef;
import com.monetoring.v2.Actors.Builder.ActorBuilder;
import com.monetoring.v2.Actors.Messages.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Service
public class CrawlerService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ActorBuilder builder;
    ActorRef superVisor;
    Map<String, ActorRef> actorPerHost  = new HashMap<>();

    private ActorRef SiteCrawlerPerHost(String url){
        String host = null;
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if(actorPerHost.containsKey(host.toLowerCase())){
            return actorPerHost.get(host.toLowerCase());
        }
        ActorRef actorRef = builder.
                buildActor("SiteCrawlerActor", "sitecrawleractor-"+host);
        actorPerHost.put(host.toLowerCase(), actorRef);

        return actorRef;

    }

    public void Crawl(Collection<String> urls) {
        superVisor = builder.getSuperVisor();
        for (String url: urls) {
            //log.debug(url +" -> " + SiteCrawlerPerHost(url));
            superVisor.tell(new Message("start",url), ActorRef.noSender());
        }
        //log.debug(actorPerHost.keySet().toString());
        //log.debug(actorPerHost.values().toString());
    }
}
