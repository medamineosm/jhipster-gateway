package com.monetoring.v2.Web;

import com.monetoring.v2.Service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@RestController
public class CrawlerController {

    @Autowired
    private CrawlerService crawlerService;

    @GetMapping("CrawlByCollection")
    public void crawl(@RequestParam String urls){
        crawlerService.Crawl(Arrays.asList(urls.split(";")));
    }
}
