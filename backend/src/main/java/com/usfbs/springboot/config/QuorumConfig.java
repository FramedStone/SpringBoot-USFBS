package com.usfbs.springboot.config;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.tx.RawTransactionManager;
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

    @Value("${quorum.chainId}")
    private long chainId;              

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
    public RawTransactionManager rawTransactionManager(
            Quorum quorum,
            Credentials credentialAdmin
    ) {
        return new RawTransactionManager(quorum, credentialAdmin, chainId);
    }

    @Bean
    public ContractGasProvider contractGasProvider() {
        return new StaticGasProvider(BigInteger.ZERO, BigInteger.valueOf(4_700_000));
    }
}
