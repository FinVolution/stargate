package com.ppdai.stargate.controller;

import com.ppdai.atlas.client.model.UserDto;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET)
    public Response<List<UserDto>> getUsersByUserName(@RequestParam(value = "name") String userName) {
        List<UserDto> userDtos = userService.findUsersByUserName(userName);
        return Response.success(userDtos);
    }
}
