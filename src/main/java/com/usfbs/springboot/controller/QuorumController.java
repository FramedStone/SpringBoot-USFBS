package com.usfbs.springboot.controller;

import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;

import com.usfbs.springboot.service.QuorumService;

import java.io.IOException;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class QuorumController {
   private final QuorumService quorum;
   
   public QuorumController(QuorumService quorum) {
    this.quorum = quorum;
   }

   @GetMapping("/client-version")
   public String getClientVersion() throws IOException {
        Web3ClientVersion version = quorum.getQuorum().web3ClientVersion().send();
        return version.getWeb3ClientVersion();
   }
   
}
