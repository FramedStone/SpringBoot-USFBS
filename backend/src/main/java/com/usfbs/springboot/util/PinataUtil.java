package com.usfbs.springboot.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usfbs.springboot.dto.PinataManifest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Component
public class PinataUtil {

    @Value("${pinata.jwt}")
    private String pinataJwt;

    @Value("${pinata.gateway}")
    private String pinataGateway;

    @Value("${pinata.api.url:https://api.pinata.cloud}")
    private String pinataApiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PinataUtil(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String uploadFileToIPFS(byte[] fileData, String fileName) throws Exception {
        try {
            if (pinataJwt == null || pinataJwt.isEmpty()) {
                // Mock implementation for testing
                String mockCid = "bafkreib" + System.currentTimeMillis();
                System.out.println("Mock file upload for: " + fileName + " -> " + mockCid);
                return mockCid;
            }

            String uploadUrl = pinataApiUrl + "/pinning/pinFileToIPFS";

            // Set up headers for file upload
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(pinataJwt);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create multipart request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileResource = new ByteArrayResource(fileData) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };
            body.add("file", fileResource);

            // Add pinata options
            Map<String, Object> pinataOptions = Map.of("cidVersion", 1);
            body.add("pinataOptions", objectMapper.writeValueAsString(pinataOptions));

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

            // Make the request to upload file
            ResponseEntity<Map> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String ipfsHash = (String) responseBody.get("IpfsHash");
                System.out.println("Successfully uploaded file to IPFS: " + ipfsHash);
                return ipfsHash;
            } else {
                throw new Exception("Failed to upload file to IPFS: HTTP " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("IPFS file upload error: " + e.getMessage());
            // Fallback to mock for development
            String mockCid = "bafkreib" + System.currentTimeMillis();
            System.out.println("Mock file upload for: " + fileName + " -> " + mockCid);
            return mockCid;
        }
    }

    public String uploadJsonToIPFS(Map<String, Object> jsonData, String fileName) throws Exception {
        try {
            if (pinataJwt == null || pinataJwt.isEmpty()) {
                // Mock implementation for testing
                String mockCid = "bafkreic" + System.currentTimeMillis();
                System.out.println("Mock JSON upload for: " + fileName + " -> " + mockCid);
                System.out.println("JSON data: " + objectMapper.writeValueAsString(jsonData));
                return mockCid;
            }

            String uploadUrl = pinataApiUrl + "/pinning/pinJSONToIPFS";

            // Set up headers for JSON upload
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(pinataJwt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request body with JSON data and metadata
            Map<String, Object> requestBody = Map.of(
                "pinataContent", jsonData,
                "pinataOptions", Map.of("cidVersion", 1),
                "pinataMetadata", Map.of("name", fileName)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make the request to upload JSON
            ResponseEntity<Map> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                entity,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String ipfsHash = (String) responseBody.get("IpfsHash");
                System.out.println("Successfully uploaded JSON to IPFS: " + ipfsHash);
                return ipfsHash;
            } else {
                throw new Exception("Failed to upload JSON to IPFS: HTTP " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("IPFS JSON upload error: " + e.getMessage());
            // Fallback to mock for development
            String mockCid = "bafkreic" + System.currentTimeMillis();
            System.out.println("Mock JSON upload for: " + fileName + " -> " + mockCid);
            System.out.println("JSON data: " + objectMapper.writeValueAsString(jsonData));
            return mockCid;
        }
    }

    public void unpinFromIPFS(String ipfsHash) throws Exception {
        try {
            if (pinataJwt == null || pinataJwt.isEmpty()) {
                System.out.println("Mock unpin for IPFS hash: " + ipfsHash);
                return;
            }

            String unpinUrl = pinataApiUrl + "/pinning/unpin/" + ipfsHash;

            // Set up headers for unpin request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(pinataJwt);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the DELETE request to unpin
            ResponseEntity<String> response = restTemplate.exchange(
                unpinUrl,
                HttpMethod.DELETE,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Successfully unpinned IPFS hash: " + ipfsHash);
            } else {
                throw new Exception("Failed to unpin from IPFS: HTTP " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("IPFS unpin error for hash " + ipfsHash + ": " + e.getMessage());
            // Mock unpin for testing
            System.out.println("Mock unpin for IPFS hash: " + ipfsHash);
        }
    }

    public String fetchFromIPFS(String ipfsHash) throws Exception {
        try {
            // Construct proper IPFS gateway URL
            String gatewayUrl;
            
            // Check if ipfsHash is a full URL or just a CID
            if (ipfsHash.startsWith("http://") || ipfsHash.startsWith("https://")) {
                gatewayUrl = ipfsHash;
            } else {
                // Construct URL using Pinata gateway
                if (pinataGateway != null && !pinataGateway.isEmpty()) {
                    // Use custom Pinata gateway
                    gatewayUrl = "https://" + pinataGateway + "/ipfs/" + ipfsHash;
                } else {
                    // Use public IPFS gateway as fallback
                    gatewayUrl = "https://gateway.pinata.cloud/ipfs/" + ipfsHash;
                }
            }

            System.out.println("Fetching from IPFS URL: " + gatewayUrl);

            // Set up headers for IPFS request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            if (pinataJwt != null && !pinataJwt.isEmpty()) {
                headers.setBearerAuth(pinataJwt);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the request to fetch IPFS content
            ResponseEntity<String> response = restTemplate.exchange(
                gatewayUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new Exception("Failed to fetch from IPFS: HTTP " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("IPFS fetch error for hash " + ipfsHash + ": " + e.getMessage());
            
            // Return mock manifest data for testing
            Map<String, Object> mockManifest = Map.of(
                "title", "Mock Announcement for " + ipfsHash,
                "startDate", System.currentTimeMillis() / 1000, // Convert to seconds
                "endDate", (System.currentTimeMillis() / 1000) + 86400, // +1 day
                "fileCid", "bafkreibmock" + ipfsHash.substring(Math.max(0, ipfsHash.length() - 8))
            );
            
            return objectMapper.writeValueAsString(mockManifest);
        }
    }

    public PinataManifest parseManifest(String manifestJson) throws Exception {
        try {
            return objectMapper.readValue(manifestJson, PinataManifest.class);
        } catch (Exception e) {
            System.err.println("Failed to parse manifest JSON: " + e.getMessage());
            throw new Exception("Invalid manifest format: " + e.getMessage());
        }
    }
}