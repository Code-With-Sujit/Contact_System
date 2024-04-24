package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController 
{
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	Random random = new Random(1000);
	
    //form open handler
    @GetMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
    
    @PostMapping("/send_otp")
   	public String sendOTP(@RequestParam("email") String email,HttpSession session)
   	{
    	//System.out.println(email);
    	
    	//generating random no
    	int otp = random.nextInt(99999);
    	//System.out.println(otp);
    	 
    	//verify OTP code here
    	
    	String subject="OTP from Smart Contact Manager App";
    	String message="<h1 style='border:1px solid grey;padding:20px;background-color:black;color:white;'>OTP :- <b>"+otp+"</b></h1>";
    	boolean result = this.emailService.sendMail(email,subject,message);
    	if(result) 
    	{
    		session.setAttribute("myOTP", otp);
    		session.setAttribute("email", email);
    		session.setAttribute("message", new Message("We have sent OTP to your email", "success"));
    		return "verify_otp";
    	}
    	else 
    	{
    		session.setAttribute("message", new Message("Invalid email !!", "danger"));
            return "redirect:/forgot_email_form";
		}
    	
   		
   	}
    
    //verifying otp
    @PostMapping("/verify_otp")
    public String verifyingOTP(@RequestParam("otp") int otp,HttpSession session,Model model)
    {
    	int myOTP=(int)session.getAttribute("myOTP");
    	String email=(String)session.getAttribute("email");
    	if(myOTP == otp)
    	{
    		//password change form
    		User user = this.userRepository.getUserByUserName(email);
    		if(user==null)
    		{
    			//send error message
    			session.setAttribute("message", new Message("User doesn't exist with this email !!", "danger"));
                return "forgot_email_form";
    		}
    		else 
    		{
    			 //send change password form
    			model.addAttribute("title","Change password form");
    			return "password_change_form";
    		}
    		
    	}
    	else
    	{
    		session.setAttribute("message",new Message("You have entered wrong OTP", "danger"));
    		return"verify_otp";
    	}
    }
    
    
    //change password handler
    @PostMapping("/change_password")
    public String changePassword(@RequestParam("password") String password,HttpSession session)
    {
    	String email =(String) session.getAttribute("email");
    	User user = this.userRepository.getUserByUserName(email);
    	user.setPassword(this.bCryptPasswordEncoder.encode(password));
    	this.userRepository.save(user);
    	
    	session.setAttribute("message", new Message("Password changed successfully", "success"));
    	return "redirect:/signin?change=Password changed successfully...";
    }
    
    
    
}
