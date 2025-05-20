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
    private String rpcUrl;
    @Value("${quorum.moderator}")
    private String moderator;
    @Value("${quorum.admin}")
    private String admin;
    @Value("${quorum.contractAddress.booking}")
    private String bookingAddress;
    @Value("${quorum.contractAddress.sportFacility}")
    private String sportFacilityAddress;

    @Bean
    public Quorum quorum() {
        return Quorum.build(new HttpService(rpcUrl));
    }

    @Bean
    public Credentials credentialModerator() {
        return Credentials.create(moderator);
    }

    @Bean
    public Credentials credentialAdmin() {
        return Credentials.create(admin);
    }

    @Bean
    public Credentials credentialUser(String userAddress) {
        return Credentials.create(userAddress);
    }

    @Bean
    public ContractGasProvider contractGasProvider() {
        return new StaticGasProvider(BigInteger.ZERO, BigInteger.valueOf(4700000));
    }

    @Bean
    public String bookingAddress() {
        return bookingAddress;
    }

    @Bean
    public String sportFacilityAddress() {
        return sportFacilityAddress;
    }

}
