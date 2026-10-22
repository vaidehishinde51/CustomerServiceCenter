package com.customerservice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobinScheduler {

    public SchedulerResult schedule(
            List<CustomerQuery> queries,
            List<Agent> agents,
            int quantum) {

        if (queries == null || queries.isEmpty()) {
            throw new IllegalArgumentException("Add at least one customer query.");
        }
        if (agents == null || agents.isEmpty()) {
            throw new IllegalArgumentException("Add at least one service agent.");
        }
        if (quantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be positive.");
        }

        // Work on a sorted copy so the UI's input order is not changed.
        List<CustomerQuery> pending = new ArrayList<>(queries);
        pending.sort(Comparator.comparingInt(CustomerQuery::getArrivalTime)
                .thenComparingInt(CustomerQuery::getId));

        Queue<CustomerQuery> readyQueue = new ArrayDeque<>();
        List<String[]> log = new ArrayList<>();
        List<CustomerQuery> completed = new LinkedList<>();

        int currentTime = 0;
        int dispatches = 0;
        int agentIndex = 0;
        int nextPending = 0;

        while (completed.size() < pending.size()) {
            // Admit every query that has arrived by the current time.
            while (nextPending < pending.size()
                    && pending.get(nextPending).getArrivalTime() <= currentTime) {
                readyQueue.offer(pending.get(nextPending++));
            }

            // If nobody has arrived yet, advance time to the next arrival.
            if (readyQueue.isEmpty()) {
                currentTime = pending.get(nextPending).getArrivalTime();
                continue;
            }

            CustomerQuery query = readyQueue.poll();
            Agent agent = agents.get(agentIndex);
            agentIndex = (agentIndex + 1) % agents.size();

            query.recordFirstResponse(currentTime);
            int duration = Math.min(quantum, query.getRemainingTime());
            int startTime = currentTime;
            currentTime += duration;

            query.execute(duration);
            agent.recordService(duration);
            dispatches++;

            // New arrivals during this time slice enter the queue before
            // the preempted query is re-queued, as in standard Round Robin.
            while (nextPending < pending.size()
                    && pending.get(nextPending).getArrivalTime() <= currentTime) {
                readyQueue.offer(pending.get(nextPending++));
            }

            String status;
            if (query.isCompleted()) {
                query.completeAt(currentTime);
                completed.add(query);
                status = "COMPLETED";
            } else {
                readyQueue.offer(query);
                status = "PREEMPTED - REQUEUED";
            }

            log.add(new String[] {
                String.valueOf(query.getId()),
                query.getCustomerName(),
                agent.getName(),
                String.valueOf(startTime),
                String.valueOf(currentTime),
                String.valueOf(duration),
                String.valueOf(query.getRemainingTime()),
                status
            });
        }

        double waiting = 0;
        double turnaround = 0;
        double response = 0;
        for (CustomerQuery q : completed) {
            waiting += q.getWaitingTime();
            turnaround += q.getTurnaroundTime();
            response += q.getResponseTime();
        }

        int n = completed.size();
        int contextSwitches = Math.max(0, dispatches - 1);
        return new SchedulerResult(log, completed, agents,
                waiting / n, turnaround / n, response / n,
                contextSwitches, currentTime);
    }
}
