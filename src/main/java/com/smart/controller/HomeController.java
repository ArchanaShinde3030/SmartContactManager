package com.smart.controller;
import javax.servlet.http.HttpSession;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;



//import jakarta.servlet.http.HttpSession;
//import jakarta.validation.Valid;

@Controller
public class HomeController {

    @Autowired
	private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder  passwordEncoder;
    
	//Home handler
	@GetMapping("/")
	public String home( Model model) {
		
		model.addAttribute("title","Home-smart contact manager");
		return "home";
	}
	//about handler
	@GetMapping("/about")
	public String about( Model model) {
		
		model.addAttribute("title","About-smart contact manager");
		return "about";
	}
	
	//signup handler
	@GetMapping("/signup")
	public String signup( Model model) {
		
		model.addAttribute("title","Register-smart contact manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	//handler for registering user
	@RequestMapping( value="/do-register", method=RequestMethod.POST)
	public String registerUser( @Valid @ModelAttribute("user") User user ,@RequestParam(value="agrement",defaultValue="false")boolean agrement,Model model,
		BindingResult resultt,HttpSession session) {
		
		try {
			if(!agrement) {
				System.out.println("you are not agreed terms and condition!!");
				
				throw new Exception("you are not agreed terms and condition!!");
			}
			if(resultt.hasErrors()) {
				System.out.println("Error:"+resultt.toString());
				model.addAttribute("user", user);
				 return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImgUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("Agrement "+ agrement);
			System.out.println("User "+ user);
			User result=this.userRepository.save(user);
			model.addAttribute("user",result);
			
			session.setAttribute("message",new Message("Successfully Registared!!","alert-Success"));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message",new Message("somthing went wrong!!"+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
	
	//handler for custom login page
	@GetMapping("/login")
	public String customLogin(Model model) {
		model.addAttribute("title","Login page");
		return "login";
	}
}
