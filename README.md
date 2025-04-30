# Banking Management System

## Overview

A robust Java-based Banking Management System demonstrating advanced Object-Oriented Programming principles, secure file handling, and complete banking operations. The system features account management, transaction processing, interest calculation, and administrative functions with full data persistence.

## Features

### Core Banking Operations
- **Account Management**:
  - Secure account creation with authentication
  - Password-protected access
  - Account age tracking
  - Password change functionality

- **Transaction Processing**:
  - Deposit/withdrawal with validation
  - Fund transfers between accounts
  - Complete transaction history
  - Transaction auditing

- **Financial Services**:
  - Interest calculation with multiple strategies
  - Custom interest rate strategies
  - Account balance management

### Administrative Functions
- **System Administration**:
  - Admin dashboard with statistics
  - Account search capabilities
  - View all accounts
  - Monitor transaction averages
  - System backup/restore

### Security Features
- Data serialization for persistence
- Automatic backup on startup/shutdown
- Comprehensive input validation
- Custom exception handling

## Technologies Used

- **Java**: Core application development
- **OOP Principles**: Encapsulation, Inheritance, Polymorphism
- **Design Patterns**:
  - Strategy Pattern (Interest calculation)
  - Singleton Pattern (Bank instance)
  - Factory Pattern (Transaction creation)
- **File Handling**: Java Serialization for data persistence
- **Collections Framework**: For efficient data management

## Class Structure

1. **BankingSystem** (Main class)
   - Handles application startup and main menu

2. **Bank** (Singleton)
   - Core banking operations
   - Account management
   - Transaction processing

3. **Account**
   - Account information storage
   - Balance management
   - Transaction history

4. **Transaction**
   - Transaction records
   - Timestamps
   - Transaction types (Deposit/Withdrawal/Transfer)

5. **InterestStrategy** (Interface)
   - StandardInterest (Implementation)
   - PremiumInterest (Implementation)

6. **AdminController**
   - System statistics
   - Account management
   - Search functionality

7. **Custom Exceptions**
   - InsufficientFundsException
   - InvalidAccountException
   - SecurityException

## Installation

### Prerequisites
- Java JDK 17 or later
- Maven (for building)
