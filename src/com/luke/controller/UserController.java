package com.luke.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.luke.model.rspnstatus.ResponseStatus;
import com.luke.model.rspnstatus.Failed;
import com.luke.model.rspnstatus.Success;
import com.luke.model.user.UserInfo;
import com.luke.service.UserService;

@Controller
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
    @Autowired
    private UserService userService;
    
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showHomePage() {
        return "/main/index";
    }
    
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showUserPage(ModelMap model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", authentication.getName());
        List<UserInfo> list;
        Collection<GrantedAuthority> authority = (Collection<GrantedAuthority>)authentication.getAuthorities();
        /* ROLE_ADMIN,返回全部用户 */
        for(GrantedAuthority g : authority) {
            if(g.getAuthority().equals("ROLE_ADMIN")) {
                //System.out.print(" "+g.getAuthority());
                list = userService.getAllUsers();
                model.addAttribute("users", list);
                return "/main/users";
            }
        }
        /* ROLE_ROLE,返回ROLE用户 */
        list = userService.getUsersByRoleUser();
        model.addAttribute("users", list);
        //System.out.println("current user is:" + authentication.getName());
        return "/main/users";
    }
    
	/* Add a new user */
    @RequestMapping(value = "/user/add", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseStatus addUser(
			@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password,
			@RequestParam(value = "authority", required = true) String authority) {
        username = username.trim();
        password = password.trim();
        authority = authority.trim();
        try {
        	return userService.addUserAndAuthority(username, password, authority);
        } catch (Exception e) {
        	return new Failed(e.toString());
        }
    }
    
    @RequestMapping(value = "/user/delete", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseStatus deleteUser(@RequestParam(value = "username", required = true) String username) {
    	try {
            username = username.trim();
	    	return userService.deleteUserAndAuthority(username);
    	} catch (Exception e) {
        	return new Failed(e.toString());
        }
    }
    
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseStatus updateUser(
    		@RequestParam(value = "username", required = true) String username,
			@RequestParam(value = "password", required = true) String password,
			@RequestParam(value = "authority", required = true) String authority) {
		try {
	        username = username.trim();
	        password = password.trim();
	        authority = authority.trim();
			return userService.updateUserAndAuthorityByName(username, password, authority);
		} catch (Exception e) {
			return new Failed(e.toString());
		}
    }

    @RequestMapping(value = "/user/changePassword", method = RequestMethod.POST)
    @ResponseBody
    public ResponseStatus changePassword(
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "password", required = true) String password) {
        username = username.trim();
        password = password.trim();
        int n = userService.updatePasswordByName(username, password);
        if(n == 0)
        	return new Failed("non user been changed password");
        else if (n == 1)
        	return new Success();
        else
        	return new Failed("mutipult users been changed password , num:"+n);
    }
    
    @RequestMapping(value = "/user/get")
    @ResponseBody
    public UserInfo getUser(@RequestParam(value = "username", required = true) String username) {
        username = username.trim();
        return userService.getUser(username);
    }
    
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/user/query/all")
    @ResponseBody
    public List<UserInfo> allUsers() {
        return userService.getAllUsers();
    }
    
    /* Spring Security */
    @RequestMapping(value = "/user/whoami")
    @ResponseBody
    public ResponseStatus whoAmI() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<GrantedAuthority> authority = (Collection<GrantedAuthority>)authentication.getAuthorities();
        for(GrantedAuthority g : authority)
        	logger.info(" "+g.getAuthority());

        HashMap<String, Object> identity = new HashMap<String, Object>();
    	identity.put("username", authentication.getName());
    	identity.put("authority", authentication.getAuthorities());
    	identity.put("details", authentication.getDetails());
    	identity.put("principal", authentication.getPrincipal());
    	identity.put("credentials", authentication.getCredentials());
        return new Success(identity);
    }
}