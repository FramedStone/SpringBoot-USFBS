package com.usfbs.springboot.initializer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
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

import com.usfbs.springboot.config.QuorumConfig;
import com.usfbs.springboot.contracts.Booking;
import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.contracts.SportFacility;
import com.usfbs.springboot.util.DateTimeUtil;

@Component
public class ContractInitializer implements CommandLineRunner {
    
    @Autowired
    private Quorum quorum;

    @Autowired
    private Credentials credentialAdmin;

    @Value("${quorum.chainId}")
    private long chainId;

    @Autowired
    private ContractGasProvider contractGasProvider;

    @Value("${quorum.contractAddress.booking:}")
    private String bookingContractAddress;
    @Value("${quorum.contractAddress.sportFacility:}") 
    private String sportFacilityContractAddress;
    @Value("${quorum.contractAddress.management:}") 
    private String managementContractAddress;

    private SportFacility sportFacilityContract;
    private Booking      bookingContract;
    private Management   managementContract;

    /**
     * Safely converts Object to BigInteger for timestamp formatting
     * Handles both BigInteger and String types from contract events
     * Reference: https://docs.web3j.io/4.9.4/smart_contracts/smart_contracts/#solidity-smart-contract-wrappers
     * @param value Object that should be a timestamp
     * @return BigInteger timestamp or null if conversion fails
     */
    private BigInteger safeToBigInteger(Object value) {
        try {
            if (value == null) {
                return null;
            }
            if (value instanceof BigInteger) {
                return (BigInteger) value;
            }
            if (value instanceof String) {
                // Handle string representation of numbers
                String stringValue = (String) value;
                if (stringValue.trim().isEmpty()) {
                    return null;
                }
                return new BigInteger(stringValue);
            }
            if (value instanceof Long) {
                return BigInteger.valueOf((Long) value);
            }
            if (value instanceof Integer) {
                return BigInteger.valueOf((Integer) value);
            }
            // Try string conversion as last resort
            return new BigInteger(value.toString());
        } catch (Exception e) {
            System.err.println("Failed to convert value to BigInteger: " + value + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Safely formats timestamp using DateTimeUtil with type conversion
     * @param timestamp Object that should be a timestamp
     * @return Formatted datetime string
     */
    private String safeFormatTimestamp(Object timestamp) {
        BigInteger bigIntTimestamp = safeToBigInteger(timestamp);
        if (bigIntTimestamp == null) {
            return "N/A";
        }
        return DateTimeUtil.formatTimestamp(bigIntTimestamp);
    }

    /**
     * Safely formats booking time using DateTimeUtil with type conversion
     * @param timeValue Object that should be a booking time
     * @return Formatted booking time string
     */
    private String safeFormatBookingTime(Object timeValue) {
        BigInteger bigIntTime = safeToBigInteger(timeValue);
        if (bigIntTime == null) {
            // If conversion failed, treat as string time format
            return timeValue != null ? timeValue.toString() : "N/A";
        }
        return DateTimeUtil.formatBookingTime(bigIntTime);
    }

    private String safeFormatBookingTime(BigInteger timestamp) {
        try {
            // Use Malaysian timezone formatting
            return DateTimeUtil.formatBookingTime(timestamp);
        } catch (Exception e) {
            return "Invalid timestamp: " + timestamp;
        }
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> ContractInitializer.run() started");
        try {
            RawTransactionManager transactionManager = new RawTransactionManager(quorum, credentialAdmin, chainId);

            // prepare admin list
            String adminAddr = credentialAdmin.getAddress();
            List<String> admins = Collections.singletonList(adminAddr);

            // SportFacility.sol contract initialization
            if (sportFacilityContractAddress != null && !sportFacilityContractAddress.isEmpty()) {
                sportFacilityContract = SportFacility.load(
                  sportFacilityContractAddress, quorum, transactionManager, contractGasProvider);
                System.out.println("SportFacility contract loaded from " + sportFacilityContractAddress);
            } else {
                sportFacilityContract = SportFacility
                  .deploy(quorum, transactionManager, contractGasProvider, admins)
                  .send();
                System.out.println("SportFacility contract deployed at " + sportFacilityContract.getContractAddress() + " remember to update env");

                TransactionReceipt deploymentReceipt = sportFacilityContract.getTransactionReceipt()
                    .orElseThrow(() -> new RuntimeException("Deployment receipt not found"));
                System.out.println("Sport Facility Deployment TX hash = " + deploymentReceipt.getTransactionHash());
            }

            // Booking.sol contract initialization
            if (bookingContractAddress != null && !bookingContractAddress.isEmpty()) {
                bookingContract = Booking.load(
                  bookingContractAddress, quorum, transactionManager, contractGasProvider);
                System.out.println("Booking contract loaded from " + bookingContractAddress);
            } else {
                bookingContract = Booking
                  .deploy(
                    quorum,
                    transactionManager,
                    contractGasProvider,
                    admins,
                    sportFacilityContract.getContractAddress()
                  )
                  .send();
                System.out.println("Booking contract deployed at " + bookingContract.getContractAddress() + " remember to update env");

                TransactionReceipt deploymentReceipt = bookingContract.getTransactionReceipt()
                    .orElseThrow(() -> new RuntimeException("Deployment receipt not found"));
                System.out.println("Booking Deployment TX hash = " + deploymentReceipt.getTransactionHash());
            }

            // Management.sol contract initialization
            if (managementContractAddress != null && !managementContractAddress.isEmpty()) {
                managementContract = Management.load(
                  managementContractAddress, quorum, transactionManager, contractGasProvider);
                System.out.println("Management contract loaded from " + managementContractAddress);
            } else {
                managementContract = Management
                  .deploy(quorum, transactionManager, contractGasProvider, admins)
                  .send();
                System.out.println("Management contract deployed at " + managementContract.getContractAddress() + " remember to update env");

                TransactionReceipt deploymentReceipt = managementContract.getTransactionReceipt()
                    .orElseThrow(() -> new RuntimeException("Deployment receipt not found"));
                System.out.println("Management Deployment TX hash = " + deploymentReceipt.getTransactionHash());
            }

            // Subscribe to Booking.sol events with formatted timestamps
            bookingContract.bookingCreatedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [bookingCreated] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    bookingId      = " + event.bookingId);
                System.out.println("    ipfsHash       = " + event.ipfsHash);
                System.out.println("    facilityName   = " + event.facilityName);
                System.out.println("    courtName      = " + event.courtName);
                System.out.println("    note           = " + event.note);
                System.out.println("    startTime      = " + safeFormatBookingTime(event.startTime));
                System.out.println("    endTime        = " + safeFormatBookingTime(event.endTime));
                System.out.println("    status         = " + event.status);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in bookingCreated subscription: " + error.getMessage());
                error.printStackTrace();
            });

            bookingContract.bookingUpdatedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [bookingUpdated] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    bookingId      = " + event.bookingId);
                System.out.println("    oldData        = " + event.oldData);
                System.out.println("    newData        = " + event.newData);
                System.out.println("    note           = " + event.note);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in bookingUpdated subscription: " + error.getMessage());
                error.printStackTrace();
            });

            bookingContract.bookingDeletedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [bookingDeleted] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    bookingId      = " + event.bookingId);
                System.out.println("    ipfsHash       = " + event.ipfsHash);
                System.out.println("    status         = " + event.status);
                System.out.println("    note           = " + event.note);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in bookingDeleted subscription: " + error.getMessage());
                error.printStackTrace();
            });

            bookingContract.bookingRequestedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [bookingRequested] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    bookingId      = " + event.bookingId);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in bookingRequested subscription: " + error.getMessage());
                error.printStackTrace();
            });

            // Subscribe to SportFacility.sol events with formatted timestamps
            sportFacilityContract.sportFacilityAddedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [sportFacilityAdded] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    facilityName   = " + event.facilityName);
                System.out.println("    Location       = " + event.Location);
                System.out.println("    status         = " + event.status);
                System.out.println("    courts         = " + event.courts); 
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in sportFacilityAdded subscription: " + error.getMessage());
                error.printStackTrace();
            });

            sportFacilityContract.sportFacilityModifiedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [sportFacilityModified] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    facilityName   = " + event.facilityName);
                System.out.println("    oldData        = " + event.oldData);
                System.out.println("    newData        = " + event.newData);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in sportFacilityModified subscription: " + error.getMessage());
                error.printStackTrace();
            });

            sportFacilityContract.sportFacilityDeletedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [sportFacilityDeleted] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    facilityName   = " + event.facilityName);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in sportFacilityDeleted subscription: " + error.getMessage());
                error.printStackTrace();
            });

            sportFacilityContract.courtAddedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [courtAdded] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    courtName      = " + event.courtName);
                System.out.println("    earliestTime   = " + safeFormatBookingTime(event.earliestTime));
                System.out.println("    latestTime     = " + safeFormatBookingTime(event.latestTime));
                System.out.println("    status         = " + event.status);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in courtAdded subscription: " + error.getMessage());
                error.printStackTrace();
            });

            sportFacilityContract.courtModifiedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [courtModified] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    facilityName   = " + event.facilityName);
                System.out.println("    courtName      = " + event.courtName);
                System.out.println("    oldData        = " + event.oldData);
                System.out.println("    newData        = " + event.newData);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in courtModified subscription: " + error.getMessage());
                error.printStackTrace();
            });

            sportFacilityContract.courtDeletedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [courtDeleted] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    courtName      = " + event.courtName);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in courtDeleted subscription: " + error.getMessage());
                error.printStackTrace();
            });

            sportFacilityContract.facilityDetailsRequestedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [facilityDetailsRequested] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    facilityName   = " + event.facilityName);
                System.out.println("    note           = " + event.note);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in facilityDetailsRequested subscription: " + error.getMessage());
                error.printStackTrace();
            });

            sportFacilityContract.courtDetailsRequestedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [courtDetailsRequested] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    facilityName   = " + event.facilityName);
                System.out.println("    courtName      = " + event.courtName);
                System.out.println("    note           = " + event.note);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in courtDetailsRequested subscription: " + error.getMessage());
                error.printStackTrace();
            });

            // Subscribe to Management.sol events with formatted timestamps
            managementContract.userAddedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [userAdded] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    user           = " + event.user);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in userAdded subscription: " + error.getMessage());
                error.printStackTrace();
            });

            managementContract.userBannedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [userBanned] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    user           = " + event.user);
                System.out.println("    note           = " + event.note);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in userBanned subscription: " + error.getMessage());
                error.printStackTrace();
            });

            managementContract.userUnbannedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [userUnbanned] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    user           = " + event.user);
                System.out.println("    note           = " + event.note);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in userUnbanned subscription: " + error.getMessage());
                error.printStackTrace();
            });

            managementContract.announcementAddedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [announcementAdded] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    ipfsHash       = " + event.ipfsHash);
                System.out.println("    startTime      = " + safeFormatBookingTime(event.startTime));
                System.out.println("    endTime        = " + safeFormatBookingTime(event.endTime));
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in announcementAdded subscription: " + error.getMessage());
                error.printStackTrace();
            });

            managementContract.announcementIpfsHashModifiedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [announcementIpfsHashModified] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    oldIpfsHash    = " + event.ipfsHash_);
                System.out.println("    newIpfsHash    = " + event.ipfsHash);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in announcementIpfsHashModified subscription: " + error.getMessage());
                error.printStackTrace();
            });

            managementContract.announcementTimeModifiedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [announcementTimeModified] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    ipfsHash       = " + event.ipfsHash);
                System.out.println("    oldStartTime   = " + safeFormatBookingTime(event.startTime_));
                System.out.println("    oldEndTime     = " + safeFormatBookingTime(event.endTime_));
                System.out.println("    newStartTime   = " + safeFormatBookingTime(event.startTime));
                System.out.println("    newEndTime     = " + safeFormatBookingTime(event.endTime));
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in announcementTimeModified subscription: " + error.getMessage());
                error.printStackTrace();
            });

            managementContract.announcementDeletedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [announcementDeleted] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    ipfsHash       = " + event.ipfsHash);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in announcementDeleted subscription: " + error.getMessage());
                error.printStackTrace();
            });

            managementContract.announcementRequestedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ).subscribe(event -> {
                System.out.println(">>> [announcementRequested] event received:");
                System.out.println("    from           = " + event.from);
                System.out.println("    ipfsHash       = " + event.ipfsHash);
                System.out.println("    timestamp      = " + safeFormatTimestamp(event.timestamp));
                System.out.println();
            }, error -> {
                System.err.println("Error in announcementRequested subscription: " + error.getMessage());
                error.printStackTrace();
            });

            System.out.println(">>> ContractInitializer.run() completed successfully");
            System.out.println(">>> All smart contract event subscriptions are active");
            System.out.println(">>> Timezone: " + ZoneId.systemDefault());
            
        } catch (Exception e) {
            System.err.println(">>> ContractInitializer.run() failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
