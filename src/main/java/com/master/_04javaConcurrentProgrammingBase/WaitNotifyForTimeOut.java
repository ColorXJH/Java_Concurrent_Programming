package com.master._04javaConcurrentProgrammingBase;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: WaitNotifyForTimeOut
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 等待通知 超时机制
 * @Datetime: 2023/11/12 10:21
 * @author: ColorXJH
 */
public class WaitNotifyForTimeOut {
    private static boolean flag=true;
    private static Object lock=new Object();

    public static void main(String[] args) throws InterruptedException {
        TimeOut time=new TimeOut();
        Thread wait=new Thread(new Wait(time),"WAIT-THREAD");
        Thread notify=new Thread(new Notify(time),"NOTIFY-THREAD");
        //1：线程正常执行时
        wait.start();
        notify.start();
        TimeUnit.SECONDS.sleep(6);

        //2：线程被打断时，注释掉1下方的内容
//        wait.start();
//        TimeUnit.SECONDS.sleep(1);
//        wait.interrupt();
    }

    static class TimeOut{
        public synchronized Object get(long timeout) throws InterruptedException {
                //超时上限时间=当前时间+5秒
                long future=System.currentTimeMillis()+timeout;
                //超时时间段：5秒
                long remaining=timeout;
                //不符合业务逻辑条件+超时时间过了
                while (flag&&remaining>0){
                    //等待5秒，5秒之后依旧自动执行，此时remaining=0，下次执行跳过该循环执行默认值
                    wait(remaining);
                    //如果在5秒内被唤醒，则remaining超时时间段>0,如果条件值flag被改变，也将执行后续操作
                    remaining=future-System.currentTimeMillis();
                }
                System.out.println("不管是提前时间被唤醒，还是超时被唤醒，都会执行这段程序，不超时返回的flag=false，超时的仍然为true");
                return flag;
        }
        public synchronized void notifyThread(){
            flag=false;
            notify();
        }
    }
    static class Wait implements Runnable{
        TimeOut time;
        public Wait(TimeOut time){
            this.time=time;
        }
        @Override
        public void run() {
            try {
                Object o = time.get(5000);
                System.out.println("返回的结果值为： "+o);
            } catch (InterruptedException e) {
                System.out.println("线程被打断了自动唤醒,此时的flag值没有改变：flag="+flag);
                throw new RuntimeException(e);
            }
        }
    }

    static class Notify implements Runnable{
        TimeOut time;
        public Notify(TimeOut time){
            this.time=time;
        }
        @Override
        public void run() {
            try {
                Thread.sleep(7000);
                time.notifyThread();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
