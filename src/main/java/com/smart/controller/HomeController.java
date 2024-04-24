package com.smart.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController 
{
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	@GetMapping("/")
   public String test( Model model)
   {
	   model.addAttribute("title","Smart contact manager");
	   System.out.println("Home page running..");
	   return "home";
   }
   
   @GetMapping("/about")
   public String about(Model model)
   {
	  model.addAttribute("title","about page");
	  System.out.println("about page is running...");
	  return "about"; 
   }
   
   
   @GetMapping("/signup")
   public String signup(Model model)
   {
	  model.addAttribute("title","Register Here");
	  
	  model.addAttribute("user",new User());
	  System.out.println("Register page is running...");
	  return "signup"; 
   }
   
   @PostMapping("/do_register")
   public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result,Model model,HttpSession session)
   {
	  
	   try 
	   {
		 
		   if(result.hasErrors())
		   {
			   //System.out.println("ERROR"+result.toString());
			   model.addAttribute("user",user);
			   return "signup";
		   }
		   user.setRole("ROLE_USER");
		   user.setEnabled(true);
		   user.setPassword(passwordEncoder.encode(user.getPassword()));
		   user.setImageUrl("default.png");
		   
		   //SAVING USER
		   this.userRepository.save(user);
		   
		   model.addAttribute("user",new User());
		   session.setAttribute("message",new Message("Successfully Registered !!","alert-success"));

		   
	   } 
	   catch (Exception e) 
	   {
		e.printStackTrace();
		model.addAttribute("user",user);
		session.setAttribute("message",new Message("Something went wrong !!"+e.getMessage(),"alert-danger"));
		
		return "signup";
	   }
	   return "signup";
   }
   
   @GetMapping("/signin")
   public String customeLogin()
   {
	   return "login";
   }
   
}
