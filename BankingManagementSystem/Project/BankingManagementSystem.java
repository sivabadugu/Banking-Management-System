
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;

// Enhanced custom exception hierarchy for banking operations
abstract class BankingException extends Exception {
    public BankingException(String message) {
        super(message);
    }
}

class InvalidAmountException extends BankingException {
    public InvalidAmountException() {
        super("Amount must be positive");
    }
}

class InsufficientFundsException extends BankingException {
    public InsufficientFundsException() {
        super("Insufficient funds");
    }
}

class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber);
    }
}

class AuthenticationException extends BankingException {
    public AuthenticationException() {
        super("Invalid credentials");
    }
}

// Interest calculation strategy interface
interface InterestCalculationStrategy {
    double calculateInterest(Account account);
}

// Standard interest calculation
class StandardInterest implements InterestCalculationStrategy {
    private double rate;

    public StandardInterest(double rate) {
        this.rate = rate;
    }

    @Override
    public double calculateInterest(Account account) {
        return account.getBalance() * rate / 100;
    }
}

// Premium interest calculation (higher rate for higher balances)
class PremiumInterest implements InterestCalculationStrategy {
    @Override
    public double calculateInterest(Account account) {
        double balance = account.getBalance();
        if (balance > 100000) {
            return balance * 0.05;
        } else if (balance > 50000) {
            return balance * 0.03;
        } else {
            return balance * 0.01;
        }
    }
}

// Account class representing a bank account
class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private String accountNumber;
    private String accountHolder;
    private double balance;
    private String password;
    private List<Transaction> transactions;
    private LocalDate creationDate;
    private InterestCalculationStrategy interestStrategy;

    public Account(String accountNumber, String accountHolder, double initialDeposit, String password) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = initialDeposit;
        this.password = password;
        this.transactions = new ArrayList<>();
        this.creationDate = LocalDate.now();
        this.interestStrategy = new StandardInterest(1.0); // Default 1% interest
        this.transactions.add(new Transaction("Initial Deposit", initialDeposit, balance));
    }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getAccountHolder() { return accountHolder; }
    public double getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }
    public LocalDate getCreationDate() { return creationDate; }
    public int getAccountAgeInMonths() {
        return Period.between(creationDate, LocalDate.now()).getMonths();
    }

    public void setInterestStrategy(InterestCalculationStrategy strategy) {
        this.interestStrategy = strategy;
    }

    // Account operations
    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }
        balance += amount;
        transactions.add(new Transaction("Deposit", amount, balance));
    }

    public void withdraw(double amount) throws InvalidAmountException, InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAmountException();
        }
        if (amount > balance) {
            throw new InsufficientFundsException();
        }
        balance -= amount;
        transactions.add(new Transaction("Withdrawal", -amount, balance));
    }

    public void calculateInterest() throws InvalidAmountException {
        double interest = interestStrategy.calculateInterest(this);
        if (interest <= 0) {
            throw new InvalidAmountException();
        }
        balance += interest;
        transactions.add(new Transaction("Interest", interest, balance));
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public void changePassword(String oldPassword, String newPassword) throws AuthenticationException {
        if (!authenticate(oldPassword)) {
            throw new AuthenticationException();
        }
        this.password = newPassword;
    }

    @Override
    public String toString() {
        return String.format("Account[%s, Holder: %s, Balance: %.2f, Created: %s]", 
                accountNumber, accountHolder, balance, creationDate);
    }
}

// Enhanced Transaction class with transaction types
class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Type { DEPOSIT, WITHDRAWAL, INTEREST, TRANSFER }
    
    private Type type;
    private String description;
    private double amount;
    private double balanceAfter;
    private String timestamp;
    private String relatedAccount; // For transfers

    public Transaction(String description, double amount, double balanceAfter) {
        this.type = amount >= 0 ? Type.DEPOSIT : Type.WITHDRAWAL;
        this.description = description;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public Transaction(Type type, String description, double amount, double balanceAfter, String relatedAccount) {
        this.type = type;
        this.description = description;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.relatedAccount = relatedAccount;
    }

    @Override
    public String toString() {
        String base = String.format("[%s] %s (%s): %.2f, Balance: %.2f", 
                timestamp, description, type, amount, balanceAfter);
        if (relatedAccount != null) {
            base += String.format(" (Related Account: %s)", relatedAccount);
        }
        return base;
    }
}

