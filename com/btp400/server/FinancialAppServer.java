package com.btp400.server;


import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;

import com.btp400.packets.RequestPacket;
import com.btp400.packets.ResponsePacket;
import com.seneca.accounts.*;
import com.seneca.business.Bank;
import com.seneca.exceptions.AccountExceptions;

public class FinancialAppServer {

	private static Bank m_Bank;

	static final int SUCCESS_CODE = 400;
	static final int GENERIC_ERROR_CODE = 500;
	static final int ACC_NOT_FOUND_CODE = 404;
	static final int ACC_TRANSACTION_ERROR_CODE = 502;
	static final int TRANSACTION_DENIED_CODE = 700;
	static final int DISCONNECT_CODE = 101;
	static final int DISPLAY_CODE = 100; // its really hard to send error code using current display method ;3

	public static void main(String args[]) {

		
		loadBank(new Bank());

		int connectedClients = 0;

		ServerSocket server = null;
		try {
			server = new ServerSocket(8000);
			server.setReuseAddress(true);
			System.out.println("Bank Server Started : " + m_Bank.getBank_Name());
			// The main thread is just accepting new connections
			while (true) {
				Socket client = server.accept();
				System.out.println("New client connected " + client.getInetAddress().getHostAddress());
				ClientHandler clientSock = new ClientHandler(client, ++connectedClients);

				// The background thread will handle each client separately
				new Thread(clientSock).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/*
		 * 
		 * 
		 * do {
		 * 
		 * DisplayMenu(); userChoice = menuChoice(); //System.out.println(userChoice);
		 * 
		 * switch(userChoice) {
		 * 
		 * case 1: openAccount(); break; case 2: closeAccount(); break; case 3:
		 * depositMoney(); break; case 4 : withdrawMoney(); break; case 5:
		 * DisplayAccountsMenu(); break; case 6: DisplayTaxStatement(); break; case 7:
		 * System.out.println("\nExiting..."); userDone = true; break; }
		 * 
		 * 
		 * } while(userDone == false);
		 * 
		 * 
		 */

	}

	private static class ClientHandler implements Runnable {

		private final Socket clientSocket;
		private final int ID;

		public ClientHandler(Socket socket, int id) {
			this.clientSocket = socket;
			this.ID = id;
		}

		@Override
		public void run() {
			ObjectOutputStream out = null;
			ObjectInputStream in = null;
			RequestPacket recvPacket = null;
			String line = "";
			boolean userDone = false;

			String Response_msg = "";
			int Response_code = 0;

			try {
				out = new ObjectOutputStream(clientSocket.getOutputStream());
				in = new ObjectInputStream(clientSocket.getInputStream());

				// 1. Send bank name
				out.writeObject(m_Bank.getBank_Name());

				// 2.
				// out.writeObject(m_bank.);

				while (userDone == false) {

					Response_code = SUCCESS_CODE; // response code for everything being okay

					recvPacket = (RequestPacket) in.readObject();

					line = recvPacket.getMessage();
					System.out.printf("Client " + this.ID + ": %s\n", line);

					switch (recvPacket.getUserChoice()) {

					case 1:

						if (openAccount(recvPacket.getAccount())) {

							System.out.println(recvPacket.getAccount());
							Response_msg = "Account Sucessfully Added";
							System.out.println("success2");
						} else {
							System.out.println("fail");
							Response_code = GENERIC_ERROR_CODE;
							Response_msg = "Oops, it looks like that account may already exist or you sent us a null account.";
							System.out.println("fail2");
						}

						break;

					case 2:
						Account acc;
						acc = closeAccount(recvPacket.getAccNum());

						if (acc != null) {
							Response_msg = "Account sucessfully deleted: \n" + acc;
						} else {
							Response_code = ACC_NOT_FOUND_CODE;
							Response_msg = "Account does not exist";
						}
						break;

					case 3:
						Response_code = depositMoney(recvPacket.getAccNum(), recvPacket.getAmount());

						if (Response_code == ACC_NOT_FOUND_CODE) {
							Response_msg = "Account does not exist";
						} else if (Response_code == ACC_TRANSACTION_ERROR_CODE) {
							Response_msg = "The account encountered a transaction error";
						} else if (Response_code == SUCCESS_CODE) {
							Response_msg = "Money sucessfully deposited";
						}
						break;

					case 4:
						Response_code = withdrawMoney(recvPacket.getAccNum(), recvPacket.getAmount());

						if (Response_code == ACC_NOT_FOUND_CODE) {
							Response_msg = "Account does not exist";
						} else if (Response_code == ACC_TRANSACTION_ERROR_CODE) {
							Response_msg = "The account encountered a transaction error";
						} else if (Response_code == SUCCESS_CODE) {
							Response_msg = "Money sucessfully deposited";
						}

						break;

					case 5:
						Response_msg = DisplayAccountsMenu(recvPacket);
						Response_code = DISPLAY_CODE;
						break;

					case 6:
						Response_msg = DisplayTaxStatement(recvPacket);
						Response_code = DISPLAY_CODE;
						break;
						
					case 7:
						Response_code = DISCONNECT_CODE;
						Response_msg = "disconection request recieved";
						userDone = true;
						break;

					}

					System.out.println("Server : sent to client " + this.ID + "( " + Response_code + ", " + Response_msg + " )");
					out.writeObject(new ResponsePacket(Response_code, Response_msg));
				}

			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Server is still running");
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
				System.out.println("Server is still running");
			} finally {
				try {
					if (out != null) {
						out.close();
					}
					if (in != null)
						in.close();
					clientSocket.close();
					
					System.out.println("Server : client " + this.ID + " disconnected sucessfully");
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println("Server is still running");
				}
			}
		}
	}

	private static String DisplayTaxStatement(RequestPacket packet) {

		Account[] accounts = m_Bank.searchByAccountName(packet.getAccHolderName());
		StringBuffer count = new StringBuffer("\n[");

		String info = null;
		
		Taxable taxAcc = null;
		int i = 1;

		if (accounts.length > 0) {

			
			 info =  "";
			 
			info += (new StringBuffer(accounts[0].getLastName()).append(", ").append(accounts[0].getFirstName()) + "\n");
			
			info += (new StringBuffer("Tax Rate: ")
					.append(String.format("%.0f", Taxable.TAX.multiply(new BigDecimal("100")).doubleValue()))
					.append("%")) + "\n";

			for (Account acc : accounts) {

				if (acc instanceof Taxable) {
					taxAcc = (Taxable) acc;

					info += (new StringBuffer(count).append(i++).append("]") + "\n");
					info += (new StringBuffer(taxAcc.createTaxStatement()).append("\n\n"));

				}

			}
		}
		else {
			info = "No Accounts were found with that name";
		}
		
		return info;
	}

	private static int depositMoney(String userAccountNumber, double despotAmount) {


		Account[] bankAccounts = m_Bank.getAllAccounts();

		int returnNum = 0;
		boolean accFound = false;

		for (Account acc : bankAccounts) {

			if (acc.getAccountNumber().equals(userAccountNumber)) {
				try {
					accFound = true;
					acc.deposit(despotAmount);
					returnNum = SUCCESS_CODE;
					// System.out.println("Money sucessfully deposited.");

				} catch (AccountExceptions e) {
					// System.out.println("TEST");
					returnNum = ACC_TRANSACTION_ERROR_CODE;
					// System.out.println(errMsg.append(e));
				}

			}

		}

		if (accFound == false) {
			returnNum = ACC_NOT_FOUND_CODE;
			// System.out.println("That account does not exist.. good bye");

		}

		return returnNum;

	}

	private static int withdrawMoney(String userAccountNumber, double withdrawAmount) {

		
		Account[] bankAccounts = m_Bank.getAllAccounts();

		boolean accFound = false;
		int returnNum = 0;

		for (Account acc : bankAccounts) {

			if (acc.getAccountNumber().equals(userAccountNumber)) {
				try {

					accFound = true;
					if (acc.withdraw(withdrawAmount)) {
						returnNum = SUCCESS_CODE;
						// System.out.println("Money sucessfully withdrawn.");
					} else {
						returnNum = TRANSACTION_DENIED_CODE;
						// System.out.println("Money withdrawal failed.");
					}

				} catch (AccountExceptions e) {
					returnNum = ACC_TRANSACTION_ERROR_CODE;
					// System.out.println(errMsg.append(e));
				}

			}

		}

		if (accFound == false) {
			returnNum = ACC_NOT_FOUND_CODE;
			// System.out.println("That account does not exist.. good bye");

		}

		return returnNum;

	}

	private static String DisplayAccountsMenu(RequestPacket packet) {

		String info = null;

		switch (packet.getSubUserChoice()) {

		case "a":
			info = DisplayAccounts(1, packet.getAccHolderName(), packet.getAmount());

			break;
		case "b":
			info = DisplayAccounts(2, packet.getAccHolderName(), packet.getAmount());

			break;
		case "c":
			info = DisplayAccounts(3, packet.getAccHolderName(), packet.getAmount());

			break;
		default:
			info = "Invalid input. Try Again";
			break;

		}

		if (info == null) {
			info = "Something went wrong ):";
		}

		return info;

	}

	private static String DisplayAccounts(int option, String accHolderName, double amount) {

		String info = null;
		String fromStrBuffer = null;
		Account[] accounts = null;

		if (option == 1) {

			accounts = m_Bank.searchByAccountName(accHolderName);

		} else if (option == 2) {

			accounts = m_Bank.searchByBalance(amount);
		} else if (option == 3) {
			accounts = m_Bank.getAllAccounts();
		}

		if (accounts != null) {
			if (accounts.length > 0) {

				info = "";
				for (int i = 0; i < accounts.length; ++i) {

					fromStrBuffer = (new StringBuffer("\n[").append((i + 1)).append("]")).toString(); // idk

					info += (fromStrBuffer + "\n" + accounts[i] + "\n"); // print account info to string

				}

			} else {
				info = "Your queury returned empty";
			}
		} else {
			info = "Something unexpected went wrong ";
		}

		return info;

	}

	public static void loadBank(Bank bank) {
		if (bank != null) {
			m_Bank = bank;

			m_Bank.addAccount(new GIC("John Doe", "N100", 6000, 2, 1.5));
			m_Bank.addAccount(new GIC("Mary Ryan", "N101", 15000, 4, 2.5));

			m_Bank.addAccount(new Chequing("John Doe", "N102", 6000, -1, -1));
			m_Bank.addAccount(new Chequing("Mary Ryan", "N103", 15000, -1, -1));
		}

	}

	

	
	

	private static Account closeAccount(String ID) {

		Account acc = null;

		acc = m_Bank.removeAccount(ID);

		return acc;

	}

	public static boolean openAccount(Account acc) {

		return (m_Bank.addAccount(acc));

	}

}
