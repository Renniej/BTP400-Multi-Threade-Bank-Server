package com.seneca.accounts;

import java.math.BigDecimal;

public interface Taxable {
	
	final BigDecimal TAX = new BigDecimal(.15);

	/**
	 * Calculates the amount of tax the account will be charged
	 */
	void calculateTax();
	
	/**
	 * 
	 * @return the amount of tax the account will be charged
	 */
	double getTaxAmount();
	
	
	String createTaxStatement();
}
