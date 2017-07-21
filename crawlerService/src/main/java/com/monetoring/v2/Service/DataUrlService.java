package com.monetoring.v2.Service;

import com.monetoring.v2.Dao.DataUrlDao;
import com.monetoring.v2.Model.DataUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Ouasmine on 21/07/2017.
 */
@Service
public class DataUrlService {

    @Autowired
    private DataUrlDao dataUrlDao;

    public DataUrl save(DataUrl dataUrl){
        return dataUrlDao.save(dataUrl);
    }
}
