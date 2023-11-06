package com.master._04javaConcurrentProgrammingBase;

import jdk.nashorn.internal.scripts.JO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: Priority
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 线程的优先级设置
 * @Datetime: 2023/11/6 23:08
 * @author: ColorXJH
 */
public class Priority {
    //成员内部类、静态内部类、局部内部类、匿名内部类都能够访问外部类中的所有方法。
    //成员内部类、局部内部类、匿名内部类都能够访问外部类中的所有属性，静态内部类只能访问外部类中的静态属性。
    //外部类能够访问成员内部类和静态内部类中的所有属性和方法。
    //外部类不能访问局部内部类。
    private static volatile boolean notStart=true;
    private static volatile boolean notEnd=true;

    public static void main(String[] args) throws InterruptedException {
        List<Job>jobs=new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int priority=i<5? Thread.MIN_PRIORITY:Thread.MAX_PRIORITY;
            Job job=new Job(priority);
            jobs.add(job);
            Thread thread=new Thread(job,"Thread:"+i);
            thread.setPriority(priority);
            thread.start();
        }
        notStart=false;
        TimeUnit.SECONDS.sleep(10);
        notEnd=false;
        for (Job job:jobs) {
            System.out.println("job priority:"+job.priority+",Count:"+job.jobCount+",and age is :"+job.age());

        }
        //从输出可以看到，线程优先级没有生效，优先级1和10的数据量相近，没有明显差距，这表明程序的正确性不能依赖线程的优先级
        //线程的优先级不能作为程序正确的依赖，因为操作系统可以完全不用理会java线程对于优先级的设定
    }
    static class Job implements Runnable{
        private int priority;
        private long jobCount;
        public Job(int priority){
            this.priority=priority;
        }
        private int age(){
            return 0;
        }
        @Override
        public void run() {
            while (notStart){
                Thread.yield();
            }
            while (notEnd){
                Thread.yield();
                jobCount++;
            }
        }
    }
}
