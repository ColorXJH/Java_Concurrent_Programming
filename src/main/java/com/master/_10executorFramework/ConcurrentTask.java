package com.master._10executorFramework;

import java.util.concurrent.*;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2023-12-06 11:13
 */
public class ConcurrentTask {
    private final ConcurrentMap<Object, Future<String>> taskCache = new ConcurrentHashMap<Object, Future<String>>();
    private String executeTask(final String taskName){
        while (true){
            Future<String>future=taskCache.get(taskName);
            if(future==null){
                Callable<String>task=new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return taskName;
                    }
                };
                FutureTask<String>futureTask=new FutureTask<>(task);
                future=taskCache.putIfAbsent(taskName,futureTask);
                if(future==null){
                    future=futureTask;
                    futureTask.run();
                }
            }
            try {
                return future.get();
            } catch (ExecutionException | InterruptedException e) {
                taskCache.remove(taskName, future);
            }
        }
    }
}
