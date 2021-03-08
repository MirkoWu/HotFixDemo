package com.mirkowu.hotfixdemo.hotfix;

public class HotFixSdk {

    private HotFixSdk(){

    }
    private static final class Inner{
        private static final HotFixSdk INSTANCE=new HotFixSdk();
    }

    public static HotFixSdk getInstance(){
        return Inner.INSTANCE;
    }

    public void init(){

    }

}
