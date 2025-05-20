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
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
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
    public static final String BINARY = "60a060405234801561001057600080fd5b50604051610b85380380610b8583398101604081905261002f91610040565b6001600160a01b0316608052610070565b60006020828403121561005257600080fd5b81516001600160a01b038116811461006957600080fd5b9392505050565b608051610ad76100ae6000396000818161013a01528181610215015281816102bb01528181610441015281816106a901526108590152610ad76000f3fe608060405234801561001057600080fd5b506004361061007d5760003560e01c80635c60f2261161005b5780635c60f226146100bd5780635fad12bd146100d057806392a293c114610109578063eacfd5da1461011c57600080fd5b806329d340e314610082578063421b2d8b14610097578063450e2c7f146100aa575b600080fd5b6100956100903660046109ac565b61012f565b005b6100956100a53660046109c5565b61020a565b6100956100b83660046109f5565b6102b0565b6100956100cb3660046109c5565b610436565b6100e36100de3660046109ac565b6105bd565b604080518251815260208084015190820152918101519082015260600160405180910390f35b6100956101173660046109f5565b61069e565b61009561012a366004610a21565b61084e565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146101805760405162461bcd60e51b815260040161017790610a43565b60405180910390fd5b60008181526020819052604081205490036101ad5760405162461bcd60e51b815260040161017790610a6a565b60008181526020818152604080832083815560018101849055600201929092558151838152429181019190915233917fcbbe1acf7c321250d3df9e548e60b3132401dbcb2f37a214a4360ce8707e18fd910160405180910390a250565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146102525760405162461bcd60e51b815260040161017790610a43565b6001600160a01b038116600081815260016020818152604092839020805460ff1916909217909155905142815233917f8af55b0f4d8e2cbaeef69c06f910b016a57b3807c1f854de7ec4881ed83b3d7c91015b60405180910390a350565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146102f85760405162461bcd60e51b815260040161017790610a43565b60008381526020819052604081205490036103255760405162461bcd60e51b815260040161017790610a6a565b8160000361036e5760405162461bcd60e51b81526020600482015260166024820152751cdd185c9d151a5b59481b9bdd081c1c9bdd9a59195960521b6044820152606401610177565b806000036103b55760405162461bcd60e51b8152602060048201526014602482015273195b99151a5b59481b9bdd081c1c9bdd9a59195960621b6044820152606401610177565b600083815260208181526040918290206001810180546002909201805491879055859055835187815292830182905292820183905260608201859052608082018490524260a0830152919033907f40e1798b916dd071e7839ad58bd4a7ad04d11fde1a45f53a58c975b1cb250b309060c00160405180910390a25050505050565b336001600160a01b037f0000000000000000000000000000000000000000000000000000000000000000161461047e5760405162461bcd60e51b815260040161017790610a43565b6001600160a01b03811660009081526001602081905260409091205460ff161515146104ec5760405162461bcd60e51b815260206004820152601760248201527f55736572206e6f7420666f756e64202873797374656d290000000000000000006044820152606401610177565b336001600160a01b0382160361053d5760405162461bcd60e51b81526020600482015260166024820152754163636573732064656e696564202873797374656d2960501b6044820152606401610177565b6001600160a01b038116600081815260016020908152604091829020805460ff19169055815182815260149281019290925273557365722072656d6f766564202861646d696e2960601b6060830152429082015233907ffa65fb89220eec84b1ada4d079a06ae008c52f053e2fbfc171d8196e4b9abddd906080016102a5565b60408051606081018252600080825260208083018290528284018290528482528190529182205490910361062c5760405162461bcd60e51b8152602060048201526016602482015275105b9b9bdd5b98d95b595b9d081b9bdd08199bdd5b9960521b6044820152606401610177565b60008281526020818152604091829020825160608101845281548082526001830154828501526002909201548185015283519182524292820192909252909133917f3dd971c6a33aaac889ae8a341273f7114e9ce87c5cc632fb85afe2a7dafd273a910160405180910390a292915050565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146106e65760405162461bcd60e51b815260040161017790610a43565b600083815260208190526040902054156107385760405162461bcd60e51b8152602060048201526013602482015272088eae0d8d2c6c2e8cac840d2e0cce690c2e6d606b1b6044820152606401610177565b816000036107815760405162461bcd60e51b81526020600482015260166024820152751cdd185c9d151a5b59481b9bdd081c1c9bdd9a59195960521b6044820152606401610177565b806000036107c85760405162461bcd60e51b8152602060048201526014602482015273195b99151a5b59481b9bdd081c1c9bdd9a59195960621b6044820152606401610177565b60408051606080820183528582526020808301868152838501868152600089815280845286902094518555905160018501555160029093019290925582518681529182018590528183018490524290820152905133917f0cf26c0e1bf120425dfaebe1f52e67174e8de83e1a5ac5d7723aaef55a1cd01a919081900360800190a2505050565b336001600160a01b037f000000000000000000000000000000000000000000000000000000000000000016146108965760405162461bcd60e51b815260040161017790610a43565b60008281526020819052604081205490036108c35760405162461bcd60e51b815260040161017790610a6a565b600081900361090c5760405162461bcd60e51b81526020600482015260156024820152741a5c199cd2185cda081b9bdd081c1c9bdd9a591959605a1b6044820152606401610177565b60408051606080820183528382526000858152602081815284822060018082018054848801908152600280850180548a8c019081528c89528888528b89209a518b559251948a019490945590519701969096558884529083905593829055925582518581529182018490524282840152915133927f9e9b48c49e871d83025de86efbb7c9fbd84ff83c941070df3def30810b535a0e928290030190a25050565b6000602082840312156109be57600080fd5b5035919050565b6000602082840312156109d757600080fd5b81356001600160a01b03811681146109ee57600080fd5b9392505050565b600080600060608486031215610a0a57600080fd5b505081359360208301359350604090920135919050565b60008060408385031215610a3457600080fd5b50508035926020909101359150565b6020808252600d908201526c1058d8d95cdcc819195b9a5959609a1b604082015260600190565b6020808252601f908201527f416e6e6f756e63656d656e74206970667348617368206e6f7420666f756e640060408201526060019056fea2646970667358221220c79c47bfe042d1aae5d718badb4c4c115e8521de30ccc0c69e6b557676d7d83764736f6c63430008130033";

    private static String librariesLinkedBinary;

    public static final String FUNC_ADDANNOUNCEMENT = "addAnnouncement";

    public static final String FUNC_ADDUSER = "addUser";

    public static final String FUNC_DELETEANNOUNCEMENT = "deleteAnnouncement";

    public static final String FUNC_DELETEUSER = "deleteUser";

    public static final String FUNC_GETANNOUNCEMENT = "getAnnouncement";

    public static final String FUNC_UPDATEANNOUNCEMENTIPFSHASH = "updateAnnouncementIpfsHash";

    public static final String FUNC_UPDATEANNOUNCEMENTTIME = "updateAnnouncementTime";

    public static final Event ANNOUNCEMENTADDED_EVENT = new Event("announcementAdded", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ANNOUNCEMENTDELETED_EVENT = new Event("announcementDeleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ANNOUNCEMENTIPFSHASHMODIFIED_EVENT = new Event("announcementIpfsHashModified", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bytes32>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ANNOUNCEMENTREQUESTED_EVENT = new Event("announcementRequested", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ANNOUNCEMENTTIMEMODIFIED_EVENT = new Event("announcementTimeModified", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Bytes32>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
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
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AnnouncementAddedEventResponse getAnnouncementAddedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ANNOUNCEMENTADDED_EVENT, log);
        AnnouncementAddedEventResponse typedResponse = new AnnouncementAddedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
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
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AnnouncementDeletedEventResponse getAnnouncementDeletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ANNOUNCEMENTDELETED_EVENT, log);
        AnnouncementDeletedEventResponse typedResponse = new AnnouncementDeletedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
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
            typedResponse.ipfsHash_ = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
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
        typedResponse.ipfsHash_ = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
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
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AnnouncementRequestedEventResponse getAnnouncementRequestedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ANNOUNCEMENTREQUESTED_EVENT, log);
        AnnouncementRequestedEventResponse typedResponse = new AnnouncementRequestedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
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
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.startTime_ = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.endTime_ = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
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
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.startTime_ = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.endTime_ = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
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
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
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
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
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
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
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
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
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

    public RemoteFunctionCall<TransactionReceipt> addAnnouncement(byte[] ipfsHash,
            BigInteger startTime, BigInteger endTime) {
        final Function function = new Function(
                FUNC_ADDANNOUNCEMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash), 
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

    public RemoteFunctionCall<TransactionReceipt> deleteAnnouncement(byte[] ipfsHash) {
        final Function function = new Function(
                FUNC_DELETEANNOUNCEMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash)), 
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

    public RemoteFunctionCall<TransactionReceipt> getAnnouncement(byte[] ipfsHash) {
        final Function function = new Function(
                FUNC_GETANNOUNCEMENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateAnnouncementIpfsHash(byte[] ipfsHash_,
            byte[] ipfsHash) {
        final Function function = new Function(
                FUNC_UPDATEANNOUNCEMENTIPFSHASH, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash_), 
                new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updateAnnouncementTime(byte[] ipfsHash,
            BigInteger startTime, BigInteger endTime) {
        final Function function = new Function(
                FUNC_UPDATEANNOUNCEMENTTIME, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash), 
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

    public static class Announcement extends StaticStruct {
        public byte[] ipfsHash;

        public BigInteger startTime;

        public BigInteger endTime;

        public Announcement(byte[] ipfsHash, BigInteger startTime, BigInteger endTime) {
            super(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash), 
                    new org.web3j.abi.datatypes.generated.Uint256(startTime), 
                    new org.web3j.abi.datatypes.generated.Uint256(endTime));
            this.ipfsHash = ipfsHash;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Announcement(Bytes32 ipfsHash, Uint256 startTime, Uint256 endTime) {
            super(ipfsHash, startTime, endTime);
            this.ipfsHash = ipfsHash.getValue();
            this.startTime = startTime.getValue();
            this.endTime = endTime.getValue();
        }
    }

    public static class AnnouncementAddedEventResponse extends BaseEventResponse {
        public String from;

        public byte[] ipfsHash;

        public BigInteger startTime;

        public BigInteger endTime;

        public BigInteger time;
    }

    public static class AnnouncementDeletedEventResponse extends BaseEventResponse {
        public String from;

        public byte[] ipfsHash;

        public BigInteger time;
    }

    public static class AnnouncementIpfsHashModifiedEventResponse extends BaseEventResponse {
        public String from;

        public byte[] ipfsHash_;

        public byte[] ipfsHash;

        public BigInteger time;
    }

    public static class AnnouncementRequestedEventResponse extends BaseEventResponse {
        public String from;

        public byte[] ipfsHash;

        public BigInteger time;
    }

    public static class AnnouncementTimeModifiedEventResponse extends BaseEventResponse {
        public String from;

        public byte[] ipfsHash;

        public BigInteger startTime_;

        public BigInteger endTime_;

        public BigInteger startTime;

        public BigInteger endTime;

        public BigInteger time;
    }

    public static class UserAddedEventResponse extends BaseEventResponse {
        public String from;

        public String user;

        public BigInteger time;
    }

    public static class UserDeletedEventResponse extends BaseEventResponse {
        public String from;

        public String user;

        public String note;

        public BigInteger time;
    }
}
