package com.master.PART2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 一个简化的伪代码示例，展示了 EventMulticaster 的设计和基本实现：
 * @date 2024-04-07 15:03
 */
class EventMulticaster implements SomeEventListener {
    // 内部维护的监听器链表
    private List<SomeEventListener> listeners;

    // 构造函数初始化监听器链表
    public EventMulticaster() {
        this.listeners = new ArrayList<>();
    }

    // 添加监听器到链表中
    public void addListener(SomeEventListener listener) {
        listeners.add(listener);
    }

    // 删除监听器
    public void removeListener(SomeEventListener listener) {
        listeners.remove(listener);
    }

    // 事件处理方法
    @Override
    public void onEvent(Event event) {
        // 依次调用链表中每个监听器的处理方法
        for (SomeEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    // 其他方法...
}

// 示例的事件监听器接口
interface SomeEventListener {
    void onEvent(Event event);
}

class SomeEventListenerImpl1 implements SomeEventListener{

    @Override
    public void onEvent(Event event) {

    }
}
class SomeEventListenerImpl2 implements SomeEventListener{

    @Override
    public void onEvent(Event event) {

    }
}
// 示例事件类
class Event {
    // 事件数据
}

// 使用示例
public class Example {
    public static void main(String[] args) {
        EventMulticaster multicaster = new EventMulticaster();
        SomeEventListener listener1 = new SomeEventListenerImpl1();
        SomeEventListener listener2 = new SomeEventListenerImpl2();

        // 添加监听器到 EventMulticaster
        multicaster.addListener(listener1);
        multicaster.addListener(listener2);

        // 模拟事件发生，调用 EventMulticaster 的事件处理方法
        Event event = new Event();
        multicaster.onEvent(event);
    }
}

