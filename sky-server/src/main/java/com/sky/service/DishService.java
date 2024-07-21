package com.sky.service;

import com.sky.dto.DishDTO;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.stereotype.Service;


public interface DishService {
    void saveWithFlavor(DishDTO dishDTO);
}
