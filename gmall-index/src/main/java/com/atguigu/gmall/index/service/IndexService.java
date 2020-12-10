package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

public interface IndexService {
    List<CategoryEntity> queryLvl1Categories();

    List<CategoryEntity> queryLvl2CategoriesWithSub(Long pid);

    void testlock();

    void testlock1();

    public List<CategoryEntity> queryLvl2CategoriesWithSub1(Long pid);

    public void testWrite();

    public void testRead();

    public void latch() throws InterruptedException;

    public void countdown();

    public List<CategoryEntity> queryLvl2CategoriesWithSub2(Long pid);
}
