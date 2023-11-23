package com.master._07atomicOperationClass;

import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * @ClassName: AtomicIntegerArrayTest
 * @Package: com.master._07atomicOperationClass
 * @Description: 原子更新数组示例
 * @Datetime: 2023/11/23 20:39
 * @author: ColorXJH
 */
public class AtomicIntegerArrayTest {
    static int[] value=new int[]{1,2};
    static AtomicIntegerArray aia =new AtomicIntegerArray(value);

    public static void main(String[] args) {
        aia.getAndSet(0,3);
        System.out.println(aia.get(0));
        System.out.println(value[0]);
    }
}

/**
 * 需要注意的是，数组value通过构造方法传递进去，然后AtomicIntegerArray会将当前数组
 * 复制一份，所以当AtomicIntegerArray对内部的数组元素进行修改时，不会影响传入的数组。
 *
 */