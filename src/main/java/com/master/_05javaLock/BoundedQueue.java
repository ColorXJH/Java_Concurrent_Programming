package com.master._05javaLock;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 通过有界队列示例来掩饰Condition的使用方法
 * @date 2023-11-20 16:01
 */
public class BoundedQueue<T> {
    private Object[] items;
    //添加的下标，删除的下标和数组当前的数量
    private int addIndex,removeIndex,count;
}
