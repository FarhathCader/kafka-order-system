package com.example.kafka;

public class RunningAverage {
    private double total;
    private long count;

    public RunningAverage() {
    }

    public RunningAverage(double total, long count) {
        this.total = total;
        this.count = count;
    }

    public RunningAverage add(double price) {
        this.total += price;
        this.count += 1;
        return this;
    }

    public double average() {
        if (count == 0) {
            return 0.0;
        }
        return total / count;
    }

    public double getTotal() {
        return total;
    }

    public long getCount() {
        return count;
    }
}
