package com.imooc.service.impl;

import com.imooc.service.StuService;
import com.imooc.service.TestTransService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestTransServiceImpl implements TestTransService {

    @Autowired
    private StuService stuService;

    /**
     * 事务传播 - Propagation
     *      REQUIRED（常用）：使用当前的事务，如果当前没有事务，则自己新建一个事务，子方法是必须运行在一个事务中的；
     *              如果当前存在事务，则加入这个事务，成为一个整体。
     *      SUPPORTS（常用）：如果当前有事务，则使用事务；如果当前没有事务，则不是用事务。
     *      MANDATORY：强制必须存在一个事务，如果不存在，则抛出异常
     *      REQUIRES_NEW：如果当前有事务，则挂起该事务，并且自己创建一个新的事务给自己使用；
     *              即开启一个不同的事务（与NESTED的区别就在于此）。各个事务互不影响
     *              如果没有事务，则创建事务；
     *              执行该方法的父方法，在调用此方法之后出现错误，则父方法回滚，此方法不受影响
     *      NOT_SUPPORTED：如果当前有事务，则把事务挂起，执行该方法的父方法，在调用此方法之后出现错误，则父方法回滚，此方法不受影响
     *      NEVER：不允许有事务，发现有事务，则抛出异常
     *      NESTED：如果当前有事务，则嵌套开启子事务。调用此方法的父方法出现错误，则父方法回滚，当前方法不受影响继续执行；
     *              如果此方法出错，则此方法回滚，父方也回滚。
     *              如果在父方法对此方法进行try...catch...，则父方法不会受此方法的影响
     */

//     @Transactional(propagation = Propagation.REQUIRED)
    // 开启这里的事务后 saveChildren() 方法出现错误，则 saveParent() 会回滚执行前的状态
    // 因为这个事务是针对当前方法，会作用到此方法里面的子方法。也会传递到方法里面的子方法，会传递的
    public void testPropagationTrans() {
        stuService.saveParent();

        stuService.saveChildren();
    }
}
