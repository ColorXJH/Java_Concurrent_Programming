package com.master._01threadChallenge;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 多线程一定快吗？
 * @date 2022/12/5 14:23
 */
public class ConcurrencyTest {
    private static final long count= 10000L;

    public static void main(String[] args) throws InterruptedException {
        concurrency();
        serial();
    }


    private static void concurrency() throws InterruptedException {
        long start=System.currentTimeMillis();
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                int a=0;
                for(long i=0;i<count;i++){
                    a+=5;
                }
            }
        });
        thread.start();
        int b=0;
        for(long i=0;i<count;i++){
            b--;
        }
        long time=System.currentTimeMillis()-start;
        thread.join();//等待线程返回到主线程继续执行
        System.out.println("concurrency:"+time+"ms,b="+b);
    }

    private static void serial(){
        long start=System.currentTimeMillis();
        int a=0;
        for(long i=0;i<count;i++){
            a+=5;
        }
        int b=0;
        for(long i=0;i<count;i++){
            b--;
        }
        long time=System.currentTimeMillis()-start;
        System.out.println("serial:"+time+"ms,b="+b);
    }
}
