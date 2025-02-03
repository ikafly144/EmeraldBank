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
- `/wallet` Alias of `/emeraldbank wallet`
- `/em` Alias of `/emeraldbank`
- `/embank` Alias of `/emeraldbank`
- `/emeraldbank balance` Show your balance that count of emeralds you have.
- `/emeraldbank balance <player>` Show other player's balance.
- `/emeraldbank leaderboard` Show all player's balance in leaderboard.
- `/emeraldbank pay <player> <count>` Pay emeralds to other online player.
- `/emeraldbank reload` Reload config and messages
- `/emeraldbank wallet add <count>` Add emeralds to your wallet.
- `/emeraldbank wallet withdraw <count>` Withdraw emeralds from your wallet.
- `/emeraldbank wallet balance` Show your wallet balance.
- `/emeraldbank bank account create <name>` Create a new bank account.
- `/emeraldbank bank account delete <bank>` Close the specified bank account.
- `/emeraldbank bank account add <bank> <player>` Add a member that can access the bank account.
- `/emeraldbank bank account remove <bank> <player>` Remove a member from the bank account.
- `/emeraldbank bank account transfer <bank> <player>` Change the bank owner to other player.
- `/emeraldbank bank account list` Show all bank accounts.
- `/emeraldbank bank account list <bank>` Show all members of bank.
- `/emeraldbank bank balance <bank>` Show bank balance.
- `/emeraldbank bank deposit <bank> <count>` Deposit to the bank account.
- `/emeraldbank bank withdraw <bank> <count>` Withdraw to the bank account.
- `/emeraldbank bank send <bank> <target> <count>` Transfer emeralds to other bank account.
- `/emeraldbank bank pay <bank> <player> <count>` Pay emeralds to specified player.

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

## Placeholders

- `%emeraldbank_balance%`
- `%emeraldbank_balance_<player>%`
- `%emeraldbank_wallet%`
- `%emeraldbank_wallet_<player>%`
- `%emeraldbank_bank_balance_<bank>%`
- `%emeraldbank_bank_owner_<bank>%`
- `%emeraldbank_bank_members_<bank>%`
- `%emeraldbank_bank_list%`
