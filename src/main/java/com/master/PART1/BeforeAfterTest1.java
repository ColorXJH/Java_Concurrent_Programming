package com.master.PART1;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: before  method after
 * @date 2024-02-04 11:40
 */
public class BeforeAfterTest1 {
    public static void main(String[] args) {
        MyAdapter adapter=new MyAdapter(new ActionImpl());
        adapter.show();
    }
}


interface Act{
    void show();
    void beforeAfter();
}

class ActionImpl implements Act{
    @Override
    public void show() {
        System.out.println("我喜欢 唱 跳 rap 打篮球！");
    }

    @Override
    public void beforeAfter() {
        System.out.println("开始结束检查");
    }

}

class MyAdapter {
    private Act act;
    MyAdapter(Act act){
        this.act=act;
    }
    void show(){
        act.beforeAfter();
        try {
            act.show();
        } finally {
            act.beforeAfter();
        }
    }
}