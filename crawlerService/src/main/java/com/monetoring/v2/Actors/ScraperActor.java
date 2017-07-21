package com.monetoring.v2.Actors;

import akka.actor.UntypedActor;
import com.monetoring.v2.Actors.Builder.ActorBuilder;
import com.monetoring.v2.Actors.Messages.Message;
import com.monetoring.v2.Model.DataUrl;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Named("ScraperActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ScraperActor extends UntypedActor implements ActorTemplate {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ScraperActor(){
        log.info(name() + " constructor with path : " + this.getSelf().path());
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
                    log.info("Scraping ... : " + ((Message) message).getObject());
                    actorBuilder.getIndexer().tell(new Message( "index", parse((String) ((Message) message).getObject())),self());
                    this.getContext().parent().tell(new Message(), this.self());
                    break;
            }
        }else{
            unhandled(message);
            log.error("Unhandled Message : " + ((Message)message).getMsg());
        }
    }

    private DataUrl parse(String url){
        DataUrl data = null;
        try {
            Connection.Response repsonse = Jsoup.connect(url).ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1").execute();
            String contentType = repsonse.contentType();
            if (contentType.startsWith("text/html")){
                data = new DataUrl();
                Document doc = repsonse.parse();
                data.setTitle(doc.title());
                data.setHost(new URL(url).getHost());
                data.setHtmlContent(doc.html());
                data.setLinks(parseLinks(doc));
                log.info(data.toString());
            }else{
                log.warn(url + " is not a html page !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            return data;
        }
    }

    private Set<String> parseLinks(Document doc){
        Set<String> links = new HashSet<>();
        for(Element e : doc.getElementsByTag("a")){
            if(e.hasAttr("href") && !e.attr("href").isEmpty()){
               if(isValide(e.attr("href")))
                   links.add(e.attr("href"));
            }
        }
        return links;
    }

    private boolean isValide(String url){
        if(!url.endsWith("xml"))
            return true;
        else
            return false;
    }
}
