# EmeraldBank - A Simple Emerald Economy Plugin

A simple and lightweight economics plugin using Emerald.
All messages can be customized in `messages.yml`.

## Requires

- [Paper](https://papermc.io/) `1.21.x` only supported
- [Vault](https://github.com/MilkBowl/Vault/releases)
- [OpenInv](https://github.com/Jikoo/OpenInv/releases)

## Commands

- `/bank` Alias of `/emeraldbank bank`
- `/balance` Alias of `/emeraldbank balance`
- `/pay` Alias of `/emeraldbank pay`
- `/emeraldbank` There are aliases `/em` and `/embank`.
    - `balance` Show your balance that count of emeralds you have.
    - `balance <player>` Show other player's balance.
    - `leaderboard` Show all player's balance in leaderboard.
    - `pay` Pay emeralds to other online player.
    - `reload` Reload config and messages
    - `bank`
        - `account`
            - `create <name>` Create a new bank account.
            - `delete <bank>` Close the specified bank account.
            - `add <bank> <player>` Add a member that can access the bank account.
            - `remove <bank> <player>` Remove a member from the bank account.
            - `transfer <bank> <player>` Change the bank owner to other player.
            - `list` Show all bank accounts.
            - `list <bank>` Show all members of bank.
        - `balance <bank>` Show bank balance.
        - `deposit <bank> <count>` Deposit to the bank account.
        - `withdraw <bank> <count>` Withdraw to the bank account.
        - `send <bank> <target> <count>` Transfer emeralds to other bank account.
        - `pay <bank> <player> <count>` Pay emeralds to specified player.

## Permissions

- `emeraldbank.admin`
    - `emeraldbank.balance.all`
    - `emeraldbank.reload`
    - `emeraldbank.bypass`
- `emeraldbank.bypass`
    - `emeraldbank.bypass.cost`
    - `emeraldbank.bypass.deposit`
    - `emeraldbank.bypass.member`
    - `emeraldbank.bypass.owner`
- `emeraldbank.default`
    - `emeraldbank.balance`
    - `emeraldbank.pay`
    - `emeraldbank.leaderboard`
    - `emeraldbank.banking`
- `emeraldbank.banking`
    - `emeraldbank.banking.balance`
    - `emeraldbank.banking.deposit`
    - `emeraldbank.banking.withdraw`
    - `emeraldbank.banking.send`
    - `emeraldbank.banking.pay`
    - `emeraldbank.banking.account`
- `emeraldbank.banking.account`
    - `emeraldbank.banking.account.create`
    - `emeraldbank.banking.account.delete`
    - `emeraldbank.banking.account.add`
    - `emeraldbank.banking.account.remove`
    - `emeraldbank.banking.account.list`
    - `emeraldbank.banking.account.transfer`