package com.example.neetx.controllertest;

import android.app.Application;

public class MyApplication extends Application {
    private boolean finished;
    private int chance;
    private long delay;


    public boolean getFinished() {
        return this.finished;
    }

    public void setFinished(boolean var) {
        this.finished = var;
    }

    public int getChance() {

        return this.chance;
    }
    public void setChance(int chance){
        this.chance = chance;
    }

    public long getDelay(){
        return this.delay;
    }

    public void setDelay(long delay){
        this.delay = delay;
    }
}
