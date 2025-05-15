package com.usfbs.springboot.initializer;

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

    @Value("${quorum.contractAddress.booking:}") // empty string if not defined
    private String bookingContractAddress;

    @Override
    public void run(String... args) throws Exception {
        RawTransactionManager transactionManager = new RawTransactionManager(quorum, credentials, chainId);
        Booking bookingContract;

        if (bookingContractAddress != null && !bookingContractAddress.isEmpty()) {
            // Load existing contract
            bookingContract = Booking.load(bookingContractAddress, quorum, transactionManager, contractGasProvider);
            System.out.println("Booking contract loaded from " + bookingContractAddress);
        } else {
            // Deploy new contract
            String admin = credentials.getAddress();
            bookingContract = Booking.deploy(quorum, transactionManager, contractGasProvider, admin).send();
            System.out.println("Booking deployed at " + bookingContract.getContractAddress() + " remember to update env");

            TransactionReceipt deploymentReceipt = bookingContract.getTransactionReceipt()
                .orElseThrow(() -> new RuntimeException("Deployment receipt not found"));
            System.out.println("Deployment TX hash = " + deploymentReceipt.getTransactionHash());
        }

        // subscribe to Booking.sol events
        bookingContract.bookingCreatedEventFlowable(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST
        ).subscribe(event -> {
            System.out.println(">>> [bookingCreated] event received:");
            System.out.println("    from           = " + event.from);
            System.out.println("    bookingId      = " + event.bookingId);
            System.out.println("    ipfsHash       = " + Numeric.toHexString(event.ipfsHash));
            System.out.println("    sportFacility  = " + event.sportFacility);
            System.out.println("    court          = " + event.court);
            System.out.println("    startTime      = " + event.startTime);
            System.out.println("    endTime        = " + event.endTime);
            System.out.println("    status         = " + event.status);
            System.out.println("    note           = " + event.note);
            System.out.println("    timestamp      = " + event.time);
            System.out.println("\n"); 
        }, error -> {
            System.err.println("Error in bookingCreated subscription: " + error.getMessage());
            error.printStackTrace();
        });

        bookingContract.bookingStatusUpdatedEventFlowable(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST
        ).subscribe(event -> {
            System.out.println(">>> [bookingStatusUpdated] event received:");
            System.out.println("    bookingId      = " + event.bookingId);
            System.out.println("    ipfsHash       = " + Numeric.toHexString(event.ipfsHash));
            System.out.println("    sportFacility  = " + event.sportFacility);
            System.out.println("    court          = " + event.court);
            System.out.println("    startTime      = " + event.startTime);
            System.out.println("    endTime        = " + event.endTime);
            System.out.println("    status         = " + event.status);
            System.out.println("    note           = " + event.note);
            System.out.println("    timestamp      = " + event.time);
            System.out.println("\n"); 
        }, error -> {
            System.err.println("Error in bookingStatusUpdated subscription: " + error.getMessage());
            error.printStackTrace();
        });
    }
}
