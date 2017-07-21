package com.monetoring.v2.Dao;

import com.monetoring.v2.Model.DataUrl;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Ouasmine on 21/07/2017.
 */
public interface DataUrlDao extends MongoRepository<DataUrl, String>{
}
