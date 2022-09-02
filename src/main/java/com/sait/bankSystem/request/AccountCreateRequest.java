package com.sait.bankSystem.request;

import lombok.Data;

@Data
public class AccountCreateRequest {
	
	private String name;
	private String surname;
	private String email;
	private String tc;
	private String type;
	
	
}
