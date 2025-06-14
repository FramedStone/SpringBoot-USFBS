package com.usfbs.springboot.config;

import com.usfbs.springboot.contracts.SportFacility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.quorum.Quorum;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

@Configuration
public class SportFacilityContractConfig {

    @Value("${quorum.contractAddress.sportFacility}")
    private String sportFacilityContractAddress;

    @Bean
    public SportFacility sportFacility(
            Quorum quorum,
            RawTransactionManager rawTransactionManager,
            ContractGasProvider contractGasProvider
    ) {
        return SportFacility.load(
            sportFacilityContractAddress,
            quorum,
            rawTransactionManager,
            contractGasProvider
        );
    }
}