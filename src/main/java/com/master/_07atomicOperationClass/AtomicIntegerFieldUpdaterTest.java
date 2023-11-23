package com.master._07atomicOperationClass;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @ClassName: AtomicIntegerFieldUpdaterTest
 * @Package: com.master._07atomicOperationClass
 * @Description: 原子更新字段类示例
 * @Datetime: 2023/11/23 21:12
 * @author: ColorXJH
 */
public class AtomicIntegerFieldUpdaterTest {
    //创建原子更新器，并设置需要更新的对象类和对象属性
    private static AtomicIntegerFieldUpdater<User> atfu=AtomicIntegerFieldUpdater.newUpdater(User.class,"old");

    public static void main(String[] args) {
        //设置年龄是10岁
        User color=new User("Color",10);
        //年龄长了一岁，但是仍任输出旧的年龄
        System.out.println(atfu.getAndIncrement(color));
        //输出color现有的年龄
        System.out.println(atfu.get(color));
    }


    static class User{
        private String name;
        //更新类的字段（属性）必须使用public volatile修饰符
        public volatile int old;
        public User(String name,int old){
            this.name=name;
            this.old=old;
        }

        public String getName() {
            return name;
        }

        public int getOld() {
            return old;
        }
    }
}

