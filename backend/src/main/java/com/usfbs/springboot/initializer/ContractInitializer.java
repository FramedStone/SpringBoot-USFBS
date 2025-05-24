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

import com.usfbs.springboot.contracts.Booking;
import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.contracts.SportFacility;

@Component
public class ContractInitializer implements CommandLineRunner {
    @Autowired
    private Quorum quorum;

    // @Autowired
    // private Credentials credentialModerator;
    @Autowired
    private Credentials credentialAdmin;
    // @Autowired
    // private Credentials credentialUser;

    @Value("${quorum.chainId}")
    private long chainId;

    @Autowired
    private ContractGasProvider contractGasProvider;

    @Value("${quorum.contractAddress.booking:}") // empty string if not defined
    private String bookingContractAddress;
    @Value("${quorum.contractAddress.sportFacility:}") 
    private String sportFacilityContractAddress;
    @Value("${quorum.contractAddress.management:}") 
    private String managementContractAddress;

    @Override
    public void run(String... args) throws Exception {
        RawTransactionManager transactionManager = new RawTransactionManager(quorum, credentialAdmin, chainId);
        Booking bookingContract;
        SportFacility sportFacilityContract;
        Management managementContract;

        // SportFacility.sol
        if (sportFacilityContractAddress != null && !sportFacilityContractAddress.isEmpty()) {
            // Load existing contract
            sportFacilityContract = SportFacility.load(sportFacilityContractAddress, quorum, transactionManager, contractGasProvider);
            System.out.println("SportFacility contract loaded from " + sportFacilityContractAddress);
        } else {
            // Deploy new contract
            String admin = credentialAdmin.getAddress();
            sportFacilityContract = SportFacility.deploy(quorum, transactionManager, contractGasProvider, admin).send();
            System.out.println("SportFacility contract deployed at " + sportFacilityContract.getContractAddress() + " remember to update env");

            TransactionReceipt deploymentReceipt = sportFacilityContract.getTransactionReceipt()
                .orElseThrow(() -> new RuntimeException("Deployment receipt not found"));
            System.out.println("Sport Facility Deployment TX hash = " + deploymentReceipt.getTransactionHash());
        }

        // Booking.sol
        if (bookingContractAddress != null && !bookingContractAddress.isEmpty()) {
            // Load existing contract
            bookingContract = Booking.load(bookingContractAddress, quorum, transactionManager, contractGasProvider);
            System.out.println("Booking contract loaded from " + bookingContractAddress);
        } else {
            // Deploy new contract
            String admin = credentialAdmin.getAddress();
            bookingContract = Booking.deploy(quorum, transactionManager, contractGasProvider, admin, sportFacilityContract.getContractAddress()).send();
            System.out.println("Booking contract deployed at " + bookingContract.getContractAddress() + " remember to update env");

            TransactionReceipt deploymentReceipt = bookingContract.getTransactionReceipt()
                .orElseThrow(() -> new RuntimeException("Deployment receipt not found"));
            System.out.println("Booking Deployment TX hash = " + deploymentReceipt.getTransactionHash());
        }

        // Management.sol
        if (managementContractAddress != null && !managementContractAddress.isEmpty()) {
            // Load existing contract
            managementContract = Management.load(managementContractAddress, quorum, transactionManager, contractGasProvider);
            System.out.println("Management contract loaded from " + managementContractAddress);
        } else {
            // Deploy new contract
            String admin = credentialAdmin.getAddress();
            managementContract = Management.deploy(quorum, transactionManager, contractGasProvider, admin).send();
            System.out.println("Management contract deployed at " + managementContract.getContractAddress() + " remember to update env");

            TransactionReceipt deploymentReceipt = managementContract.getTransactionReceipt()
                .orElseThrow(() -> new RuntimeException("Deployment receipt not found"));
            System.out.println("Management Deployment TX hash = " + deploymentReceipt.getTransactionHash());
        }

        // subscribe to Booking.sol events
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
            System.out.println("    startTime      = " + event.startTime);
            System.out.println("    endTime        = " + event.endTime);
            System.out.println("    status         = " + event.status);
            System.out.println("    timestamp      = " + event.timestamp);
            System.out.println("\n"); 
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
            System.out.println("    timestamp      = " + event.timestamp);
            System.out.println("\n"); 
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
            System.out.println();
        }, error -> {
            System.err.println("Error in bookingRequested subscription: " + error.getMessage());
            error.printStackTrace();
        });

        bookingContract.timeSlotRequestedEventFlowable(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST
        ).subscribe(event -> {
            System.out.println(">>> [timeSlotRequested] event received:");
            System.out.println("    from           = " + event.from);
            System.out.println("    facilityName   = " + event.facilityName);
            System.out.println("    courtName      = " + event.courtName);
            System.out.println("    timestamp      = " + event.timestamp);
            System.out.println();
        }, error -> {
            System.err.println("Error in timeSlotRequested subscription: " + error.getMessage());
            error.printStackTrace();
        });

        // subscribe to SportFacility.sol events
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    earliestTime   = " + event.earliestTime);
            System.out.println("    latestTime     = " + event.latestTime);
            System.out.println("    status         = " + event.status);
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
            System.out.println();
        }, error -> {
            System.err.println("Error in courtDetailsRequested subscription: " + error.getMessage());
            error.printStackTrace();
        });

        // subscribe to Management.sol events
        managementContract.userAddedEventFlowable(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST
        ).subscribe(event -> {
            System.out.println(">>> [userAdded] event received:");
            System.out.println("    from           = " + event.from);
            System.out.println("    user           = " + event.user);
            System.out.println("    timestamp      = " + event.timestamp);
            System.out.println();
        }, error -> {
            System.err.println("Error in userAdded subscription: " + error.getMessage());
            error.printStackTrace();
        });

        managementContract.userBannedEventFlowable(
            DefaultBlockParameterName.EARLIEST,
            DefaultBlockParameterName.LATEST
        ).subscribe(event -> {
            System.out.println(">>> [userDeleted] event received:");
            System.out.println("    from           = " + event.from);
            System.out.println("    user           = " + event.user);
            System.out.println("    note           = " + event.note);
            System.out.println("    timestamp      = " + event.timestamp);
            System.out.println();
        }, error -> {
            System.err.println("Error in userDeleted subscription: " + error.getMessage());
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    startTime      = " + event.startTime);
            System.out.println("    endTime        = " + event.endTime);
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    ipfsHash_      = " + event.ipfsHash_);
            System.out.println("    ipfsHash       = " + event.ipfsHash);
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    startTime_     = " + event.startTime_);
            System.out.println("    endTime_       = " + event.endTime_);
            System.out.println("    startTime      = " + event.startTime);
            System.out.println("    endTime        = " + event.endTime);
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
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
            System.out.println("    timestamp      = " + event.timestamp);
            System.out.println();
        }, error -> {
            System.err.println("Error in announcementRequested subscription: " + error.getMessage());
            error.printStackTrace();
        });
    }
}