// Enhanced Bank class with additional features
class Bank implements Serializable {
    private static final long serialVersionUID = 1L;
    private Map<String, Account> accounts;
    private transient double totalTransactionAmount; // Not serialized
    private transient int transactionCount; // Not serialized

    public Bank() {
        this.accounts = new HashMap<>();
        this.totalTransactionAmount = 0;
        this.transactionCount = 0;
    }

    public void addAccount(Account account) throws BankingException {
        if (accounts.containsKey(account.getAccountNumber())) {
            throw new RuntimeException("Account number already exists");
        }
        accounts.put(account.getAccountNumber(), account);
    }

    public Account getAccount(String accountNumber) throws AccountNotFoundException {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }

    public boolean authenticate(String accountNumber, String password) throws AccountNotFoundException {
        Account account = getAccount(accountNumber);
        return account.authenticate(password);
    }

    public void transfer(String fromAccount, String toAccount, double amount) 
            throws BankingException {
        Account source = getAccount(fromAccount);
        Account destination = getAccount(toAccount);
        
        source.withdraw(amount);
        destination.deposit(amount);
        
        // Record transfer transactions
        String desc = String.format("Transfer to %s", toAccount);
        source.getTransactions().add(new Transaction(
            Transaction.Type.TRANSFER, desc, -amount, source.getBalance(), toAccount));
        
        desc = String.format("Transfer from %s", fromAccount);
        destination.getTransactions().add(new Transaction(
            Transaction.Type.TRANSFER, desc, amount, destination.getBalance(), fromAccount));
        
        // Update bank statistics
        totalTransactionAmount += amount;
        transactionCount++;
    }

    public double getAverageTransactionAmount() {
        return transactionCount == 0 ? 0 : totalTransactionAmount / transactionCount;
    }

    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this);
        }
    }

    public static Bank loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            Bank bank = (Bank) ois.readObject();
            // Initialize transient fields after deserialization
            bank.totalTransactionAmount = 0;
            bank.transactionCount = 0;
            return bank;
        }
    }

    public List<Account> getAccountsByHolder(String holderName) {
        List<Account> result = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account.getAccountHolder().equalsIgnoreCase(holderName)) {
                result.add(account);
            }
        }
        return result;
    }
}

