package com.usfbs.springboot.configuration;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

@Configuration
public class QuorumConfig {
    @Value("${quorum.url}")
    private String quorumUrl;
    @Value("${quorum.privateKey}")
    private String privateKey;
    // @Value("${quorum.chainId}")
    // private long chainId;

    @Bean
    public Quorum quorum() {
        return Quorum.build(new HttpService(quorumUrl));
    }

    // @Bean
    // public long chainId() {
    //     return chainId;
    // }

    @Bean
    public Credentials credentials() {
        return Credentials.create(privateKey);
    }

    @Bean
    public ContractGasProvider contractGasProvider() {
        return new StaticGasProvider(BigInteger.ZERO, BigInteger.valueOf(4700000));
    }

    // @Bean
    // TODO: Booking contract

    // @Bean
    // TODO: Facility contract

    // @Bean
    // TODO: Management contract
}
