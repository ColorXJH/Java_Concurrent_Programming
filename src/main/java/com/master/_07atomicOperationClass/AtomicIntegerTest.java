package com.master._07atomicOperationClass;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 原子更新基本类型类
 * @date 2023-11-23 16:32
 */
public class AtomicIntegerTest {
    static AtomicInteger ai=new AtomicInteger(1);
    public static void main(String[] args) {
        System.out.println(ai.getAndIncrement());
        System.out.println(ai.get());
    }
}
