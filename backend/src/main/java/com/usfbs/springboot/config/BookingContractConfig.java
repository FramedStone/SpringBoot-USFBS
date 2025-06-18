package com.usfbs.springboot.config;

import com.usfbs.springboot.contracts.Booking;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.quorum.Quorum;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

@Configuration
public class BookingContractConfig {

    @Value("${quorum.contractAddress.booking}")
    private String bookingContractAddress;

    @Bean
    public Booking booking(
            Quorum quorum,
            RawTransactionManager rawTransactionManager,
            ContractGasProvider contractGasProvider
    ) {
        return Booking.load(
            bookingContractAddress,
            quorum,
            rawTransactionManager,
            contractGasProvider
        );
    }
}