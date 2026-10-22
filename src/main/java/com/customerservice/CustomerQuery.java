package com.customerservice;

public class CustomerQuery {
    private final int id;
    private final String customerName;
    private final String description;
    private final int arrivalTime;
    private final int burstTime;

    private int remainingTime;
    private int completionTime = -1;
    private int firstResponseTime = -1;

    /** Backward-compatible constructor: query arrives at time zero. */
    public CustomerQuery(int id, String customerName,
                         String description, int burstTime) {
        this(id, customerName, description, 0, burstTime);
    }

    public CustomerQuery(int id, String customerName, String description,
                         int arrivalTime, int burstTime) {
        if (arrivalTime < 0) {
            throw new IllegalArgumentException(
                "Arrival time cannot be negative.");
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException(
                "Burst time must be greater than zero.");
        }

        this.id = id;
        this.customerName = customerName;
        this.description = description;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }

    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getDescription() { return description; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getCompletionTime() { return completionTime; }
    public int getFirstResponseTime() { return firstResponseTime; }

    public void recordFirstResponse(int time) {
        if (firstResponseTime == -1) {
            firstResponseTime = time;
        }
    }

    public void execute(int timeSlice) {
        remainingTime -= Math.min(remainingTime, timeSlice);
    }

    public boolean isCompleted() {
        return remainingTime == 0;
    }

    public void completeAt(int time) {
        completionTime = time;
    }

    public int getTurnaroundTime() {
        return completionTime - arrivalTime;
    }

    public int getWaitingTime() {
        return getTurnaroundTime() - burstTime;
    }

    public int getResponseTime() {
        return firstResponseTime - arrivalTime;
    }
}
