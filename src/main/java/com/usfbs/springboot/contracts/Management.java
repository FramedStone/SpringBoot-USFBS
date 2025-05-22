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
    public static final String BINARY = "608060405234801561001057600080fd5b5060405161114c38038061114c83398101604081905261002f91610059565b6001600160a01b03166000908152600160208190526040909120805460ff19169091179055610089565b60006020828403121561006b57600080fd5b81516001600160a01b038116811461008257600080fd5b9392505050565b6110b4806100986000396000f3fe608060405234801561001057600080fd5b506004361061007d5760003560e01c80635c60f2261161005b5780635c60f226146100d3578063931431ea146100e65780639739ad5f146100f9578063b3ee93fc1461010c57600080fd5b80631ed87304146100825780632b6c588d146100ab578063421b2d8b146100c0575b600080fd5b610095610090366004610c54565b61011f565b6040516100a29190610ce1565b60405180910390f35b6100be6100b9366004610c54565b6102ac565b005b6100be6100ce366004610d1d565b6103a6565b6100be6100e1366004610d1d565b61043d565b6100be6100f4366004610d4d565b6105b0565b6100be610107366004610d9b565b610786565b6100be61011a366004610d4d565b61096c565b61014360405180606001604052806060815260200160008152602001600081525090565b6000826040516101539190610dff565b908152604051908190036020019020805461016d90610e1b565b90506000036101975760405162461bcd60e51b815260040161018e90610e55565b60405180910390fd5b600080836040516101a89190610dff565b90815260200160405180910390206040518060600160405290816000820180546101d190610e1b565b80601f01602080910402602001604051908101604052809291908181526020018280546101fd90610e1b565b801561024a5780601f1061021f5761010080835404028352916020019161024a565b820191906000526020600020905b81548152906001019060200180831161022d57829003601f168201915b5050509183525050600182015460208201526002909101546040918201528151905191925033917fcd89f8003d5c5810477a5f9f6a603ee6803e738898ae8928844766c87efbbf6d9161029e914290610e85565b60405180910390a292915050565b3360009081526001602081905260409091205460ff161515146102e15760405162461bcd60e51b815260040161018e90610ea7565b6000816040516102f19190610dff565b908152604051908190036020019020805461030b90610e1b565b905060000361032c5760405162461bcd60e51b815260040161018e90610e55565b60008160405161033c9190610dff565b90815260405190819003602001902060006103578282610b5b565b5060006001820181905560029091015560405133907f7e5f432f0a4cebe76f981268147421b4fe4d869322e486ddcf97817cf0bf608d9061039b9084904290610e85565b60405180910390a250565b3360009081526001602081905260409091205460ff161515146103db5760405162461bcd60e51b815260040161018e90610ea7565b6001600160a01b03811660008181526002602052604090819020805460ff191660011790555133907f8af55b0f4d8e2cbaeef69c06f910b016a57b3807c1f854de7ec4881ed83b3d7c906104329042815260200190565b60405180910390a350565b3360009081526001602081905260409091205460ff161515146104725760405162461bcd60e51b815260040161018e90610ea7565b6001600160a01b03811660009081526002602052604090205460ff1615156001146104df5760405162461bcd60e51b815260206004820152601760248201527f55736572206e6f7420666f756e64202873797374656d29000000000000000000604482015260640161018e565b336001600160a01b038216036105305760405162461bcd60e51b81526020600482015260166024820152754163636573732064656e696564202873797374656d2960501b604482015260640161018e565b6001600160a01b038116600081815260026020908152604091829020805460ff19169055815182815260149281019290925273557365722072656d6f766564202861646d696e2960601b6060830152429082015233907ffa65fb89220eec84b1ada4d079a06ae008c52f053e2fbfc171d8196e4b9abddd90608001610432565b3360009081526001602081905260409091205460ff161515146105e55760405162461bcd60e51b815260040161018e90610ea7565b6000836040516105f59190610dff565b908152604051908190036020019020805461060f90610e1b565b1590506106545760405162461bcd60e51b8152602060048201526013602482015272088eae0d8d2c6c2e8cac840d2e0cce690c2e6d606b1b604482015260640161018e565b8160000361069d5760405162461bcd60e51b81526020600482015260166024820152751cdd185c9d151a5b59481b9bdd081c1c9bdd9a59195960521b604482015260640161018e565b806000036106e45760405162461bcd60e51b8152602060048201526014602482015273195b99151a5b59481b9bdd081c1c9bdd9a59195960621b604482015260640161018e565b60405180606001604052808481526020018381526020018281525060008460405161070f9190610dff565b9081526040519081900360200190208151819061072c9082610f1d565b50602082015160018201556040918201516002909101555133907f49a4e12bfea796ddfee159f7d8552825d60cea59ab6ea03f43431bfb474b79a390610779908690869086904290610fdd565b60405180910390a2505050565b3360009081526001602081905260409091205460ff161515146107bb5760405162461bcd60e51b815260040161018e90610ea7565b6000826040516107cb9190610dff565b90815260405190819003602001902080546107e590610e1b565b90506000036108065760405162461bcd60e51b815260040161018e90610e55565b805160000361084f5760405162461bcd60e51b81526020600482015260156024820152741a5c199cd2185cda081b9bdd081c1c9bdd9a591959605a1b604482015260640161018e565b60405180606001604052808281526020016000846040516108709190610dff565b90815260200160405180910390206001015481526020016000846040516108979190610dff565b9081526020016040518091039020600201548152506000826040516108bc9190610dff565b908152604051908190036020019020815181906108d99082610f1d565b5060208201516001820155604091820151600290910155516000906108ff908490610dff565b908152604051908190036020019020600061091a8282610b5b565b5060006001820181905560029091015560405133907f748e0cf6928a9d4807ef9076c5befa6090e9dd72cc44f78bc0858726e675cf4f906109609085908590429061100c565b60405180910390a25050565b3360009081526001602081905260409091205460ff161515146109a15760405162461bcd60e51b815260040161018e90610ea7565b6000836040516109b19190610dff565b90815260405190819003602001902080546109cb90610e1b565b90506000036109ec5760405162461bcd60e51b815260040161018e90610e55565b81600003610a355760405162461bcd60e51b81526020600482015260166024820152751cdd185c9d151a5b59481b9bdd081c1c9bdd9a59195960521b604482015260640161018e565b80600003610a7c5760405162461bcd60e51b8152602060048201526014602482015273195b99151a5b59481b9bdd081c1c9bdd9a59195960621b604482015260640161018e565b60008084604051610a8d9190610dff565b908152602001604051809103902060010154905060008085604051610ab29190610dff565b908152602001604051809103902060020154905083600086604051610ad79190610dff565b90815260200160405180910390206001018190555082600086604051610afd9190610dff565b9081526040519081900360200181206002019190915533907fcf919766b203e7636b2f7d99b6c4fd24957448dd84e095089b465af12fd6c85190610b4c908890869086908a908a904290611042565b60405180910390a25050505050565b508054610b6790610e1b565b6000825580601f10610b77575050565b601f016020900490600052602060002090810190610b959190610b98565b50565b5b80821115610bad5760008155600101610b99565b5090565b634e487b7160e01b600052604160045260246000fd5b600082601f830112610bd857600080fd5b813567ffffffffffffffff80821115610bf357610bf3610bb1565b604051601f8301601f19908116603f01168101908282118183101715610c1b57610c1b610bb1565b81604052838152866020858801011115610c3457600080fd5b836020870160208301376000602085830101528094505050505092915050565b600060208284031215610c6657600080fd5b813567ffffffffffffffff811115610c7d57600080fd5b610c8984828501610bc7565b949350505050565b60005b83811015610cac578181015183820152602001610c94565b50506000910152565b60008151808452610ccd816020860160208601610c91565b601f01601f19169290920160200192915050565b602081526000825160606020840152610cfd6080840182610cb5565b905060208401516040840152604084015160608401528091505092915050565b600060208284031215610d2f57600080fd5b81356001600160a01b0381168114610d4657600080fd5b9392505050565b600080600060608486031215610d6257600080fd5b833567ffffffffffffffff811115610d7957600080fd5b610d8586828701610bc7565b9660208601359650604090950135949350505050565b60008060408385031215610dae57600080fd5b823567ffffffffffffffff80821115610dc657600080fd5b610dd286838701610bc7565b93506020850135915080821115610de857600080fd5b50610df585828601610bc7565b9150509250929050565b60008251610e11818460208701610c91565b9190910192915050565b600181811c90821680610e2f57607f821691505b602082108103610e4f57634e487b7160e01b600052602260045260246000fd5b50919050565b602080825260169082015275105b9b9bdd5b98d95b595b9d081b9bdd08199bdd5b9960521b604082015260600190565b604081526000610e986040830185610cb5565b90508260208301529392505050565b6020808252600d908201526c1058d8d95cdcc819195b9a5959609a1b604082015260600190565b601f821115610f1857600081815260208120601f850160051c81016020861015610ef55750805b601f850160051c820191505b81811015610f1457828155600101610f01565b5050505b505050565b815167ffffffffffffffff811115610f3757610f37610bb1565b610f4b81610f458454610e1b565b84610ece565b602080601f831160018114610f805760008415610f685750858301515b600019600386901b1c1916600185901b178555610f14565b600085815260208120601f198616915b82811015610faf57888601518255948401946001909101908401610f90565b5085821015610fcd5787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b608081526000610ff06080830187610cb5565b6020830195909552506040810192909252606090910152919050565b60608152600061101f6060830186610cb5565b82810360208401526110318186610cb5565b915050826040830152949350505050565b60c08152600061105560c0830189610cb5565b60208301979097525060408101949094526060840192909252608083015260a09091015291905056fea26469706673582212204bf4fa1b000e7b10020fbfabb86f4854d547773417ba02c5121e23b92021989764736f6c63430008130033";

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
