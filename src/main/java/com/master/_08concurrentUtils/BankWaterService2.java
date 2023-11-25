package com.master._08concurrentUtils;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @ClassName: BankWaterService2
 * @Package: com.master._08concurrentUtils
 * @Description: 区别CyclicBarrier 使用CountDownLatch实现
 * @Datetime: 2023/11/25 15:58
 * @author: ColorXJH
 */
public class BankWaterService2{
    //创建4个计数点的倒计时门闩
    private static CountDownLatch latch=new CountDownLatch(4);
    //假设只有四个sheet，所以只启动四个线程
    private Executor executor= Executors.newFixedThreadPool(4);
    //保存每个sheet计算出的银流结果
    private ConcurrentHashMap<String,Integer> sheetBankWaterCount=new ConcurrentHashMap<>();
    private void count(){
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("到达门闩"+ finalI);
                    //计算当前sheet的银流数据，计算代码省略
                    sheetBankWaterCount.put(Thread.currentThread().getName(),1);
                    //流水计算完成，插入一个门闩
                    latch.countDown();
                }
            });
        }
        //关闭线程池
        if(executor instanceof ExecutorService){
            ExecutorService service=(ExecutorService)executor;
            service.shutdown();
        }
    }
    public void  afterTest() {
        System.out.println("倒计时门闩结束了---");
        int result=0;
        //汇总每个sheet计算出的结果
        for(Map.Entry<String,Integer> sheet:sheetBankWaterCount.entrySet()){
            result+=sheet.getValue();
        }
        //将结果输出
        sheetBankWaterCount.put("result",result);
        System.out.println(result);
    }

    public static void main(String[] args) throws InterruptedException {
        BankWaterService2 service=new BankWaterService2();
        service.count();
        latch.await();
        service.afterTest();
    }
}
