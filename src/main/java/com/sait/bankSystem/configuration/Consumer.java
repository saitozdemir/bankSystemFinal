package com.sait.bankSystem.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class Consumer {

	@KafkaListener(topics = "logs", groupId = "logs_consumer_group")
	public void listenTransfer(@Payload String message, 
			  @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
			  @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) Integer key
	) {
	    try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("logs.txt")));
			bw.write(message + "\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}