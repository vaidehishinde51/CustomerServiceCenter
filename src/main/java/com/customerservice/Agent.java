package com.customerservice;

public class Agent {
    private final int id;
    private final String name;
    private int totalServiceTime;
    private int queriesHandled;

    public Agent(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotalServiceTime() {
        return totalServiceTime;
    }

    public int getQueriesHandled() {
        return queriesHandled;
    }

    public void recordService(int duration) {
        totalServiceTime += duration;
        queriesHandled++;
    }
}