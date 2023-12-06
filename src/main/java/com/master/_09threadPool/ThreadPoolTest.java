package com.master._09threadPool;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 线程池测试
 * @date 2023-12-06 9:00
 */
public class ThreadPoolTest {
    public static void main(String[] args) {
        ThreadPoolExecutor executor=new ThreadPoolExecutor(10,20,1000L, TimeUnit.SECONDS, (BlockingQueue<Runnable>) new ArrayList<Runnable>());
    }
}
