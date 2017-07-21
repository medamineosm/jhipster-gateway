package com.monetoring.v2.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Document
public class DataUrl {

    @Id
    private String url;
    private String host;
    private String title;
    private String htmlContent;
    private Set<String> links;
    private Date scrapedAt;

    public DataUrl() {
        this.scrapedAt = new Date();
    }

    public DataUrl(String url, String host, String title, String htmlContent, Set<String> links) {
        this.url = url;
        this.host = host;
        this.title = title;
        this.htmlContent = htmlContent;
        this.links = links;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public Date getScrapedAt() {
        return scrapedAt;
    }

    public void setScrapedAt(Date scrapedAt) {
        this.scrapedAt = scrapedAt;
    }

    public Set<String> getLinks() {
        return links;
    }

    public void setLinks(Set<String> links) {
        this.links = links;
    }

    @Override
    public String toString() {
        return "DataUrl{" +
                "url='" + url + '\'' +
                ", host='" + host + '\'' +
                ", title='" + title + '\'' +
                ", htmlContent='" + htmlContent.length() + '\'' +
                ", links=" + links.size() +
                ", scrapedAt=" + scrapedAt +
                '}';
    }
}
