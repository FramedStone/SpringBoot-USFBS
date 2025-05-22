package com.usfbs.springboot.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/LFDT-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.14.0.
 */
@SuppressWarnings("rawtypes")
public class Management extends Contract {
    public static final String BINARY = "60a060405234801561001057600080fd5b506040516111d13803806111d183398101604081905261002f91610040565b6001600160a01b0316608052610070565b60006020828403121561005257600080fd5b81516001600160a01b038116811461006957600080fd5b9392505050565b6080516111236100ae600039600081816102b7015281816103c40152818161046a015281816105f1015281816107da01526109d301526111236000f3fe608060405234801561001057600080fd5b506004361061007d5760003560e01c80635c60f2261161005b5780635c60f226146100d3578063931431ea146100e65780639739ad5f146100f9578063b3ee93fc1461010c57600080fd5b80631ed87304146100825780632b6c588d146100ab578063421b2d8b146100c0575b600080fd5b610095610090366004610cc3565b61011f565b6040516100a29190610d50565b60405180910390f35b6100be6100b9366004610cc3565b6102ac565b005b6100be6100ce366004610d8c565b6103b9565b6100be6100e1366004610d8c565b61045f565b6100be6100f4366004610dbc565b6105e6565b6100be610107366004610e0a565b6107cf565b6100be61011a366004610dbc565b6109c8565b61014360405180606001604052806060815260200160008152602001600081525090565b6000826040516101539190610e6e565b908152604051908190036020019020805461016d90610e8a565b90506000036101975760405162461bcd60e51b815260040161018e90610ec4565b60405180910390fd5b600080836040516101a89190610e6e565b90815260200160405180910390206040518060600160405290816000820180546101d190610e8a565b80601f01602080910402602001604051908101604052809291908181526020018280546101fd90610e8a565b801561024a5780601f1061021f5761010080835404028352916020019161024a565b820191906000526020600020905b81548152906001019060200180831161022d57829003601f168201915b5050509183525050600182015460208201526002909101546040918201528151905191925033917fcd89f8003d5c5810477a5f9f6a603ee6803e738898ae8928844766c87efbbf6d9161029e914290610ef4565b60405180910390a292915050565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146102f45760405162461bcd60e51b815260040161018e90610f16565b6000816040516103049190610e6e565b908152604051908190036020019020805461031e90610e8a565b905060000361033f5760405162461bcd60e51b815260040161018e90610ec4565b60008160405161034f9190610e6e565b908152604051908190036020019020600061036a8282610bca565b5060006001820181905560029091015560405133907f7e5f432f0a4cebe76f981268147421b4fe4d869322e486ddcf97817cf0bf608d906103ae9084904290610ef4565b60405180910390a250565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146104015760405162461bcd60e51b815260040161018e90610f16565b6001600160a01b038116600081815260016020818152604092839020805460ff1916909217909155905142815233917f8af55b0f4d8e2cbaeef69c06f910b016a57b3807c1f854de7ec4881ed83b3d7c91015b60405180910390a350565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146104a75760405162461bcd60e51b815260040161018e90610f16565b6001600160a01b03811660009081526001602081905260409091205460ff161515146105155760405162461bcd60e51b815260206004820152601760248201527f55736572206e6f7420666f756e64202873797374656d29000000000000000000604482015260640161018e565b336001600160a01b038216036105665760405162461bcd60e51b81526020600482015260166024820152754163636573732064656e696564202873797374656d2960501b604482015260640161018e565b6001600160a01b038116600081815260016020908152604091829020805460ff19169055815182815260149281019290925273557365722072656d6f766564202861646d696e2960601b6060830152429082015233907ffa65fb89220eec84b1ada4d079a06ae008c52f053e2fbfc171d8196e4b9abddd90608001610454565b336001600160a01b037f0000000000000000000000000000000000000000000000000000000000000000161461062e5760405162461bcd60e51b815260040161018e90610f16565b60008360405161063e9190610e6e565b908152604051908190036020019020805461065890610e8a565b15905061069d5760405162461bcd60e51b8152602060048201526013602482015272088eae0d8d2c6c2e8cac840d2e0cce690c2e6d606b1b604482015260640161018e565b816000036106e65760405162461bcd60e51b81526020600482015260166024820152751cdd185c9d151a5b59481b9bdd081c1c9bdd9a59195960521b604482015260640161018e565b8060000361072d5760405162461bcd60e51b8152602060048201526014602482015273195b99151a5b59481b9bdd081c1c9bdd9a59195960621b604482015260640161018e565b6040518060600160405280848152602001838152602001828152506000846040516107589190610e6e565b908152604051908190036020019020815181906107759082610f8c565b50602082015160018201556040918201516002909101555133907f49a4e12bfea796ddfee159f7d8552825d60cea59ab6ea03f43431bfb474b79a3906107c290869086908690429061104c565b60405180910390a2505050565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146108175760405162461bcd60e51b815260040161018e90610f16565b6000826040516108279190610e6e565b908152604051908190036020019020805461084190610e8a565b90506000036108625760405162461bcd60e51b815260040161018e90610ec4565b80516000036108ab5760405162461bcd60e51b81526020600482015260156024820152741a5c199cd2185cda081b9bdd081c1c9bdd9a591959605a1b604482015260640161018e565b60405180606001604052808281526020016000846040516108cc9190610e6e565b90815260200160405180910390206001015481526020016000846040516108f39190610e6e565b9081526020016040518091039020600201548152506000826040516109189190610e6e565b908152604051908190036020019020815181906109359082610f8c565b50602082015160018201556040918201516002909101555160009061095b908490610e6e565b90815260405190819003602001902060006109768282610bca565b5060006001820181905560029091015560405133907f748e0cf6928a9d4807ef9076c5befa6090e9dd72cc44f78bc0858726e675cf4f906109bc9085908590429061107b565b60405180910390a25050565b336001600160a01b037f00000000000000000000000000000000000000000000000000000000000000001614610a105760405162461bcd60e51b815260040161018e90610f16565b600083604051610a209190610e6e565b9081526040519081900360200190208054610a3a90610e8a565b9050600003610a5b5760405162461bcd60e51b815260040161018e90610ec4565b81600003610aa45760405162461bcd60e51b81526020600482015260166024820152751cdd185c9d151a5b59481b9bdd081c1c9bdd9a59195960521b604482015260640161018e565b80600003610aeb5760405162461bcd60e51b8152602060048201526014602482015273195b99151a5b59481b9bdd081c1c9bdd9a59195960621b604482015260640161018e565b60008084604051610afc9190610e6e565b908152602001604051809103902060010154905060008085604051610b219190610e6e565b908152602001604051809103902060020154905083600086604051610b469190610e6e565b90815260200160405180910390206001018190555082600086604051610b6c9190610e6e565b9081526040519081900360200181206002019190915533907fcf919766b203e7636b2f7d99b6c4fd24957448dd84e095089b465af12fd6c85190610bbb908890869086908a908a9042906110b1565b60405180910390a25050505050565b508054610bd690610e8a565b6000825580601f10610be6575050565b601f016020900490600052602060002090810190610c049190610c07565b50565b5b80821115610c1c5760008155600101610c08565b5090565b634e487b7160e01b600052604160045260246000fd5b600082601f830112610c4757600080fd5b813567ffffffffffffffff80821115610c6257610c62610c20565b604051601f8301601f19908116603f01168101908282118183101715610c8a57610c8a610c20565b81604052838152866020858801011115610ca357600080fd5b836020870160208301376000602085830101528094505050505092915050565b600060208284031215610cd557600080fd5b813567ffffffffffffffff811115610cec57600080fd5b610cf884828501610c36565b949350505050565b60005b83811015610d1b578181015183820152602001610d03565b50506000910152565b60008151808452610d3c816020860160208601610d00565b601f01601f19169290920160200192915050565b602081526000825160606020840152610d6c6080840182610d24565b905060208401516040840152604084015160608401528091505092915050565b600060208284031215610d9e57600080fd5b81356001600160a01b0381168114610db557600080fd5b9392505050565b600080600060608486031215610dd157600080fd5b833567ffffffffffffffff811115610de857600080fd5b610df486828701610c36565b9660208601359650604090950135949350505050565b60008060408385031215610e1d57600080fd5b823567ffffffffffffffff80821115610e3557600080fd5b610e4186838701610c36565b93506020850135915080821115610e5757600080fd5b50610e6485828601610c36565b9150509250929050565b60008251610e80818460208701610d00565b9190910192915050565b600181811c90821680610e9e57607f821691505b602082108103610ebe57634e487b7160e01b600052602260045260246000fd5b50919050565b602080825260169082015275105b9b9bdd5b98d95b595b9d081b9bdd08199bdd5b9960521b604082015260600190565b604081526000610f076040830185610d24565b90508260208301529392505050565b6020808252600d908201526c1058d8d95cdcc819195b9a5959609a1b604082015260600190565b601f821115610f8757600081815260208120601f850160051c81016020861015610f645750805b601f850160051c820191505b81811015610f8357828155600101610f70565b5050505b505050565b815167ffffffffffffffff811115610fa657610fa6610c20565b610fba81610fb48454610e8a565b84610f3d565b602080601f831160018114610fef5760008415610fd75750858301515b600019600386901b1c1916600185901b178555610f83565b600085815260208120601f198616915b8281101561101e57888601518255948401946001909101908401610fff565b508582101561103c5787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b60808152600061105f6080830187610d24565b6020830195909552506040810192909252606090910152919050565b60608152600061108e6060830186610d24565b82810360208401526110a08186610d24565b915050826040830152949350505050565b60c0815260006110c460c0830189610d24565b60208301979097525060408101949094526060840192909252608083015260a09091015291905056fea2646970667358221220e0addb9eb2ad7f5916f5384fb44aa3d0bd6684a30297d3f33495b9535690691c64736f6c63430008130033";

