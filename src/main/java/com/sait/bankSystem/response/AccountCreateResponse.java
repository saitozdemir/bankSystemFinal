package com.sait.bankSystem.response;

import lombok.Data;

@Data
public class AccountCreateResponse {
	
	private String message;
	private long accountNumber;
}
