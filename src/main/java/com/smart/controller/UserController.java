package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController 
{
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//adding common data to all response for all handler upcoming request
	@ModelAttribute
	public void addCommonData(Model model,Principal p)
	{
		String name = p.getName();
		User user = repository.getUserByUserName(name);
		model.addAttribute("user",user);
	}
	
	//home dashboard
	@GetMapping("/index")
    public String dashboard(Model model)
    {
		model.addAttribute("title","Home Dashboard");
		return "normal/user_dashboard";
    }
	
	//open add form handler
	@GetMapping("/add_contact")
	public String openContactForm(Model model)
	{
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
    @PostMapping("/process-contact")
	public String processContact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session)
	{
    	try 
    	{
    		//System.out.println(contact);
        	
        	String name = principal.getName();
        	User user = this.repository.getUserByUserName(name);
        	
        	//processing and uploading file
        	if(file.isEmpty())
        	{
        	  //if the file is empty
        		contact.setImage("contact.png");       		
        		System.out.println("File is empty");
        	}
        	else 
        	{
        		 //upload the file to folder and update the image name into contact
        		contact.setImage(file.getOriginalFilename());
        		
        		File saveFile = new ClassPathResource("static/img").getFile();
        		
        		Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
        		
        		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        		
        		
        		System.out.println("Image is uploaded");
        		
        	}
        	
        	contact.setUser(user);
        	user.getContacts().add(contact);
        	
        	
        	this.repository.save(user);
        	
        	//message success......
        	session.setAttribute("message", new Message("Contact added successfully","success"));
    		
		} 
    	catch (Exception e) 
    	{
			System.out.println("Error : "+e.getMessage());
			//message failure ..........
			session.setAttribute("message",new Message("Something went wrong try again", "danger"));
			return "normal/add_contact_form";
		}
    	return "normal/add_contact_form";
    	
	}
    
    
    //show contact handler
    //per page =5(n)
    //current page=0[page]
    
    @GetMapping("/show_contact/{page}")
    public String showContact(@PathVariable("page") int page,Model model,Principal principal)
    {
    	model.addAttribute("title","View contact");
    	
    	//contact ki list ko bhejna hai
    	
    /*	
     * one way to get contact details of logined user
     * String name = principal.getName();
    	User user = this.repository.getUserByUserName(name);
    	List<Contact> contacts = user.getContacts();
    	
    */	
    	String name = principal.getName();
    	User user = this.repository.getUserByUserName(name);
    	
/* pagination logic */
    	Pageable pageable=PageRequest.of(page, 4);
    	
    	Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
    	
    	model.addAttribute("currentPage",page);
    	model.addAttribute("totalPages",contacts.getTotalPages());
    	model.addAttribute("contacts",contacts);
    	
    	return "normal/show_contacts";
    }
    
    
    //Showing particular contact details.
    
    @GetMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") int contactId,Model model,Principal principal)
    {
    	//System.out.println("CID"+contactId);
    	Optional<Contact> contactOptional = this.contactRepository.findById(contactId);
    	
    	
    	try 
    	{
    		Contact contact = contactOptional.get();
    		String name = principal.getName();
            User user = this.repository.getUserByUserName(name);
            
            if(user.getId() == contact.getUser().getId())
            {
            	model.addAttribute("contact",contact);
            	model.addAttribute("title",contact.getName());
            }
		} 
    	catch (Exception e) 
    	{
			System.out.println(e.getMessage());
		}
    	
    	return "normal/contact_details";
    }
    
    
    //delete contact handler
    
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") int conactId,HttpSession session)
    {
    	try 
    	{
    		Optional<Contact> optionalUser = this.contactRepository.findById(conactId);
    		Contact contact = optionalUser.get();
        	contact.setUser(null);
        	
        		this.contactRepository.delete(contact);
        		//message success......
            	session.setAttribute("message", new Message("Contact deleted successfully","success"));
        	    
		} 
    	catch (Exception e) 
    	{
			System.out.println(e.getMessage());
			//message failure ..........
			session.setAttribute("message",new Message("Something went wrong try again", "danger"));
		}
    	
    	
    	return "redirect:/user/show_contact/0";
    }
    
    
    //updating form
    @PostMapping("/updateContact/{cId}")
    public String updateContactForm(@PathVariable("cId") int cid,Model model)
    {
    	model.addAttribute("title","update Contact");
    	Optional<Contact> optionalContact = this.contactRepository.findById(cid);
    	Contact contact = optionalContact.get();
    	model.addAttribute("contact",contact);
    	return "normal/updateForm";
    }
    
    
    //processing updating form
    @PostMapping("/process-update")
    public String processingContactForm(@ModelAttribute("contact") Contact contact,@RequestParam("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal)
    {
    	
    	try 
    	{
    		Contact oldContact=this.contactRepository.findById(contact.getCid()).get();
    		
			//image if new
    		if(!file.isEmpty())
    		{
    			//file work
    			//rewrite
    			//delete old photo
    			
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file1=new File(deleteFile,oldContact.getImage());
                file1.delete();
    			
    			//and update photo
    			
                File saveFile = new ClassPathResource("static/img").getFile();
        		
        		Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
        		
        		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        		
        		contact.setImage(file.getOriginalFilename());
    			
    		}
    		else 
    		{
    			//System.out.println("your profile photo is not changed");
    			contact.setImage(oldContact.getImage());
    			
			}
    		String name = principal.getName();
    		User user = this.repository.getUserByUserName(name);
    		contact.setUser(user);
    	    this.contactRepository.save(contact);
    	  //message success......
        	session.setAttribute("message", new Message("Contact updated successfully","success"));
		} 
    	catch (Exception e) 
    	{
			e.printStackTrace();
			//message success......
        	session.setAttribute("message", new Message(" Something went worng ! try again..","danger"));
		}
    	
    	return "redirect:/user/"+contact.getCid()+"/contact";
    }
    
    
    //User Profile
    @GetMapping("/userProfile")
    public String userProfileView(Model model)
    {
    	model.addAttribute("title","User Profile");
    	return "normal/userProfile";
    }
    
    //open setting handler
    @GetMapping("/settings")
    public String openSettings(Model model)
    {
    	model.addAttribute("title","Settings");
    	return "normal/settings";
    }
    
    
    //change password handler
    @PostMapping("/change_password")
    public String changePassword(@RequestParam("old_Password") String oldPassword,@RequestParam("new_Password") String newPassword,Principal principal,HttpSession session)
    {
    	String name = principal.getName();
    	//get the user using username(Email)
    	User curentUser = this.repository.getUserByUserName(name);
    	if(bCryptPasswordEncoder.matches(oldPassword,curentUser.getPassword()))
    	{
    		//System.out.println("Your password changed successfully...");
    		//change password here
    		curentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
    		
    		//saving the same user
    		this.repository.save(curentUser);
    		session.setAttribute("message", new Message("Your password is successfully changed..", "success"));
    		
    	}
    	else {
			//System.out.println("Something went wrong");
    		session.setAttribute("message", new Message("Your old password is wrong ! try again..", "danger"));
    		return "redirect:/user/settings";
    	}
    	
    	
    	return "redirect:/user/index";
    }
}
