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
- `/emeraldbank pay <player> <count>` Pay emeralds to other player.
- `/emeraldbank reload` Reload config and messages
- `/emeraldbank wallet add <count>` Add emeralds to your wallet.
- `/emeraldbank wallet add all` Add all emeralds to your wallet.
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

## Configuration

```yaml
#  ______                          _     _ ____              _    
# |  ____|                        | |   | |  _ \            | |   
# | |__   _ __ ___   ___ _ __ __ _| | __| | |_) | __ _ _ __ | | __
# |  __| | '_ ` _ \ / _ | '__/ _` | |/ _` |  _ < / _` | '_ \| |/ /
# | |____| | | | | |  __| | | (_| | | (_| | |_) | (_| | | | |   < 
# |______|_| |_| |_|\___|_|  \__,_|_|\__,_|____/ \__,_|_| |_|_|\_\
#                                                                 
#                                                                 

# DO NOT CHANGE THIS VALUE!
config_version: 0

# The cost for creating a bank.
pay-cost: disabled

#################################################################
##                                                              #
##  Banking                                                     #
##                                                              #
#################################################################
banking:
  # Enable or disable banking.
  enabled: true
  tax:
    # The cost for creating a bank.
    create-cost: disabled

    # The cost for creating a bank.
    add-member-cost: disabled

    # The cost for removing a member from a bank.
    transfer-bank-cost: disabled

    # The cost for sending from a bank to another bank.
    deposit-cost: disabled

    # The cost for withdrawing from a bank.
    withdraw-cost: disabled

    # The cost for paying from a bank to a player.
    pay-cost: disabled

# The default destination for payments.
# INVENTORY: The player's inventory.
# WALLET: The player's wallet.
default-destination: INVENTORY

#################################################################
##                                                              #
##  Database                                                     #
##                                                              #
#################################################################
database:
  # The type of database to use.
  # Change of database type will require a restart of the server.
  # H2: H2 Database
  # MYSQL: MySQL Database
  type: H2

  # The host of the database.
  host: localhost

  # The port of the database.
  port: 3306

  # The name of the database.
  database: emeraldbank

  # The username of the database.
  username: root

  # The password of the database.
  password: password

#################################################################
##                                                              #
##  Messages                                                    #
##                                                              #
#################################################################
messages:
  name: EmeraldBank
  reload: <green>Reloaded configuration and messages
  economy-format: <value> <green><currency></green>
  currency-name: Emerald
  currency-name-plural: Emeralds
  balance: '<green><player>''s Balance: <value> (Wallet: <wallet>)'
  balance-bank: '<green><bank>''s Balance: <value>'
  leaderboard: '<player>: <balance>'
  pay-success: <green>Successfully paid <value> to <player>
  error-pay: <red>Failed to pay <value> to <player>
  error-pay-self: <red>You cannot pay yourself!
  banking-create: <green>Created bank <bank>
  banking-delete: <green>Deleted bank <bank>
  banking-deposit: <green>Deposited <value> to <bank>
  banking-withdraw: <green>Withdrew <value> from <bank>
  banking-add-member: <green>Added <player> to bank <bank>
  banking-remove-member: <green>Removed <player> from bank <bank>
  banking-list: '<green>Banks: <banks>'
  banking-members: '<green>Members of <bank>: <members>'
  banking-add-owner: <green>Added <player> as owner of bank <bank>!
  banking-remove-owner: <green>Removed <player> as owner of bank <bank>!
  banking-send: <green>Sent <value> from <bank_from> to <bank_to>
  banking-pay: <green>Paid <value> from bank <bank> to <player>
  offline-transaction: <green><value> has been moved to your balance while you were offline!
  wallet: '<green><player>''s Wallet: <value>'
  add-wallet: <green>Added <value> to <player>'s wallet!
  withdraw-wallet: <green>Withdrew <value> from <player>'s wallet!
  received: <green>Received <value> from <source>
  receive-bank: <green>Received <value> to bank <destination> from <source>
  error-banking-disabled: <red>Banking is disabled!
  error-banking-create: <red>Failed to create bank <bank>!
  error-banking-delete: <red>Failed to delete bank <bank>!
  error-banking-exists: <red>Bank <bank> already exists!
  error-banking-no-bank: <red>Bank <bank> does not exist!
  error-banking-deposit: <red>Failed to deposit <value> to <bank>
  error-banking-withdraw: <red>Failed to withdraw <value> from <bank>
  error-banking-not-owner: <red><player> is not the owner of bank <bank>!
  error-banking-not-member: <red><player> is not a member of bank <bank>!
  error-banking-member-exists: <red><player> is already a member of bank <bank>!
  error-banking-add-member: <red>Failed to add <player> to bank <bank>!
  error-banking-remove-member: <red>Failed to remove <player> from bank <bank>!
  error-banking-delete-remaining: <red>Bank <bank> has a remaining balance of <value>!
  error-banking-send: <red>Failed to send <value> from bank <bank_from> to bank <bank_to>!
  error-banking-pay: <red>Failed to pay <value> from bank <bank> to <player>!
  error-banking-remove-owner: <red><player> is the owner of bank <bank>!
  error-banking-create-cost: <red>Failed to create bank due to insufficient funds <cost>!
  error-banking-add-member-cost: <red>Failed to add member <player> to bank <bank> due to insufficient funds <cost>!
  error-banking-transfer-cost: <red>Failed to transfer bank <bank> to <player> due to insufficient funds <cost>!
  error-banking-deposit-cost: <red>Failed to deposit <value> to bank <bank> due to insufficient funds <cost>!
  error-banking-withdraw-cost: <red>Failed to withdraw <value> from bank <bank> due to insufficient funds <cost>!
  error-banking-pay-cost: <red>Failed to pay <value> from bank <bank> to <player> due to insufficient funds <cost>!
  error-pay-cost: <red>Failed to pay <value> to <player> due to insufficient funds <cost>!
  error-add-wallet: <red>Failed to add <value> to <player>'s wallet!
  error-withdraw-wallet: <red>Failed to withdraw <value> from <player>'s wallet!
  error-reload: <red>Failed to reload configuration and messages!
  error-banking-remove-last-owner: <red>Failed to remove <player> as owner of bank <bank>!
  error-banking-remove-last-member: <red>Failed to remove <player> as member of bank <bank>!
  error-player-not-found: <red>Player <player> not found!

```
