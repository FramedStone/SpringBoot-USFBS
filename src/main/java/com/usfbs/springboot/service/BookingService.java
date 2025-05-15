package com.usfbs.springboot.service;

import java.math.BigInteger;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.Quorum;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import com.usfbs.springboot.contracts.Booking;

import jakarta.annotation.PostConstruct;

@Service
public class BookingService {
    @Autowired
    private Quorum quorum;
    
    @Autowired
    private Credentials credentials;

    @Autowired
    private ContractGasProvider contractGasProvider;

    @Value("${quorum.chainId}")
    private long chainId;

    @Value("${quorum.contractAddress.booking}")
    private String bookingContractAddress;

    private Booking bookingContract;

    @PostConstruct
    public void init() {
        RawTransactionManager transactionManager = new RawTransactionManager(quorum, credentials, chainId);

        if(bookingContractAddress == null || bookingContractAddress.isEmpty()) {
            throw new IllegalStateException("No contract address found (Booking).");
        }

        bookingContract = Booking.load(bookingContractAddress, quorum, transactionManager, contractGasProvider);
        System.out.println("Booking contract loaded at: " + bookingContract.getContractAddress()); 

    }

    // create booking
    public BigInteger create_booking(
        byte[] ipfs_hash,
        String sport_facility,
        String court,
        BigInteger start_time,
        BigInteger end_time
    ) throws Exception {
        TransactionReceipt receipt = bookingContract.createBooking(
            ipfs_hash, sport_facility, court, start_time, end_time
        ).send();

        // Parse event logs from receipt
        List<Booking.BookingCreatedEventResponse> events = Booking.getBookingCreatedEvents(receipt);
        if (events.isEmpty()) {
            throw new RuntimeException("No bookingCreated event found in receipt");
        }

        Booking.BookingCreatedEventResponse event = events.get(0); 
        return event.bookingId;
    }


}
