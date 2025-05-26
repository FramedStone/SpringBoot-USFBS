package com.usfbs.springboot.config;

import com.usfbs.springboot.contracts.Management;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.quorum.Quorum;
import org.web3j.tx.gas.ContractGasProvider;

@Configuration
public class ManagementContractConfig {

    @Value("${quorum.contractAddress.management}")
    private String managementContractAddress;

    @Bean
    public Management managementContract(
            Quorum quorum,
            Credentials credentialAdmin,
            ContractGasProvider contractGasProvider
    ) {
        return Management.load(
            managementContractAddress,
            quorum,
            credentialAdmin,
            contractGasProvider
        );
    }
}