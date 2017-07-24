package com.monetoring.v2.Service;

import com.monetoring.v2.Model.DataUrl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataUrlServiceTest {

    @Autowired
    private DataUrlService dataUrlService;

    @Test
    public void save() throws Exception {
        dataUrlService.save(new DataUrl
                ( "http://www.1-2-3.fr", "www.1-2-3.fr", "title test", "<html></html>", new HashSet<String>(), 200));
    }

}