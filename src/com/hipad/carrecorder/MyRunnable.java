package com.hipad.carrecorder;

public abstract interface MyRunnable extends Runnable {


    public void setSuspend(boolean susp);

    public boolean isSuspend();

    public void runPesonelLogic();
}
