package com.bank.util;

import com.bank.model.Account;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

// CO3: File Handling using FileReader, FileWriter, BufferedReader, ObjectStream
public class FileHandler {

    private static final String DATA_DIR = "data/";
    private static final String ACCOUNTS_FILE = DATA_DIR + "accounts.dat";

    static {
        // Ensure data directory exists
        new File(DATA_DIR).mkdirs();
    }

    // Save all accounts to file
    @SuppressWarnings("unchecked")
    public static void saveAccounts(Map<String, Account> accountsByNumber,
                                    Map<String, String> usernameToAccount) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(ACCOUNTS_FILE)))) {
            oos.writeObject(accountsByNumber);
            oos.writeObject(usernameToAccount);
        } catch (IOException e) {
            System.err.println("Error saving accounts: " + e.getMessage());
        }
    }

    // Load all accounts from file
    @SuppressWarnings("unchecked")
    public static Object[] loadAccounts() {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) {
            return new Object[]{new HashMap<String, Account>(), new HashMap<String, String>()};
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(ACCOUNTS_FILE)))) {
            Map<String, Account> accountsByNumber = (Map<String, Account>) ois.readObject();
            Map<String, String> usernameToAccount = (Map<String, String>) ois.readObject();
            return new Object[]{accountsByNumber, usernameToAccount};
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading accounts: " + e.getMessage());
            return new Object[]{new HashMap<String, Account>(), new HashMap<String, String>()};
        }
    }

    // Export bank statement to text file
    public static String exportStatement(Account account) {
        String fileName = DATA_DIR + "statement_" + account.getAccountNumber() + "_" +
                System.currentTimeMillis() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("========================================");
            writer.newLine();
            writer.write("      BANK MANAGEMENT SYSTEM");
            writer.newLine();
            writer.write("         ACCOUNT STATEMENT");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.write("Account Holder : " + account.getAccountHolderName());
            writer.newLine();
            writer.write("Account Number : " + account.getAccountNumber());
            writer.newLine();
            writer.write("Account Type   : " + account.getAccountType().getDisplayName());
            writer.newLine();
            writer.write("Current Balance: ₹" + String.format("%.2f", account.getBalance()));
            writer.newLine();
            writer.write("----------------------------------------");
            writer.newLine();
            writer.write("TRANSACTION HISTORY");
            writer.newLine();
            writer.write("----------------------------------------");
            writer.newLine();

            if (account.getTransactionHistory().isEmpty()) {
                writer.write("No transactions found.");
                writer.newLine();
            } else {
                for (var txn : account.getTransactionHistory()) {
                    writer.write(txn.toString());
                    writer.newLine();
                }
            }

            writer.write("========================================");
            writer.newLine();
            writer.write("Generated at: " + java.time.LocalDateTime.now());
            writer.newLine();
            writer.write("========================================");

        } catch (IOException e) {
            return null;
        }

        return fileName;
    }
}
