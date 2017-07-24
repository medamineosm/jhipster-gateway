package com.monetoring.v2.Actors.Builder;

import akka.actor.*;
import com.monetoring.v2.Actors.IndexerActor;
import com.monetoring.v2.Actors.ScraperActor;
import com.monetoring.v2.Dao.DataUrlDao;
import com.monetoring.v2.Service.DataUrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import static com.monetoring.v2.Config.SpringExtension.SpringExtProvider;

/**
 * Created by Ouasmine on 20/07/2017.
 */
@Service
@Scope("singleton")
public class ActorBuilder {
    @Autowired
    private ActorSystem system;
    @Autowired
    private DataUrlService dataUrlService;
    private static ActorRef superVisor;
    private static ActorRef indexer;


    public ActorRef buildActor(String nameOfActorBean, String nameOfActor){
        ActorRef actor = system.actorOf(
                SpringExtProvider.get(system).props(nameOfActorBean), nameOfActor);
        return actor;
    }

    public void terminate(){
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        system.terminate();
    }

    public ActorRef getSuperVisor(){
        if(superVisor != null)
            return superVisor;
        else
            return superVisor = buildActor("SuperVisorActor", "supervisoractor");
    }

    /*
    * public ActorRef getIndexer(){
        if(indexer != null)
            return indexer;
        else
            return indexer = buildActor("IndexerActor", "indexeractor");
    }
    * */

    public ActorRef getIndexer(DataUrlService dao){
        if(indexer != null)
            return indexer;
        else
            return indexer = system.actorOf(Props.create(IndexerActor.class, dao));
    }

    public ActorRef getScrapper(UntypedActor parentActor){
        return parentActor.getContext().actorOf(Props.create(ScraperActor.class, getIndexer(dataUrlService)));
    }

    public ActorSystem getSystem() {
        return system;
    }
}
