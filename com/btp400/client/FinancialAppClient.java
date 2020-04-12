package com.btp400.client;


import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.InetAddress;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

import com.btp400.packets.RequestPacket;
import com.btp400.packets.ResponsePacket;
import com.seneca.accounts.Account;
import com.seneca.accounts.Chequing;
import com.seneca.accounts.GIC;


public class FinancialAppClient {

	private static String m_bankName;
	private static RequestPacket sentPacket;
	private static ResponsePacket recvPacket;

	public static void main(String args[]) {

		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		recvPacket = null;
		sentPacket = null;
		
		boolean userDone = false;
		int userChoice = 0;

		try (Socket socket = new Socket(InetAddress.getByName("localhost"), 8000)) {

			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());

			// 1. Recieve bank name for menu ;3
			m_bankName = (String) in.readObject();

			Scanner scanner = new Scanner(System.in);
	

		
				do {

					sentPacket = new RequestPacket();

					DisplayMenu();

					userChoice = menuChoice();

					sentPacket.setUserChoice(userChoice);

					// System.out.println(userChoice);

					switch (userChoice) { // Choose action and format request packet for that action

					// each case handles formatting a request packet object
					case 1:
						openAccount();
						sentPacket.setMessage("Requesting to add account");
						break;

					case 2:
						closeAccount();
						sentPacket.setMessage("Requesting to close an account");
						break;

					case 3:
						getTransactionInfo();
						sentPacket.setMessage("Requesting to deposit money");
						break;

					case 4:
						getTransactionInfo();
						sentPacket.setMessage("Requesting to withdraw money");
						break;
					case 5:
						DisplayAccountsMenu();
						sentPacket.setMessage("Requesting to display accounts");
						break;
					 case 6: 
						 DisplayTaxStatement(); 
						 sentPacket.setMessage("Requesting to display tax statement");
						 break;
					 case 7:
						 sentPacket.setMessage("Ending connection");
						
						 userDone = true; 
						 break;
					 
					}

					// after request has been formatted send out those packets
					out.writeObject(sentPacket);
					out.flush();
					
					System.out.println("\n --- Request sent to server ---\n");

					recvPacket = (ResponsePacket) in.readObject();

					System.out.println(recvPacket.getMessage() + "\n");

				} while (userDone == false);

				
				
			
				 System.out.println("\nExiting...");
				try {
					if (out != null) {
						out.close();
					}
					if (in != null)
						in.close();
					socket.close();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			scanner.close();
			
			 System.out.println("Finished ending connect. Goodbye.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}

	}

	public static void DisplayMenu() {



		StringBuffer menu = new StringBuffer("\nWelcome to ").append(m_bankName).append(" Bank!\n");
		menu.append("1. Open an account.\n");
		menu.append("2. Close an account.\n");
		menu.append("3. Deposit money.\n");
		menu.append("4. Withdraw money.\n");
		menu.append("5. Display accounts.\n");
		menu.append("6. Display a tax statement.\n");
		menu.append("7. Exit.");

		

		System.out.println(menu);

	}

	private static void closeAccount() {

		
		Scanner userInput = new Scanner(System.in);

		System.out.println("Please enter account number :  ");

		sentPacket.setAccNum(userInput.nextLine());

	}

	private static void getTransactionInfo() {

		sentPacket.setAccNum(getUserInputString("account number"));
		sentPacket.setAmount(getUserInputMoneyAmount());

	}
	
	
private static void DisplayTaxStatement() {
		
		

		sentPacket.setAccHolderName(getUserInputString("account holder's name"));
		
		
	}

	public static int menuChoice() {

		int int_userInput = -1;
		boolean valid_input = true;
		Scanner userInput = new Scanner(System.in);

		do {

			System.out.println("\nPlease enter your choice : ");

			try {

				int_userInput = userInput.nextInt();

				if (!(int_userInput >= 1 && int_userInput <= 7)) {
					valid_input = false;
					System.out.println("\nThat is not a valid numbered option.");
				} else {
					valid_input = true;
				}
			} catch (InputMismatchException e) {

				valid_input = false;
				System.out.println("\nThat was not a valid input. Try Again.");

			} catch (Exception e) {
				valid_input = false;
				System.out.println("\nOops.. Something unexpected happended. Try Again.");
			}

			userInput.nextLine(); // consume \n after nextInt is called.
		} while (valid_input == false);

		return int_userInput;

	}

	public static void openAccount() {

		Account acc = produceAccountFromUser();

		sentPacket.setAccount(acc);

		// System.out.println(acc);

	}

	public static Account produceAccountFromUser() { // Produces an account object from user's inputs

		boolean valid_accData = false;
		String[] accData = null;
		Account acc = null;

		String accType = getUserInputAccountType();

		do {

			accData = getUserInputAccountData();

			if (accData.length == 5) {

				if (accType.equals("GIC")) {

					try {

						acc = new GIC(accData[0], accData[1], Double.parseDouble(accData[2]),
								Integer.parseInt(accData[4]), Double.parseDouble(accData[3]));
						valid_accData = true;

					} catch (NumberFormatException e) {
						System.out.println("\nThere was a type mismatch in one of your fields ):");
					} catch (Exception e) {
						System.out.println(
								"\nThere was an unexpected error in your input. Please send this error to devs and try again:"
										+ e);
					}

				} else if (accType.equals("CHQ")) {
					try {

						acc = new Chequing(accData[0], accData[1], Double.parseDouble(accData[2]),
								Double.parseDouble(accData[3]), Integer.parseInt(accData[4]));
						valid_accData = true;

					} catch (NumberFormatException e) {
						System.out.println("\nThere was a type mismatch in one of your fields ):");
					} catch (Exception e) {
						System.out.println(
								"\nThere was an unexpected error in your input. Please send this error to devs and try again:"
										+ e);
					}
				}

			} else {
				System.out.println("\nThe GIC & CHQ account only accepts 5 parameters. Please try again.");
			}
		} while (valid_accData == false);

		return acc;
	}

	public static String getUserInputAccountType() {

		Scanner userInput = new Scanner(System.in);
		String str_userInput;
		boolean valid_accType = false;

		do {
			System.out.println("\nPlease enter the account type (CHQ/GIC) : ");
			str_userInput = userInput.nextLine();

			if (str_userInput.equals("GIC") || (str_userInput.equals("CHQ"))) {
				valid_accType = true;
			} else {
				System.out.println("\nInvalid account type. Try again..");
			}

		} while (valid_accType == false);

		return str_userInput;
	}

	public static String[] getUserInputAccountData() {

		Scanner userInput = new Scanner(System.in);
		String[] accData = null;

		System.out.println(
				"Please enter account information at one line\r\n" + "(e.g. John M. Doe;A1234;1000.00;1.5;2)\r\n");

		accData = userInput.nextLine().split("\\s*;\\s*");

		return accData;
	}

	public static double getUserInputMoneyAmount() {

		double double_userInput = 0;
		boolean valid_input = false;
		Scanner userInput = new Scanner(System.in);

		String str_userInput = null;

		do {

			System.out.println("Please enter amount:");
			str_userInput = userInput.nextLine();

			try {

				double_userInput = Double.parseDouble(str_userInput);
				valid_input = true;

			} catch (NumberFormatException e) {
				System.out.println("\nThere was a type mismatch in one of your fields ):");
			} catch (Exception e) {
				System.out.println(
						"\nThere was an unexpected error in your input. Please send this error to devs and try again:"
								+ e);
			}

		} while (valid_input == false);

		return double_userInput;

	}

	public static String getUserInputString(String field) {

		Scanner userInput = new Scanner(System.in);
		String str_userInput;

		System.out.println(new StringBuffer("\nPlease enter ").append(field).append(" :"));
		str_userInput = userInput.nextLine();

		return str_userInput;
	}

	private static void DisplayAccountsMenu() {

		Scanner userInput = new Scanner(System.in);
		
		String choice;
		boolean valid_input = false;

		System.out.println(
				"\na) Display all accounts with the same name\nb) Display all accounts with the same final balance\nc) Display all accounts opened at the bank\n");

		
		do {
			
			choice = userInput.nextLine();

			switch (choice) {

			case "a":
				DisplayAccounts(1);
				valid_input = true;
				break;
			case "b":
				DisplayAccounts(2);
				valid_input = true;
				break;
			case "c":
				DisplayAccounts(3);
				valid_input = true;
				break;
			default:
				System.out.println("Invalid input. Try Again");
				break;

			}

		} while (valid_input == false);

		sentPacket.setSubUserChoice(choice);
		
	}

	private static void DisplayAccounts(int option) {

		Scanner userInput = new Scanner(System.in);

		StringBuffer prompt = new StringBuffer();
	

		if (option == 1) {

			System.out.println(prompt.append("Please enter Name"));
			
			sentPacket.setAccHolderName(userInput.nextLine());
			

		} else if (option == 2) {

			
			sentPacket.setAmount(getUserInputMoneyAmount());
			
		} else if (option == 3) {
			//accounts = m_Bank.getAllAccounts();
		}

		

	}
}
