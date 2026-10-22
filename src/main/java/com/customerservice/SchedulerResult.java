package com.customerservice;

import java.util.List;

public class SchedulerResult {
    public final List<String[]> executionLog;
    public final List<CustomerQuery> completedQueries;
    public final List<Agent> agents;
    public final double averageWaitingTime;
    public final double averageTurnaroundTime;
    public final double averageResponseTime;
    public final int contextSwitches;
    public final int totalTime;

    public SchedulerResult(
            List<String[]> executionLog,
            List<CustomerQuery> completedQueries,
            List<Agent> agents,
            double averageWaitingTime,
            double averageTurnaroundTime,
            double averageResponseTime,
            int contextSwitches,
            int totalTime) {

        this.executionLog = executionLog;
        this.completedQueries = completedQueries;
        this.agents = agents;
        this.averageWaitingTime = averageWaitingTime;
        this.averageTurnaroundTime = averageTurnaroundTime;
        this.averageResponseTime = averageResponseTime;
        this.contextSwitches = contextSwitches;
        this.totalTime = totalTime;
    }
}