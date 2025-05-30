package com.usfbs.springboot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.MultipartConfigElement;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PinataUtil {

    @Value("${pinata.jwt}")
    private String pinataJwt;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        if (pinataJwt == null || pinataJwt.isBlank()) {
            throw new IllegalStateException("TODO: Set PINATA_JWT in environment");
        }
    }

    /**
     * Uploads a byte[] (e.g. JSON) as “file” to Pinata
     * Docs: https://docs.pinata.cloud/api-pinning/pin-file-to-ipfs
     */
    public String uploadJsonToIPFS(Object data, String fileName) throws Exception {
        byte[] jsonBytes = mapper.writeValueAsBytes(data);
        RequestBody bodyPart = RequestBody.create(
                jsonBytes,
                MediaType.parse("application/json")
        );
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, bodyPart)
                .build();

        Request request = new Request.Builder()
                .url("https://api.pinata.cloud/pinning/pinFileToIPFS")
                .addHeader("Authorization", "Bearer " + pinataJwt)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Pinata JSON upload failed: " + response);
            }
            String resp = response.body().string();
            return mapper.readTree(resp).get("IpfsHash").asText();
        }
    }

    public String uploadFileToIPFS(byte[] fileBytes, String fileName) throws Exception {
        RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse("application/octet-stream"));
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, fileBody)
                .build();

        Request request = new Request.Builder()
                .url("https://api.pinata.cloud/pinning/pinFileToIPFS")
                .addHeader("Authorization", "Bearer " + pinataJwt)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new Exception("Pinata upload failed: " + response);
            String responseBody = response.body().string();
            return mapper.readTree(responseBody).get("IpfsHash").asText();
        }
    }
}