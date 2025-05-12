package com.usfbs.springboot.initializer;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameterName;            
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.Quorum;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

import com.usfbs.springboot.contracts.Booking;

@Component
public class ContractInitializer implements CommandLineRunner {
    @Autowired
    private Quorum quorum;

    @Value("${quorum.privateKey}")
    private String privateKey;

    @Autowired
    private Credentials credentials;

    @Value("${quorum.chainId}")
    private long chainId;

    @Autowired
    private ContractGasProvider contractGasProvider;

    @Override
    public void run(String... args) throws Exception {
        // Deployment 
        RawTransactionManager transactionManager = new RawTransactionManager(quorum, credentials, chainId);
        String admin = credentials.getAddress();

        Booking bookingContract = Booking.deploy(
            quorum, transactionManager, contractGasProvider, admin
        ).send();
        System.out.println("Booking deployed at " + bookingContract.getContractAddress());

        TransactionReceipt deploymentReceipt = bookingContract.getTransactionReceipt()
            .orElseThrow(() -> new RuntimeException("Deployment receipt not found"));
        System.out.println("Deployment TX hash = " + deploymentReceipt.getTransactionHash());

        // Example createBooking function call 
        byte[] ipfsHashBytes = Numeric.hexStringToByteArray(
            "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"
        );
        if (ipfsHashBytes.length != 32) {
            throw new IllegalArgumentException("IPFS hash must be 32 bytes");
        }
        TransactionReceipt createReceipt = bookingContract.createBooking(
            ipfsHashBytes,
            "Tennis Court A",
            "Court 1",
            BigInteger.valueOf(1698765600L),
            BigInteger.valueOf(1698772800L)
        ).send();
        System.out.println("createBooking TX hash = " + createReceipt.getTransactionHash());

        // Events Listener/subscriber
        bookingContract.bookingCreatedEventFlowable(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST
        ).subscribe(event -> {
            // event bookingCreated(
            //     address indexed from,
            //     uint256 bookingId,
            //     bytes32 ipfsHash,
            //     string sportFacility,
            //     string court,
            //     uint256 startTime,
            //     uint256 endTime,
            //     string status,
            //     string note,
            //     uint256 time
            // );
            System.out.println(">>> bookingCreated event:");
            System.out.println("    from           = " + event.from);
            System.out.println("    bookingId      = " + event.bookingId);
            System.out.println("    ipfsHash       = " + event.ipfsHash);
            System.out.println("    sportFacility  = " + event.sportFacility);
            System.out.println("    court          = " + event.court);
            System.out.println("    start          = " + event.startTime);
            System.out.println("    end            = " + event.endTime);
            System.out.println("    status         = " + event.status);
            System.out.println("    note           = " + event.note);
            System.out.println("    timestamp      = " + event.time);
        }, error -> {
            System.err.println("Error in event subscription: " + error.getMessage());
            error.printStackTrace();
        });

        bookingContract.bookingStatusUpdatedEventFlowable(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST
        ).subscribe(event -> {
            // event bookingStatusUpdated(
            //     uint256 bookingId,
            //     bytes32 ipfsHash,
            //     string sportFacility,
            //     string court,
            //     uint256 startTime,
            //     uint256 endTime,
            //     string status,
            //     string note,
            //     uint256 time
            // );
            System.out.println(">>> bookingCreated event:");
            System.out.println("    bookingId      = " + event.bookingId);
            System.out.println("    ipfsHash       = " + event.ipfsHash);
            System.out.println("    sportFacility  = " + event.sportFacility);
            System.out.println("    court          = " + event.court);
            System.out.println("    start          = " + event.startTime);
            System.out.println("    end            = " + event.endTime);
            System.out.println("    status         = " + event.status);
            System.out.println("    note           = " + event.note);
            System.out.println("    timestamp      = " + event.time);
        }, error -> {
            System.err.println("Error in event subscription: " + error.getMessage());
            error.printStackTrace();
        });
    }
}
