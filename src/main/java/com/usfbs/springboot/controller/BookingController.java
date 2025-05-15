package com.usfbs.springboot.controller;

import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.web3j.utils.Numeric;

import com.usfbs.springboot.service.BookingService;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping
    public BigInteger create_booking(@RequestBody CreateBookingRequest req) throws Exception {
        byte[] ipfs_bytes = Numeric.hexStringToByteArray(req.ipfs_hash);
        if (ipfs_bytes.length != 32) {
            throw new IllegalArgumentException("IPFS hash must be 32 bytes");
        }

        return bookingService.create_booking(
            ipfs_bytes,
            req.sport_facility,
            req.court,
            req.start_time,
            req.end_time
        );
    }

    public static class CreateBookingRequest {
        public String ipfs_hash;
        public String sport_facility;
        public String court;
        public BigInteger start_time;
        public BigInteger end_time;
    }
}