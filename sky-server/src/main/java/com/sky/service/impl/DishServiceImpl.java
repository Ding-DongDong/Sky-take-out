package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应口味
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //向菜品表插入一条数据并没有口味，所以不需要DTO
        dishMapper.insert(dish);
        //获取insert语句生成的主键值
        Long dishId=dish.getId();
        //向口味表插入N条数据，一道菜的口味可能有很多
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size()>0){
            //为菜品的dishId赋值，防止后面插入空值
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //在if里面插入多条数据
            //sql支持批量插入
            dishFlavorMapper.insertBatch(flavors);

        }
    }
}
