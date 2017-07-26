package com.monetoring.v2.Actors;

/**
 * Created by Ouasmine on 21/07/2017.
 */
public interface ActorTemplate {
    String name();
    void shutdown();
    void forceShutDown();
}
