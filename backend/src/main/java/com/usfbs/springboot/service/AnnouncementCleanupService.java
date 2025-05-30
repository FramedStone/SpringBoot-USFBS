package com.usfbs.springboot.service;

import com.usfbs.springboot.contracts.Management;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementCleanupService {

    private final Management managementContract;

    public AnnouncementCleanupService(Management managementContract) {
        this.managementContract = managementContract;
    }

    // Runs every day at 12:00am server time
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredAnnouncements() {
        try {
            var contractAddress = managementContract.getContractAddress();
            var startBlock = org.web3j.protocol.core.DefaultBlockParameterName.EARLIEST;
            var endBlock = org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

            // Ensure .toList().blockingGet() is used to get a List
            List<Management.AnnouncementAddedEventResponse> addedEvents =
                managementContract.announcementAddedEventFlowable(
                    new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress)
                ).toList().blockingGet();

            List<Management.AnnouncementIpfsHashModifiedEventResponse> ipfsModifiedEvents =
                managementContract.announcementIpfsHashModifiedEventFlowable(
                    new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress)
                ).toList().blockingGet();

            List<Management.AnnouncementTimeModifiedEventResponse> timeModifiedEvents =
                managementContract.announcementTimeModifiedEventFlowable(
                    new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress)
                ).toList().blockingGet();

            List<Management.AnnouncementDeletedEventResponse> deletedEvents =
                managementContract.announcementDeletedEventFlowable(
                    new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress)
                ).toList().blockingGet();

            // Reconstruct state
            HashMap<String, AnnouncementState> announcementMap = new HashMap<>();
            for (var event : addedEvents) {
                announcementMap.put(event.ipfsHash, new AnnouncementState(
                    event.ipfsHash,
                    event.startTime.longValue(),
                    event.endTime.longValue()
                ));
            }
            for (var event : ipfsModifiedEvents) {
                AnnouncementState state = announcementMap.remove(event.ipfsHash_);
                if (state != null) {
                    state.ipfsHash = event.ipfsHash;
                    announcementMap.put(event.ipfsHash, state);
                }
            }
            for (var event : timeModifiedEvents) {
                AnnouncementState state = announcementMap.get(event.ipfsHash);
                if (state != null) {
                    state.startDate = event.startTime.longValue();
                    state.endDate = event.endTime.longValue();
                }
            }
            for (var event : deletedEvents) {
                announcementMap.remove(event.ipfsHash);
            }

            long now = System.currentTimeMillis() / 1000L;
            List<String> expiredHashes = announcementMap.values().stream()
                .filter(state -> state.endDate < now)
                .map(state -> state.ipfsHash)
                .collect(Collectors.toList());

            for (String expiredHash : expiredHashes) {
                try {
                    managementContract.deleteAnnouncement(expiredHash).send();
                } catch (Exception e) {
                    System.err.println("Scheduled deleteAnnouncement failed for " + expiredHash + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("AnnouncementCleanupService error: " + e.getMessage());
        }
    }

    // Helper class
    private static class AnnouncementState {
        String ipfsHash;
        long startDate;
        long endDate;

        AnnouncementState(String ipfsHash, long startDate, long endDate) {
            this.ipfsHash = ipfsHash;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}