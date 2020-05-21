package com.imooc.service.impl;

import com.imooc.mapper.StuMapper;
import com.imooc.pojo.Stu;
import com.imooc.service.StuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service // service 需要被容器扫描到，所以要加service
public class StuServiceImpl implements StuService {

    @Autowired // 注入进来
    private StuMapper stuMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Stu getStuInfo(int id) {
        return stuMapper.selectByPrimaryKey(id);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveStu() {
        Stu stu = new Stu();
        stu.setName("javk");
        stu.setAge(19);
        stuMapper.insert(stu);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateStu(int id) {
        Stu stu = new Stu();
        stu.setId(id);
        stu.setName("lucy");
        stu.setAge(20);
        stuMapper.updateByPrimaryKey(stu);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteStu(int id) {
        stuMapper.deleteByPrimaryKey(id);
    }

    public void saveParent() {
        Stu stu = new Stu();
        stu.setName("parent");
        stu.setAge(19);
        stuMapper.insert(stu);
    }
    // 测试不加事务会出什么问题

//    @Transactional(propagation = Propagation.SUPPORTS)
     @Transactional(propagation = Propagation.REQUIRED)
    public void saveChildren() {
        saveChild1();
        int a = 1 / 0;// 这里很明显报错。测试不加事务出错后数据库是怎么样的
        // Propagation.REQUIRED：
        // 测试一：本方法和调用此方法的父方法都没有开启事务：
        // 数据库保存了parent 和 child-1
        // 测试二：开启本方法的事务：
        // 数据库只保存了 parent，说明了事务会作用与当前的方法，无论哪条数据出现错误，整个方法将复原
        // 即恢复到执行前，就想这个方法没有执行过一样。
        // propagation = Propagation.SUPPORTS：
        // 如果调用此方法的父方法没有使用事务，则不使用事务，跟着父方法走的
        // 如果调用此方法的父方法使用了事务，则使用事务，跟着父方法走的
        saveChild2();
    }

    public void saveChild1() {
        Stu stu1 = new Stu();
        stu1.setName("child-1");
        stu1.setAge(11);
        stuMapper.insert(stu1);
    }

    public void saveChild2() {
        Stu stu2 = new Stu();
        stu2.setName("child-2");
        stu2.setAge(11);
        stuMapper.insert(stu2);
    }
}
