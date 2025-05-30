package com.usfbs.springboot.service;

import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.util.PinataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Map;

@Service
public class AdminService {
  private final PinataUtil pinataUtil;
  private final Management managementContract;

  @Autowired
  public AdminService(PinataUtil pinataUtil, Management managementContract) {
    this.pinataUtil = pinataUtil;
    this.managementContract = managementContract;
  }

  public void addUser(String userAddress) throws Exception {
    managementContract.addUser(userAddress).send();
  }

  public void banUser(String userAddress) throws Exception {
    managementContract.banUser(userAddress).send();
  }

  public void unbanUser(String userAddress) throws Exception {
    managementContract.unbanUser(userAddress).send();
  }

  public String uploadAnnouncement(MultipartFile file, String title, long startDate, long endDate) throws Exception {
    // 1. Pin raw file -> get CID
    String fileCid = pinataUtil.uploadFileToIPFS(
      file.getBytes(),
      file.getOriginalFilename()
    );

    // 2. Build metadata JSON ⤵
    Map<String, Object> manifest = Map.of(
      "title", title,
      "startDate", startDate,
      "endDate", endDate,
      "fileCid", fileCid
    );

    // 3. Pin the JSON manifest → get its CID
    String metaCid = pinataUtil.uploadJsonToIPFS(manifest, "announcement-manifest.json");

    // 4. Store *that* CID on-chain (plus times for contract indexing)
    TransactionReceipt receipt = managementContract.addAnnouncement(
      metaCid,
      BigInteger.valueOf(startDate),
      BigInteger.valueOf(endDate)
    ).send();

    return receipt.getTransactionHash();
  }
}
