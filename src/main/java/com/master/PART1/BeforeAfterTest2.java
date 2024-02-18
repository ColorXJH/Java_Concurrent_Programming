package com.master.PART1;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: before method after子类化
 * @date 2024-02-04 11:52
 */
public class BeforeAfterTest2 {
    public static void main(String[] args) {
        SubAction action=new SubAction();
        action.show();
    }
}


class Action1{
    void show(){
        System.out.println("你好");
    }
}

class SubAction extends Action1{
    void beforeAfter(){
        System.out.println("检查一下");
    }

    @Override
    void show() {
        beforeAfter();
        super.show();
        beforeAfter();
    }
}