package com.btp400.packets;

import java.io.Serializable;

import com.seneca.accounts.Account;

public class RequestPacket implements Serializable{
	
	private String m_message;
	
	private int m_userChoice;
	private String m_subUserChoice;
	private Account m_account;
	private String m_accNum;
	private double m_amount;
	
	private String m_AccHolderName;
	

	
	public RequestPacket() {
		// TODO Auto-generated constructor stub
		m_userChoice = 0;
		m_message = null;
		m_account = null;
		m_accNum = null;
		m_AccHolderName = null;
		m_amount = 0;
		m_subUserChoice = null;
	}
	
	
	public RequestPacket(String message, int choice) {
		// TODO Auto-generated constructor stub
		m_userChoice = choice;
		m_message = message;
		m_account = null;
	
	}
	
	
	
	
	public Account getAccount() {
		return m_account;
	}
	
	public String getMessage() {
		return m_message;
	}
	
	public int getUserChoice() {
		return m_userChoice;
	}
	
	public String getSubUserChoice() {
		return m_subUserChoice;
	}
	
	public String getAccNum() {
		return m_accNum;
	}
	
	public double getAmount() {
		return m_amount;
	}
	
	public String getAccHolderName() {
		return m_AccHolderName;
	}
	
	public void setAccHolderName(String Name) {
		m_AccHolderName = Name;
	}
	
	
	
	
	public void setAmount(double amount) {
		 m_amount = amount;
	}
	
	public void setUserChoice(int choice) {
		m_userChoice = choice;
	}
	
	public void setAccNum(String num) {
		m_accNum = num;
	}
	
	public void setSubUserChoice(String subC) {
		m_subUserChoice = subC;
	}
	

	
	
	public void setMessage(String message) {
		 m_message = message;
	}


	public void setAccount(Account acc) {
		m_account = acc;
		
	}
	

}
