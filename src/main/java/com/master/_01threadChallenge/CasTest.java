package com.master._01threadChallenge;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:cas 原子操作的实现原理
 * @date 2022/12/6 10:22
 */
public class CasTest {
    private AtomicInteger atomicInteger=new AtomicInteger(0);
    private AtomicInteger atomicInteger2=new AtomicInteger(0);
    private int i=0;
    public static void main(String[] args) throws InterruptedException {
        final CasTest cas=new CasTest();
        List<Thread> ts=new ArrayList<>(600);
        long start=System.currentTimeMillis();
        for(int j=0;j<100;j++){
            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i=0;i<10000;i++){
                        cas.count();
                        cas.safeCount();
                        cas.safeCount2();
                    }
                }
            });
            ts.add(t);
        }
        for(Thread t:ts){
            t.start();
        }
        for(Thread t:ts){
            t.join();
        }
        System.out.println(cas.i);
        System.out.println(cas.atomicInteger.get());
        System.out.println(cas.atomicInteger2.get());
        System.out.println(System.currentTimeMillis()-start);
    }
    /**
     * Description: cas实现线程安全计数器
     * @Author: ColorXJH
     * @Date: 2022/12/6 10:25
     * @param
     * @Return: void
     **/
    private void safeCount(){
        for(;;){
            int i=atomicInteger.get();
            boolean suc=atomicInteger.compareAndSet(i,++i);
            if(suc){
                break;
            }
        }
    }

    private void safeCount2(){
        atomicInteger2.incrementAndGet();
    }
    /**
     * Description: 非线程安全计数器
     * @Author: ColorXJH
     * @Date: 2022/12/6 10:26
     * @param
     * @Return: void
     **/
    private void count(){
        i++;
    }
}
