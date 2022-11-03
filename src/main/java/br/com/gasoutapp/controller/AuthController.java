package br.com.gasoutapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.gasoutapp.dto.LoginDTO;
import br.com.gasoutapp.security.LoginResultDTO;
import br.com.gasoutapp.service.UserService;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public LoginResultDTO login(@RequestBody LoginDTO dto) throws Exception {
        return userService.login(dto.getLogin(), dto.getPassword());
    }

    @RequestMapping(path = {
            "/check-admin" }, method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String checkIfAdminExists() {
        return userService.checkIfAdminExists();
    }
}