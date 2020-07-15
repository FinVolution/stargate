package com.ppdai.stargate.service;

import com.ppdai.atlas.client.model.UserDto;
import com.ppdai.stargate.remote.RemoteCmdb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private RemoteCmdb remoteCmdb;

    public List<UserDto> findUsersByUserName(String userName) {
        return remoteCmdb.searchUsersByUserName(userName);
    }
}
