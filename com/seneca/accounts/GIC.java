package com.seneca.accounts;

import java.math.BigDecimal;
import java.util.Objects;

import com.seneca.exceptions.NotAvailableForGIC;

public class GIC extends Account implements Taxable {
	
	
	/**
	 * the period of investment (in years) 
	 */
	private int m_investment_period;
	/**
	 * How much the account will be taxed
	 */
	private double m_tax_amount;
	/**
	 * annual interest rate (in percentage).
	 */
	private BigDecimal m_interest_rate;
	
	
	/**
	 * initializes the period of investment to 1 year and annual interest rate to 1.25%
	 */
	public GIC() {
		this("", "", 0.00, 1, 1.25);
	}

	/**
	 * 
	 * @param name Account holder's name
	 * @param accountNumber Account's unique id number
	 * @param starting_balance Account's starting balance
	 * @param investment_period Account's interest period in years
	 * @param interest_rate Account's annual interest rate
	 */
	public GIC(String name, String accountNumber, double starting_balance, int investment_period, double interest_rate) {

		super(name, accountNumber, starting_balance);
		
		if (investment_period > 0) {
			this.m_investment_period = investment_period;
		}
		else {
			this.m_investment_period = 1;
		}
		
		if (interest_rate > 0) {
			this.m_interest_rate = new BigDecimal(interest_rate);
		}
		else {
			this.m_interest_rate = new BigDecimal("1.25");
		}
		
		this.m_tax_amount = 0;
	}
	
	/**
	 * 
	 * @return The amount of income the account has made in interest
	 */
	public double getInterestIncome() {
		BigDecimal starting_balance = new BigDecimal(super.getBalance());

		BigDecimal income_maturity = new BigDecimal(this.getBalance());
		
		return income_maturity.subtract(starting_balance).doubleValue();	
	}

	
	@Override
	public void calculateTax() {
		
		BigDecimal taxable_income = new BigDecimal(this.getInterestIncome());
		
		this.m_tax_amount = taxable_income.multiply(TAX).doubleValue();
	}
	
	/**
	 * Balance at Maturity = Starting Balance x ( 1 + r ) ^ t
	 * r = annual interest rate
	 * t = number of years (i.e. period of investment) 
	 */
	@Override
	public double getBalance() {
		
		BigDecimal Start = new BigDecimal(super.getBalance()); //Starting Balance
		BigDecimal Interest = m_interest_rate.divide(new BigDecimal("100")).add(new BigDecimal("1")); 
		
		return Start.multiply(Interest.pow(m_investment_period)).doubleValue();
	}

	@Override
	public double getTaxAmount() {
		
		return m_tax_amount;
	}
	
	public int getInvestmentPeriod() {
		return m_investment_period;
	}
	
	public double getIntrestRate() {
		return m_interest_rate.doubleValue();
	}
	

	@Override
	public String createTaxStatement() {
		
		calculateTax();
		
		String[] fields = {"Tax rate", 
						   "Account Number", 
						   "Interest income",
						   "Amount of tax"};
		
		int width = fieldSize(fields);
		
		
		StringBuffer output = new StringBuffer(String.format("%-" + width +"s",fields[0]));
		
		output.append(String.format(": %.0f", TAX.doubleValue() * 100));
		output.append("%");
		
		output.append(String.format("\n%-" + width +"s",fields[1])).append(": ");
		output.append(this.getAccountNumber());
		
		output.append(String.format("\n%-" + width +"s",fields[2])).append(": $");
		output.append(String.format("%8.2f",this.getInterestIncome()));
		
		output.append(String.format("\n%-" + width +"s",fields[3])).append(": $");
		output.append(String.format("%8.2f",this.getTaxAmount()));	
		
		return output.toString();
	}
	

	@Override
	public boolean withdraw(double amount) {
		return false;
	}

	@Override
	public void deposit(double amount) throws NotAvailableForGIC  {
		throw new NotAvailableForGIC("Cannot deposit into a GIC");
	}
	
	@Override
	public String toString() {
					
		StringBuffer output = new StringBuffer(super.toString());
		
		String fields[] = {"Account Type", 
						 "Annual Interest Rate",
						 "Period of Investment",
						 "Interest Income at Maturity",
						 "Balance at Maturity"};
		
		int width = fieldSize(fields);
		
		
		output.append(String.format("%-" + width +"s",fields[0])).append(": GIC\n")
			  
			  .append(String.format("%-" + width +"s",fields[1])).append(": ")
			  .append(String.format("%.2f",m_interest_rate.doubleValue())).append("%\n")
		
			  .append(String.format("%-" + width +"s",fields[2])).append(": ")
			  .append(m_investment_period);
		if(m_investment_period > 1)
			output.append(" years");
		else
			output.append(" year");
		
		output.append(String.format("\n%-" + width +"s",fields[3])).append(": $")
		      .append(String.format("%.2f",this.getInterestIncome()))
		
		      .append(String.format("\n%-" + width +"s",fields[4])).append(": $")
			  .append(String.format("%.2f",this.getBalance()))
			  .append("\n");
		
		return output.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(m_interest_rate, m_investment_period) + super.hashCode();
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		
		if (!super.equals(obj))
			return false;
		
		if (!(obj instanceof GIC))
			return false;
		
		GIC other = (GIC) obj;
		
		return m_interest_rate.equals(other.m_interest_rate) 
				&& m_investment_period == other.m_investment_period; 
	}
}
