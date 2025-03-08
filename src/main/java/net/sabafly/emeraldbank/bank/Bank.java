package net.sabafly.emeraldbank.bank;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class Bank {

    @Getter
    private final String name;
    @Getter
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

}
