package com.example.neetx.controllertest;

import android.app.Application;

public class MyApplication extends Application {
    private boolean finished;
    private int chance;
    private long delay;
    private byte[] key = new byte[32];
    private byte[] iv = new byte[12];


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

    public void setKey(byte[] key){ this.key = key; }

    public byte[] getKey (){ return this.key; }

    public void setIv(byte[] iv){ this.iv = iv; }

    public byte[] getIv(){ return this.iv; }
}
