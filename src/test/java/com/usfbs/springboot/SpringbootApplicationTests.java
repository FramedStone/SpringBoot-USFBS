package com.usfbs.springboot;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.quorum.Quorum;

import com.usfbs.springboot.service.QuorumService;

@SpringBootTest
class SpringbootApplicationTests {

	@Test
	void testConnectionToNode() throws Exception {
		QuorumService service = new QuorumService();
		Quorum quorum = service.getQuorum();
		Web3ClientVersion version = quorum.web3ClientVersion().send();

		assertNotNull(version.getWeb3ClientVersion());
		System.out.println("Client Version: " + version.getWeb3ClientVersion());
	}

}