    private static String librariesLinkedBinary;

    public static final String FUNC_ADDANNOUNCEMENT = "addAnnouncement";

    public static final String FUNC_ADDUSER = "addUser";

    public static final String FUNC_DELETEANNOUNCEMENT = "deleteAnnouncement";

    public static final String FUNC_DELETEUSER = "deleteUser";

    public static final String FUNC_GETANNOUNCEMENT = "getAnnouncement";

    public static final String FUNC_UPDATEANNOUNCEMENTIPFSHASH = "updateAnnouncementIpfsHash";

    public static final String FUNC_UPDATEANNOUNCEMENTTIME = "updateAnnouncementTime";

    public static final Event ANNOUNCEMENTADDED_EVENT = new Event("announcementAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ANNOUNCEMENTDELETED_EVENT = new Event("announcementDeleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ANNOUNCEMENTIPFSHASHMODIFIED_EVENT = new Event("announcementIpfsHashModified", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ANNOUNCEMENTREQUESTED_EVENT = new Event("announcementRequested", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ANNOUNCEMENTTIMEMODIFIED_EVENT = new Event("announcementTimeModified", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event USERADDED_EVENT = new Event("userAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event USERDELETED_EVENT = new Event("userDeleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected Management(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Management(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Management(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Management(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<AnnouncementAddedEventResponse> getAnnouncementAddedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ANNOUNCEMENTADDED_EVENT, transactionReceipt);
        ArrayList<AnnouncementAddedEventResponse> responses = new ArrayList<AnnouncementAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AnnouncementAddedEventResponse typedResponse = new AnnouncementAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AnnouncementAddedEventResponse getAnnouncementAddedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ANNOUNCEMENTADDED_EVENT, log);
        AnnouncementAddedEventResponse typedResponse = new AnnouncementAddedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
        return typedResponse;
    }

    public Flowable<AnnouncementAddedEventResponse> announcementAddedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getAnnouncementAddedEventFromLog(log));
    }

    public Flowable<AnnouncementAddedEventResponse> announcementAddedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ANNOUNCEMENTADDED_EVENT));
        return announcementAddedEventFlowable(filter);
    }

    public static List<AnnouncementDeletedEventResponse> getAnnouncementDeletedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ANNOUNCEMENTDELETED_EVENT, transactionReceipt);
        ArrayList<AnnouncementDeletedEventResponse> responses = new ArrayList<AnnouncementDeletedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AnnouncementDeletedEventResponse typedResponse = new AnnouncementDeletedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AnnouncementDeletedEventResponse getAnnouncementDeletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ANNOUNCEMENTDELETED_EVENT, log);
        AnnouncementDeletedEventResponse typedResponse = new AnnouncementDeletedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<AnnouncementDeletedEventResponse> announcementDeletedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getAnnouncementDeletedEventFromLog(log));
    }

    public Flowable<AnnouncementDeletedEventResponse> announcementDeletedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ANNOUNCEMENTDELETED_EVENT));
        return announcementDeletedEventFlowable(filter);
    }

    public static List<AnnouncementIpfsHashModifiedEventResponse> getAnnouncementIpfsHashModifiedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ANNOUNCEMENTIPFSHASHMODIFIED_EVENT, transactionReceipt);
        ArrayList<AnnouncementIpfsHashModifiedEventResponse> responses = new ArrayList<AnnouncementIpfsHashModifiedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AnnouncementIpfsHashModifiedEventResponse typedResponse = new AnnouncementIpfsHashModifiedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ipfsHash_ = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AnnouncementIpfsHashModifiedEventResponse getAnnouncementIpfsHashModifiedEventFromLog(
            Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ANNOUNCEMENTIPFSHASHMODIFIED_EVENT, log);
        AnnouncementIpfsHashModifiedEventResponse typedResponse = new AnnouncementIpfsHashModifiedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ipfsHash_ = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<AnnouncementIpfsHashModifiedEventResponse> announcementIpfsHashModifiedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getAnnouncementIpfsHashModifiedEventFromLog(log));
    }

    public Flowable<AnnouncementIpfsHashModifiedEventResponse> announcementIpfsHashModifiedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ANNOUNCEMENTIPFSHASHMODIFIED_EVENT));
        return announcementIpfsHashModifiedEventFlowable(filter);
    }

    public static List<AnnouncementRequestedEventResponse> getAnnouncementRequestedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ANNOUNCEMENTREQUESTED_EVENT, transactionReceipt);
        ArrayList<AnnouncementRequestedEventResponse> responses = new ArrayList<AnnouncementRequestedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AnnouncementRequestedEventResponse typedResponse = new AnnouncementRequestedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AnnouncementRequestedEventResponse getAnnouncementRequestedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ANNOUNCEMENTREQUESTED_EVENT, log);
        AnnouncementRequestedEventResponse typedResponse = new AnnouncementRequestedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<AnnouncementRequestedEventResponse> announcementRequestedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getAnnouncementRequestedEventFromLog(log));
    }

    public Flowable<AnnouncementRequestedEventResponse> announcementRequestedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ANNOUNCEMENTREQUESTED_EVENT));
        return announcementRequestedEventFlowable(filter);
    }

    public static List<AnnouncementTimeModifiedEventResponse> getAnnouncementTimeModifiedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ANNOUNCEMENTTIMEMODIFIED_EVENT, transactionReceipt);
        ArrayList<AnnouncementTimeModifiedEventResponse> responses = new ArrayList<AnnouncementTimeModifiedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            AnnouncementTimeModifiedEventResponse typedResponse = new AnnouncementTimeModifiedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.startTime_ = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.endTime_ = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AnnouncementTimeModifiedEventResponse getAnnouncementTimeModifiedEventFromLog(
            Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ANNOUNCEMENTTIMEMODIFIED_EVENT, log);
        AnnouncementTimeModifiedEventResponse typedResponse = new AnnouncementTimeModifiedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.startTime_ = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.endTime_ = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
        return typedResponse;
    }

    public Flowable<AnnouncementTimeModifiedEventResponse> announcementTimeModifiedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getAnnouncementTimeModifiedEventFromLog(log));
    }

    public Flowable<AnnouncementTimeModifiedEventResponse> announcementTimeModifiedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ANNOUNCEMENTTIMEMODIFIED_EVENT));
        return announcementTimeModifiedEventFlowable(filter);
    }

    public static List<UserAddedEventResponse> getUserAddedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(USERADDED_EVENT, transactionReceipt);
        ArrayList<UserAddedEventResponse> responses = new ArrayList<UserAddedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            UserAddedEventResponse typedResponse = new UserAddedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.user = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static UserAddedEventResponse getUserAddedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(USERADDED_EVENT, log);
        UserAddedEventResponse typedResponse = new UserAddedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.user = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<UserAddedEventResponse> userAddedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getUserAddedEventFromLog(log));
    }

    public Flowable<UserAddedEventResponse> userAddedEventFlowable(DefaultBlockParameter startBlock,
            DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(USERADDED_EVENT));
        return userAddedEventFlowable(filter);
    }

    public static List<UserDeletedEventResponse> getUserDeletedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(USERDELETED_EVENT, transactionReceipt);
        ArrayList<UserDeletedEventResponse> responses = new ArrayList<UserDeletedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            UserDeletedEventResponse typedResponse = new UserDeletedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.user = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.note = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static UserDeletedEventResponse getUserDeletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(USERDELETED_EVENT, log);
        UserDeletedEventResponse typedResponse = new UserDeletedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.user = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.note = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<UserDeletedEventResponse> userDeletedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getUserDeletedEventFromLog(log));
    }

    public Flowable<UserDeletedEventResponse> userDeletedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(USERDELETED_EVENT));
        return userDeletedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> addAnnouncement(String ipfsHash,
            BigInteger startTime, BigInteger endTime) {
        final Function function = new Function(
                FUNC_ADDANNOUNCEMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(ipfsHash), 
                new org.web3j.abi.datatypes.generated.Uint256(startTime), 
                new org.web3j.abi.datatypes.generated.Uint256(endTime)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> addUser(String user) {
        final Function function = new Function(
                FUNC_ADDUSER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, user)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> deleteAnnouncement(String ipfsHash) {
        final Function function = new Function(
                FUNC_DELETEANNOUNCEMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(ipfsHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> deleteUser(String user) {
        final Function function = new Function(
                FUNC_DELETEUSER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, user)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> getAnnouncement(String ipfsHash) {
        final Function function = new Function(
                FUNC_GETANNOUNCEMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(ipfsHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateAnnouncementIpfsHash(String ipfsHash_,
            String ipfsHash) {
        final Function function = new Function(
                FUNC_UPDATEANNOUNCEMENTIPFSHASH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(ipfsHash_), 
                new org.web3j.abi.datatypes.Utf8String(ipfsHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateAnnouncementTime(String ipfsHash,
            BigInteger startTime, BigInteger endTime) {
        final Function function = new Function(
                FUNC_UPDATEANNOUNCEMENTTIME, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(ipfsHash), 
                new org.web3j.abi.datatypes.generated.Uint256(startTime), 
                new org.web3j.abi.datatypes.generated.Uint256(endTime)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Management load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new Management(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Management load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Management(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Management load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new Management(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Management load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Management(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Management> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider, String admin) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, admin)));
        return deployRemoteCall(Management.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    public static RemoteCall<Management> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider, String admin) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, admin)));
        return deployRemoteCall(Management.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Management> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, String admin) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, admin)));
        return deployRemoteCall(Management.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Management> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit, String admin) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, admin)));
        return deployRemoteCall(Management.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    public static void linkLibraries(List<Contract.LinkReference> references) {
        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class Announcement extends DynamicStruct {
        public String ipfsHash;

        public BigInteger startTime;

        public BigInteger endTime;

        public Announcement(String ipfsHash, BigInteger startTime, BigInteger endTime) {
            super(new org.web3j.abi.datatypes.Utf8String(ipfsHash), 
                    new org.web3j.abi.datatypes.generated.Uint256(startTime), 
                    new org.web3j.abi.datatypes.generated.Uint256(endTime));
            this.ipfsHash = ipfsHash;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Announcement(Utf8String ipfsHash, Uint256 startTime, Uint256 endTime) {
            super(ipfsHash, startTime, endTime);
            this.ipfsHash = ipfsHash.getValue();
            this.startTime = startTime.getValue();
            this.endTime = endTime.getValue();
        }
    }

    public static class AnnouncementAddedEventResponse extends BaseEventResponse {
        public String from;

        public String ipfsHash;

        public BigInteger startTime;

        public BigInteger endTime;

        public BigInteger timestamp;
    }

    public static class AnnouncementDeletedEventResponse extends BaseEventResponse {
        public String from;

        public String ipfsHash;

        public BigInteger timestamp;
    }

    public static class AnnouncementIpfsHashModifiedEventResponse extends BaseEventResponse {
        public String from;

        public String ipfsHash_;

        public String ipfsHash;

        public BigInteger timestamp;
    }

    public static class AnnouncementRequestedEventResponse extends BaseEventResponse {
        public String from;

        public String ipfsHash;

        public BigInteger timestamp;
    }

    public static class AnnouncementTimeModifiedEventResponse extends BaseEventResponse {
        public String from;

        public String ipfsHash;

        public BigInteger startTime_;

        public BigInteger endTime_;

        public BigInteger startTime;

        public BigInteger endTime;

        public BigInteger timestamp;
    }

    public static class UserAddedEventResponse extends BaseEventResponse {
        public String from;

        public String user;

        public BigInteger timestamp;
    }

    public static class UserDeletedEventResponse extends BaseEventResponse {
        public String from;

        public String user;

        public String note;

        public BigInteger timestamp;
    }
}
