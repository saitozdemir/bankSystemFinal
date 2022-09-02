package com.sait.bankSystem.model;

import lombok.Data;

@Data
public class Account {
	
	private long number;
	private String name;
	private String surname;
	private String email;
	private String tc;
	private String type;
	private double balance;
	private String lastUpdateDate;	
}
