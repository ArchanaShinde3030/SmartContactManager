package com.smart.controller;

import com.smart.SmartcontactmanagerApplication;
import com.smart.config.MyConfig;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    private final DaoAuthenticationProvider authenticationProvider;

    private final MyConfig myConfig;

    private final HomeController homeController;

    private final BCryptPasswordEncoder passwordEncoder;

    private final SmartcontactmanagerApplication smartcontactmanagerApplication;


	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;

    UserController(SmartcontactmanagerApplication smartcontactmanagerApplication, BCryptPasswordEncoder passwordEncoder, HomeController homeController, MyConfig myConfig, DaoAuthenticationProvider authenticationProvider) {
        this.smartcontactmanagerApplication = smartcontactmanagerApplication;
        this.passwordEncoder = passwordEncoder;
        this.homeController = homeController;
        this.myConfig = myConfig;
        this.authenticationProvider = authenticationProvider;
    }
	
    //method for adding common data to response 
    @ModelAttribute
    public void addCommonData(Model model,Principal principal) {
    	String username=principal.getName();
    	System.out.println("USERNAME: "+username);
    	//get the user using username(email)
    		
    		User user=userRepository.getUserByUserName(username);
    		System.out.println("User: "+user);
    		model.addAttribute("user",user);
    }
	
    //dashboard home 
    @GetMapping("/index")
	public String dashboard (Model model ,Principal principal ){
		
    	model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
		
	}
    //handler for open add from
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
    	
    	model.addAttribute("title", "Add contact");
    	
    	model.addAttribute("contact", new Contact());
    	
    	return "normal/add_contact_form";
    }
    
    //processing add contact
    
    @PostMapping("/process-contact")
    
    public String processContact(@ModelAttribute Contact contact,
    		@RequestParam("profileImage") MultipartFile file,
    		Principal principal,HttpSession session) {
    	
    	try {
    	String name=principal.getName();
    	
    	User user=this.userRepository.getUserByUserName(name);
    	
    	
    	//processing and uploading file...
    	if(file.isEmpty()) {
    		
    		//if the file is empty then try our messages
    		System.out.println("file is empty");
    		contact.setImage("contact.png");
    	}else {
    		//file the file to folder and update the name to contact
    		contact.setImage(file.getOriginalFilename());
    		File savefile=new ClassPathResource("static/img").getFile();
    		
    		Path path=Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
    		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    		System.out.println("image is uploaded");
    	}
    	
    	contact.setUser(user);
    	
    	user.getContact().add(contact);
    	
    	
    	this.userRepository.save(user);
    	
    	System.out.println("Data: "+ contact);
    	
    	System.out.println("Added to database");
    	
    	//message success....
    	session.setAttribute("message",new Message("your Contact Added !! Add more..", "success"));
    	
    	}catch(Exception e) {
    		System.out.println("Error: "+e.getMessage());
    		e.printStackTrace();
    		
    		//error message
    		session.setAttribute("message",new Message("Something went wrong !! try again..", "danger"));
    	}
    	return "normal/add_contact_form";
    }
    //handler for show contacts
    //par page =5[n]
    //current page=0[page]
    //
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page,Model m,Principal principal) {
    	m.addAttribute("title","Show User Contacts");
    	//contact ki list ko bhejni hai
    	
    	String userName=principal.getName();
		User user=this.userRepository.getUserByUserName(userName);
		
		//currentpage page
		//contact per page=5
		Pageable pageable=PageRequest.of(page, 5);
		
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentpage",page);
		m.addAttribute("totalpages",contacts.getTotalPages());
    	
    	return "normal/show_contacts";
    }
    
    //showing particular contacts details
    @GetMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) {
    	System.out.println("cId "+ cId);
    	Optional<Contact> contactOptional=this.contactRepository.findById(cId);
    	Contact contact=contactOptional.get();
    	
    	//
    String userName	=principal.getName();
    User user=this.userRepository.getUserByUserName(userName);
    
    	
    	if(user.getId()==contact.getUser().getId()) {
    		
    		model.addAttribute("contact",contact);
    		model.addAttribute("title",contact.getName());
    	}
    	return "normal/contact_detail";
    }
    //Delete contact handler
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") Integer cId,Model model,HttpSession session) {
    	Contact contact=this.contactRepository.findById(cId).get();
    	
    	//check... 
    	System.out.println("contact: "+contact.getcId());
    	
    	contact.setUser(null);
    	this.contactRepository.delete(contact);
    	
    	
    	
    	System.out.println("DELETED");
    	session.setAttribute("message", new Message("Contact Deleted Successfully....","success"));
    	return "redirect:/user/show-contacts/0";
    	
    }
    
    // open update form handler
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId,Model model) {
    	
    	model.addAttribute("title","Update Contact");
    	
    	Contact contact=this.contactRepository.findById(cId).get();

    	model.addAttribute("contact",contact);
    	return "normal/update_form";
    }
    
    //update process handler
    @RequestMapping(value = "/process-update",method =RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,
    		Model model,HttpSession session,Principal principal) {
    	try {
    		
    		Contact oldContactDetails=this.contactRepository.findById(contact.getcId()).get();
			//image....
    		if(!file.isEmpty()) {
    			
    			//file work
    			//rewrite
    			//delete old photo
    			File deletefile=new ClassPathResource("static/img").getFile();
    			File file1=new File(deletefile,oldContactDetails.getImage());
    			file1.delete();
    			//update new photo
    			
    			File savefile=new ClassPathResource("static/img").getFile();
        		
        		Path path=Paths.get(savefile.getAbsolutePath()+File.separator+file.getOriginalFilename());
        		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        		contact.setImage(file.getOriginalFilename());
    			
    		}else {
    			contact.setImage(oldContactDetails.getImage());
    		}
    		User user=this.userRepository.getUserByUserName(principal.getName());
    		contact.setUser(user);
    		this.contactRepository.save(contact);
    		session.setAttribute("message", new Message("Your contact is updated....","success"));
    		
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	String name=contact.getName();
    	System.out.println("name: "+ name);
    	System.out.println("Id: "+contact.getcId());
    	return "redirect:/user/"+contact.getcId()+"/contact";
    	
    	
    }
    //handler for serch contact
    @GetMapping("/search")
    public String searchContacts(@RequestParam("query") String query, Model model, Principal principal) {
        String username = principal.getName();
        User user = this.userRepository.getUserByUserName(username);

        List<Contact> contacts = this.contactRepository.searchContacts(query.toLowerCase(), user.getId());

        model.addAttribute("contacts", contacts);
        model.addAttribute("title", "Search Result");
        model.addAttribute("currentpage", 0);
        model.addAttribute("totalpages", 1); // assuming no pagination for search

        return "normal/show_contacts";
    }

    //handler for my profile
    @GetMapping("/profile")
    public String myProfile() {
    	
    	return "normal/my-profile";
    	
    }
    
    }
