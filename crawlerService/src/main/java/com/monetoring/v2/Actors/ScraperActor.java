package com.monetoring.v2.Actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.util.Timeout;
import com.monetoring.v2.Actors.Builder.ActorBuilder;
import com.monetoring.v2.Actors.Messages.Message;
import com.monetoring.v2.Model.DataUrl;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Named("ScraperActor")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ScraperActor extends UntypedActor implements ActorTemplate {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private ActorRef indexer;
    private String reason;

    public ScraperActor(ActorRef indexer){
        this.indexer = indexer;
        log.info(name() + " constructor " + indexer.path());
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
        if(message instanceof Message){
            switch (((Message) message).getMsg()){
                case "scrapUrl":
                    log.info("ScrapperActor Scraping ... : " + ((Message) message).getObject());
                    DataUrl data = parse((String) ((Message) message).getObject());
                    if(data != null){
                        this.indexer.tell(new Message( "index", data), self());
                        this.sender().tell(new Message("scrapFinished", data), self());
                    }else{
                        this.sender().tell(new Message(((Message) message),"scrapFailure", reason), self());
                    }
                    break;
            }
        }else if(message instanceof String && message.equals("die")){
            log.info(this.name() + "  will terminate");
            this.shutdown();
        }else{
            unhandled(message);
            log.error("Unhandled Message : " + ((Message)message).getMsg());
        }
    }

    private DataUrl parse(String url){
        DataUrl data = null;
        try {
            Thread.sleep(3000);
            Connection.Response response = Jsoup.connect(url).ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1").execute();
            String contentType = response.contentType();
            if(response.statusCode() == 200){
                if (contentType.startsWith("text/html")){
                    data = new DataUrl();
                    Document doc = response.parse();
                    data.setUrl(url);
                    data.setTitle(doc.title());
                    data.setHost(new URL(url).getHost());
                    data.setHtmlContent(doc.html());
                    data.setLinks(parseLinks(doc));
                    log.debug(data.toString());
                }else{
                    log.warn(url + " is not a html page !");
                }
            }
        } catch (HttpStatusException e) {
            log.warn("status ["+e.getStatusCode()+"] :" + e.getUrl());
            data.setUrl(e.getUrl());
            data.setStatusCode(e.getStatusCode());
            reason = e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            reason = e.getMessage();
        }finally {
            return data;
        }
    }

    private Set<String> parseLinks(Document doc){
        Set<String> links = new HashSet<>();
        for(Element e : doc.getElementsByTag("a")){
            if(e.hasAttr("href") && !e.attr("href").isEmpty()){
               if(isValide(e.attr("href")))
                   links.add(preProcessUrl(e.attr("href")));
            }
        }
        return links;
    }

    private boolean isValide(String url){
        if(!url.endsWith("xml") &&
                url.startsWith("http"))
            return true;
        else
            return false;
    }

    private String preProcessUrl(String url){
        if (url.contains("?"))
            url = url.split("//?")[0];

        return url;
    }
}
