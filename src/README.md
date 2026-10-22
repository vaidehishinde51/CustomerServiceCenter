
# Customer Service Center - Round Robin Scheduling

## Description
A Java Swing application that simulates Round Robin scheduling
for customer service queries. It demonstrates CPU scheduling
concepts from Operating Systems.

## Requirements
- JDK 17 or later
- Any Java IDE, such as IntelliJ IDEA, Eclipse, or VS Code
- No external dependencies

## How to Run

Open a terminal in the CustomerServiceCenter project directory.

Compile:

```bash
javac -d out src/main/java/com/customerservice/*.java
```

Run:

```bash
java -cp out com.customerservice.Main
```

## Features
- Add and remove service agents
- Add and remove customer queries
- Configure the Round Robin time quantum
- View the scheduling execution log
- View waiting, turnaround, and response times
- Compare service workloads across agents

## Scheduling Algorithm
1. Insert all customer queries into a FIFO ready queue.
2. Remove the first query from the queue.
3. Execute it for min(time quantum, remaining service time).
4. If unfinished, return it to the end of the queue.
5. If completed, record its completion time.
6. Repeat until the ready queue is empty.

## OS Concepts
- CPU scheduling and preemption
- Ready queue and FIFO data structure
- Burst time and remaining time
- Waiting, turnaround, and response times
- Scheduling fairness
- Dispatch transitions and simulated execution time

## Assumptions
All queries arrive at time zero, service times are measured
in abstract units, and context-switch overhead is excluded.
Agents are assigned cyclically across scheduling turns.
