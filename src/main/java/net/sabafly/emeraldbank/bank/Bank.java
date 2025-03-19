package net.sabafly.emeraldbank.bank;

import org.jetbrains.annotations.NotNull;

public class Bank {

    private final String name;
    private double balance;

    public Bank(@NotNull String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public double withdraw(double amount) {
        if (balance < amount) {
            return 0;
        }
        balance -= amount;
        return amount;
    }

    public double balance() {
        return balance;
    }

    public String name() {
        return name;
    }
}
