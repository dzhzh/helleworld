package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存基本信息
        this.save(setmealDto);
        //获取分类id
        Long setmealId = setmealDto.getId();
        //获取添加的所有菜品的集合
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        //给每个菜品赋值分类id
        setmealDishes.stream().forEach( item -> {
            item.setSetmealId(setmealId);
        });

        //批量保存
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public void removeWithDish(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,"1");

        int count = this.count(queryWrapper);
        if(count > 0){
            throw new CustomException("选中的套餐正在售卖,不能删除");
        }
        this.removeByIds(ids);
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.in(SetmealDish::getSetmealId,ids);
        //删除关系表中数据
        setmealDishService.remove(lqw);
    }

}
