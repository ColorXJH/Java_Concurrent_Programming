package com.master._08concurrentUtils;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @ClassName: BankWaterService
 * @Package: com.master._08concurrentUtils
 * @Description: 计算整个excel的日均银行流水
 * @Datetime: 2023/11/25 15:37
 * @author: ColorXJH
 */
public class BankWaterService implements Runnable{
    //创建4个屏障，处理完之后执行当前类的run方法
    private CyclicBarrier barrier=new CyclicBarrier(4,this);
    //假设只有四个sheet，所以只启动四个线程
    private Executor executor= Executors.newFixedThreadPool(4);
    //保存每个sheet计算出的银流结果
    private ConcurrentHashMap<String,Integer>sheetBankWaterCount=new ConcurrentHashMap<>();
    private void count(){
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("到达屏障之前-"+ finalI);
                    //计算当前sheet的银流数据，计算代码省略
                    sheetBankWaterCount.put(Thread.currentThread().getName(),1);
                    //流水计算完成，插入一个屏障
                    try {
                        barrier.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        //关闭线程池
        if(executor instanceof ExecutorService){
            ExecutorService service=(ExecutorService)executor;
            service.shutdown();
        }
    }
    @Override
    public void run() {
        System.out.println("到达屏障之时--优先要做的事情");
        int result=0;
        //汇总每个sheet计算出的结果
        for(Map.Entry<String,Integer> sheet:sheetBankWaterCount.entrySet()){
            result+=sheet.getValue();
        }
        //将结果输出
        sheetBankWaterCount.put("result",result);
        System.out.println(result);
    }

    public static void main(String[] args) {
        BankWaterService service=new BankWaterService();
        service.count();
    }
}
