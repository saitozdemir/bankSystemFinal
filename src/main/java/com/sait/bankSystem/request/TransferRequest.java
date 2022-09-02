package com.sait.bankSystem.request;

import lombok.Data;

@Data
public class TransferRequest {
	
	private long transferredAccountNumber;
	private double amount;

}
