package com.xs.action.bean;

public class Third {

    private boolean flag;

    public Third(boolean flag) {
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "Third{" +
                "flag=" + flag +
                '}';
    }
}
