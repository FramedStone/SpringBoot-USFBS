package com.usfbs.springboot.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;

import jakarta.annotation.PostConstruct;

@Service
public class QuorumService {
    private final Quorum quorum;
    // private final Credentials credentials;
    // private final long chainId;

    public QuorumService() {
        this.quorum = Quorum.build(new HttpService("http:localhost:22000"));
        // this.credentials = Credentials.create(privateKey);
        // this.chainId = chainId;
    }

    // Check connection after constructor
    @PostConstruct
    private void init() {
        try {
            Web3ClientVersion clientVersion = quorum.web3ClientVersion().send();
            System.out.println("Connected to Quorum client version: " + clientVersion.getWeb3ClientVersion());
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Quorum client", e);
        }
    }

    public Quorum getQuorum() {
        return quorum;
    }

    // public Credentials getCredentials() {
    //     return credentials;
    // }

    // public long getChainId() {
    //     return chainId;
    // }
}
