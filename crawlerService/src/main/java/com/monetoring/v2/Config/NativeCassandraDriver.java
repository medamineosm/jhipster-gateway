package com.monetoring.v2.Config;

import com.datastax.driver.core.*;
import com.monetoring.v2.Model.DataUrl;

import java.util.concurrent.ExecutionException;

/**
 * Created by Ouasmine on 25/07/2017.
 */
public class NativeCassandraDriver {
    private static NativeCassandraDriver driver;

    private Cluster cluster;
    private Session session;
    private PreparedStatement preparedStatement;
    private boolean flag = false;

    private Cluster getConnection(){
        //if(cluster != null)
          //  return cluster;
        PoolingOptions poolingOptions = new PoolingOptions();
        // customize options...
        poolingOptions
                .setMaxRequestsPerConnection(HostDistance.LOCAL, 32768)
                .setIdleTimeoutSeconds(100000)
                .setMaxQueueSize(100000)
                .setMaxRequestsPerConnection(HostDistance.REMOTE, 2000);

        Cluster cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .withPoolingOptions(poolingOptions)
                .build();
        return cluster;
    }

    private Session getSession(){
        //if(session != null)
          //  return session;
        return session = getConnection().newSession();
    }

    private PreparedStatement getStatementForPage(){
        if(preparedStatement != null)
            return preparedStatement;

            return preparedStatement = getSession().prepare(
                "insert into crawl.page (url, host, htmlcontent, scraped_at, statuscode, title) values (?, ?, ?, ?, ?, ?)");
    }

    public void save(DataUrl data){
        BoundStatement bound = getStatementForPage()
                .bind(data.getUrl(), data.getHost(), data.getHtmlContent(), data.getScrapedAt(), data.getStatusCode(), data.getTitle());
        session.execute(bound);
    }

    public ResultSetFuture saveAsync(DataUrl data){
        BoundStatement bound = getStatementForPage()
                .bind(data.getUrl(), data.getHost(), data.getHtmlContent(), data.getScrapedAt(), data.getStatusCode(), data.getTitle());
        bound.setConsistencyLevel(ConsistencyLevel.ALL);
        return session.executeAsync(bound);
    }

    public static NativeCassandraDriver getNativeCassandraDriver(){
        if(driver != null){
            return driver;
        }else{
            return driver = new NativeCassandraDriver();
        }
    }
}
