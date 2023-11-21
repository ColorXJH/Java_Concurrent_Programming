package com.master._06concurrentContainerAndFramework;

import org.junit.Test;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2023-11-21 16:54
 */
public class ConcurrentHashMapTest {
    @Test
    public void test() {
        //如果不进行再散列，散列冲突会非
        //常严重，因为只要低位一样，无论高位是什么数，其散列值总是一样
        System.out.println(Integer.parseInt("0001111", 2) & 15);
        System.out.println(Integer.parseInt("0011111", 2) & 15);
        System.out.println(Integer.parseInt("0111111", 2) & 15);
        System.out.println(Integer.parseInt("1111111", 2) & 15);
        System.out.println(Integer.toBinaryString(15));
    }
}
