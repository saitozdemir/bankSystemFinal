package com.sait.bankSystem.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sait.bankSystem.model.Account;
import com.sait.bankSystem.request.AccountCreateRequest;
import com.sait.bankSystem.request.AccountDepositRequest;
import com.sait.bankSystem.request.TransferRequest;
import com.sait.bankSystem.response.AccountCreateResponse;

@Service
public class AccountService {
	@Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
	
	public ResponseEntity<?> createAccount(AccountCreateRequest request) {
		if (request.getType().toUpperCase() == "DOLAR" || request.getType().toUpperCase() == "TL" || request.getType().toUpperCase() == "ALTIN") {
			Account acc = new Account();
			Random rnd = new Random();
			long accountNumber = rnd.nextLong(100000000L, 1000000000L);
			acc.setNumber(accountNumber);
			acc.setName(request.getName());
			acc.setSurname(request.getSurname());
			acc.setEmail(request.getEmail());
			acc.setTc(request.getTc());
			acc.setType(request.getType());
			acc.setBalance(0);
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date(System.currentTimeMillis());
			acc.setLastUpdateDate(dateFormat.format(date));
			
			try {
				ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(acc.getNumber() + ".txt")));
				os.writeObject(acc);
				os.close();
				AccountCreateResponse successResponse = new AccountCreateResponse();
				successResponse.setMessage("Account was created!");
				successResponse.setAccountNumber(acc.getNumber());
				return ResponseEntity.ok().body(successResponse);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		String message = "Invalid Account Type: " + request.getType();
		return ResponseEntity.badRequest().body(message);
	}
	
	public ResponseEntity<?> deposit(long accountNumber, AccountDepositRequest request) {
		
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(new File(accountNumber + ".txt")));
			try {
				Account acc = (Account)is.readObject();
				acc.setBalance(acc.getBalance()+request.getBalance());
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date(System.currentTimeMillis());
				acc.setLastUpdateDate(dateFormat.format(date));
				ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(new File(acc.getNumber() + ".txt")));
				os.writeObject(acc);
				os.close();
				String message = accountNumber + " deposit amount: " + request.getBalance() + " " + acc.getType();
				kafkaTemplate.send("logs", message);
				return ResponseEntity.ok().body(acc);
			} catch (ClassNotFoundException e) {
					e.printStackTrace();
			}
				
		} catch (FileNotFoundException e) {
				e.printStackTrace();
		} catch (IOException e) {
				e.printStackTrace();
		} 	
		return null;
	}	
	
	public ResponseEntity<?> accountDetail(long accountNumber) {
		try {
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(new File(accountNumber + ".txt")));
			Account acc = (Account)is.readObject();
			return ResponseEntity.ok().body(acc);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public ResponseEntity<?> transferMoney(long accountNumber, TransferRequest request) {
		try {
			ObjectInputStream objInt = new ObjectInputStream(new FileInputStream(new File(accountNumber + ".txt")));
			ObjectInputStream objInt2 = new ObjectInputStream(new FileInputStream(new File(request.getTransferredAccountNumber() + ".txt")));
			try {
				Account sender = (Account) objInt.readObject();
				Account receiver = (Account) objInt2.readObject();
				if(0 <= sender.getBalance() - request.getAmount()) {
					if(sender.getType()!= receiver.getType()) {
						kafkaTemplate.send("logs", "Transfer is not success. Account types are different.");
						return ResponseEntity.badRequest().body("Transfer is not success. Account types are different.");
					}else {
						receiver.setBalance(receiver.getBalance() + request.getAmount());
					}
					
					
					sender.setBalance(sender.getBalance()- request.getAmount());
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date(System.currentTimeMillis());
					sender.setLastUpdateDate(dateFormat.format(date));
					receiver.setLastUpdateDate(dateFormat.format(date));
					
					ObjectOutputStream objOut=new ObjectOutputStream(new FileOutputStream(new File(accountNumber + ".txt")));
					objOut.writeObject(sender);
					objOut.close();
					ObjectOutputStream objOut2 = new ObjectOutputStream(new FileOutputStream(new File(""+request.getTransferredAccountNumber())));
					objOut2.writeObject(receiver);
					objOut2.close();
					String logMessage = accountNumber + " transfer amount: " + request.getAmount() + " " + sender.getType() + ",transferred_account:" + request.getTransferredAccountNumber();
					kafkaTemplate.send("logs", logMessage);
					String message = "Transferred Successfully";
					return ResponseEntity.ok().body(message);
				}else {
					String message = "Insufficient balance";
					return ResponseEntity.badRequest().body(message);
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ResponseEntity<?> transactionLogs(long number) throws IOException {
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("logs.txt"));
			String line = reader.readLine();
			ArrayList<String> list = new ArrayList<String>();
			while (line != null) {
				String[] parts = line.split(" ");
				if (parts[0].equals(number + "")) {
					if (parts[1].equals("deposit")) {
						list.add(number + " nolu hesaba " + parts[3] + " " + parts[4] + " yatırılmıştır.");
					}else {
						list.add(number+" hesaptan " + parts[6] + " hesaba " +parts[3] + " " + parts[4] + " gönderilmiştir.");
					}
					line = reader.readLine();
				}else {
					line = reader.readLine();
				}
			}  
			reader.close();
			return ResponseEntity.ok().body(list);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

}
