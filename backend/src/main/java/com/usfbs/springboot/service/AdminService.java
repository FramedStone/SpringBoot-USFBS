package com.usfbs.springboot.service;

import com.usfbs.springboot.contracts.Management;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final Management managementContract;

    @Autowired
    public AdminService(Management managementContract) {
        this.managementContract = managementContract;
    }

    public void addUser(String userAddress) throws Exception {
        managementContract.addUser(userAddress).send();
    }

    public void banUser(String userAddress) throws Exception {
        managementContract.banUser(userAddress).send();
    }

    public void unbanUser(String userAddress) throws Exception {
        managementContract.unbanUser(userAddress).send();
    }
}
