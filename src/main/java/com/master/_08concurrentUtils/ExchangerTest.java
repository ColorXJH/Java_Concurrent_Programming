package com.master._08concurrentUtils;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @ClassName: ExchangerTest
 * @Package: com.master._08concurrentUtils
 * @Description: 线程间的数据交换
 * @Datetime: 2023/11/25 17:22
 * @author: ColorXJH
 */
public class ExchangerTest {
    private static final Exchanger<String>exchange=new Exchanger<>();
    private static ExecutorService service= Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        service.execute(()->{
            String A="银行流水A";
            try {
                exchange.exchange(A);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        service.execute(()->{
            String B="银行流水B";
            try {
                String exchange1 = exchange.exchange(B);
                System.out.println("AB的数据是否一致:"+exchange1.equals(B)+"; A录入的数据是："+exchange1+", B录入的数据是："+B);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        service.shutdown();
    }
}

/**
 * 如果两个线程有一个没有执行exchange()方法，则会一直等待，如果担心有特殊情况发
 * 生，避免一直等待，可以使用exchange（V x，longtimeout，TimeUnit unit）设置最大等待时长
 *
 */