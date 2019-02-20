package com.xs.action.bean;

public class First {

    private String name;

    public First(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "First{" +
                "name='" + name + '\'' +
                '}';
    }
}
