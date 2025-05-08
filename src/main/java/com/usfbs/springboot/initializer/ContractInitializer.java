package com.usfbs.springboot.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.enclave.Enclave;
import org.web3j.quorum.tx.QuorumTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import com.usfbs.springboot.contracts.Booking;


@Component
public class ContractInitializer implements CommandLineRunner {
    @Autowired
    private Quorum quorum;

    @Value("${quorum.privateKey}")
    private Enclave privateKey;

    @Autowired
    private Credentials credentials;

    @Autowired
    private ContractGasProvider contractGasProvider;

    @Override
    public void run(String... args) throws Exception {
        QuorumTransactionManager transactionManager = new QuorumTransactionManager(quorum, privateKey, credentials, null, null);

        Booking booking = Booking.deploy(quorum, transactionManager, contractGasProvider, null).send();
        System.out.println("Smart Contract (Booking) deployed at " + booking.getContractAddress());
    }
}
