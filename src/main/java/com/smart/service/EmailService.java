package com.smart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService 
{
	@Autowired
   private JavaMailSender javaMailSender;
   private boolean flag=false;
	public boolean sendMail(String emailTo,String subject,String message)
	{
		try 
		{
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper helper=new MimeMessageHelper(mimeMessage);
			
			helper.setFrom("sujitmaharana1111@gmail.com");
			helper.setTo(emailTo);
			helper.setSubject(subject);
			mimeMessage.setContent(message, "text/html");
			javaMailSender.send(mimeMessage);
			flag=true;
		} 
		catch (Exception e) 
		{
              e.printStackTrace();
		}
		
		return flag;
	}
}