// Main class to run the banking system
public class BankingManagementSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static Bank bank;
    private static final String DATA_FILE = "bank_data.dat";

    public static void main(String[] args) {
        initializeBank();
        
        boolean running = true;
        while (running) {
            System.out.println("\nBanking Management System");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Admin Menu");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1: createAccount(); break;
                    case 2: login(); break;
                    case 3: adminMenu(); break;
                    case 4: running = false; break;
                    default: System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (BankingException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }

        saveBankData();
    }

    private static void initializeBank() {
        try {
            bank = Bank.loadFromFile(DATA_FILE);
            System.out.println("Loaded existing bank data with " + 
                bank.getAccountsByHolder("").size() + " accounts.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Creating new bank data file.");
            bank = new Bank();
        }
    }

    private static void saveBankData() {
        try {
            bank.saveToFile(DATA_FILE);
            System.out.println("Bank data saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save bank data: " + e.getMessage());
        }
    }

    private static void createAccount() throws BankingException {
        System.out.println("\nCreate New Account");
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        
        System.out.print("Enter account holder name: ");
        String accountHolder = scanner.nextLine();
        
        System.out.print("Enter initial deposit: ");
        double initialDeposit = Double.parseDouble(scanner.nextLine());
        
        System.out.print("Set password: ");
        String password = scanner.nextLine();

        Account account = new Account(accountNumber, accountHolder, initialDeposit, password);
        bank.addAccount(account);
        System.out.println("Account created successfully: " + account);
    }

    private static void login() throws BankingException {
        System.out.println("\nLogin");
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (bank.authenticate(accountNumber, password)) {
            accountMenu(accountNumber);
        } else {
            throw new AuthenticationException();
        }
    }

    private static void accountMenu(String accountNumber) {
        try {
            Account account = bank.getAccount(accountNumber);
            boolean loggedIn = true;
            
            while (loggedIn) {
                System.out.println("\nAccount Menu: " + account.getAccountHolder());
                System.out.println("1. Deposit");
                System.out.println("2. Withdraw");
                System.out.println("3. Check Balance");
                System.out.println("4. View Transactions");
                System.out.println("5. Calculate Interest");
                System.out.println("6. Transfer Funds");
                System.out.println("7. Change Password");
                System.out.println("8. Account Details");
                System.out.println("9. Logout");
                System.out.print("Enter choice: ");

                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1: deposit(account); break;
                    case 2: withdraw(account); break;
                    case 3: checkBalance(account); break;
                    case 4: viewTransactions(account); break;
                    case 5: calculateInterest(account); break;
                    case 6: transferFunds(account); break;
                    case 7: changePassword(account); break;
                    case 8: showAccountDetails(account); break;
                    case 9: loggedIn = false; break;
                    default: System.out.println("Invalid choice. Please try again.");
                }
            }
            System.out.println("Logged out successfully.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void deposit(Account account) {
        try {
            System.out.print("Enter deposit amount: ");
            double amount = Double.parseDouble(scanner.nextLine());
            account.deposit(amount);
            System.out.printf("Deposit successful. New balance: %.2f%n", account.getBalance());
        } catch (InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void withdraw(Account account) {
        try {
            System.out.print("Enter withdrawal amount: ");
            double amount = Double.parseDouble(scanner.nextLine());
            account.withdraw(amount);
            System.out.printf("Withdrawal successful. New balance: %.2f%n", account.getBalance());
        } catch (InvalidAmountException | InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void checkBalance(Account account) {
        System.out.printf("Current balance: %.2f%n", account.getBalance());
    }

    private static void viewTransactions(Account account) {
        System.out.println("\nTransaction History:");
        if (account.getTransactions().isEmpty()) {
            System.out.println("No transactions found.");
        } else {
            for (Transaction t : account.getTransactions()) {
                System.out.println(t);
            }
        }
    }

    private static void calculateInterest(Account account) {
        try {
            account.calculateInterest();
            System.out.printf("Interest applied. New balance: %.2f%n", account.getBalance());
        } catch (InvalidAmountException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void transferFunds(Account sourceAccount) {
        try {
            System.out.print("Enter destination account number: ");
            String destAccount = scanner.nextLine();
            
            System.out.print("Enter transfer amount: ");
            double amount = Double.parseDouble(scanner.nextLine());
            
            bank.transfer(sourceAccount.getAccountNumber(), destAccount, amount);
            System.out.printf("Transfer successful. New balance: %.2f%n", sourceAccount.getBalance());
        } catch (BankingException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void changePassword(Account account) {
        try {
            System.out.print("Enter current password: ");
            String current = scanner.nextLine();
            
            System.out.print("Enter new password: ");
            String newPass = scanner.nextLine();
            
            account.changePassword(current, newPass);
            System.out.println("Password changed successfully.");
        } catch (AuthenticationException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void showAccountDetails(Account account) {
        System.out.println("\nAccount Details:");
        System.out.println(account);
        System.out.println("Account age: " + account.getAccountAgeInMonths() + " months");
    }

    private static void adminMenu() {
        System.out.println("\nAdmin Menu");
        System.out.println("1. View All Accounts");
        System.out.println("2. View Account by Holder");
        System.out.println("3. Bank Statistics");
        System.out.println("4. Back to Main Menu");
        System.out.print("Enter choice: ");

        try {
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1: viewAllAccounts(); break;
                case 2: viewAccountsByHolder(); break;
                case 3: showBankStatistics(); break;
                case 4: return;
                default: System.out.println("Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewAllAccounts() {
        Collection<Account> accounts = bank.getAccountsByHolder("");
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            System.out.println("\nAll Accounts:");
            for (Account acc : accounts) {
                System.out.println(acc);
            }
            System.out.println("Total accounts: " + accounts.size());
        }
    }

    private static void viewAccountsByHolder() {
        System.out.print("Enter account holder name: ");
        String name = scanner.nextLine();
        List<Account> accounts = bank.getAccountsByHolder(name);
        
        if (accounts.isEmpty()) {
            System.out.println("No accounts found for " + name);
        } else {
            System.out.println("\nAccounts for " + name + ":");
            for (Account acc : accounts) {
                System.out.println(acc);
            }
        }
    }

    private static void showBankStatistics() {
        System.out.println("\nBank Statistics:");
        Collection<Account> accounts = bank.getAccountsByHolder("");
        System.out.println("Total accounts: " + accounts.size());
        System.out.printf("Average transaction amount: %.2f%n", bank.getAverageTransactionAmount());
        
        double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
        System.out.printf("Total bank balance: %.2f%n", totalBalance);
    }
}