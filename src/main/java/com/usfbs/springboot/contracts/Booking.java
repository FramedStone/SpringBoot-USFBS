package com.usfbs.springboot.contracts;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple8;
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
public class Booking extends Contract {
    public static final String BINARY = "608060405234801561001057600080fd5b50604051620021023803806200210283398101604081905261003191610056565b600080546001600160a01b0319166001600160a01b0392909216919091179055610086565b60006020828403121561006857600080fd5b81516001600160a01b038116811461007f57600080fd5b9392505050565b61206c80620000966000396000f3fe608060405234801561001057600080fd5b50600436106100935760003560e01c8063939793f311610066578063939793f314610113578063bf7ddd5b14610126578063f245ab9b14610139578063f663e3ac1461014c578063f67e44441461015457600080fd5b80631dab301e146100985780633c15b31c146100c85780637c8d6d9e146100e857806392153eff146100fe575b600080fd5b6100ab6100a6366004611618565b610175565b6040516100bf989796959493929190611697565b60405180910390f35b6100db6100d6366004611618565b6102ec565b6040516100bf9190611719565b6100f06103f6565b6040516100bf929190611733565b61011161010c3660046117cb565b6105ac565b005b610111610121366004611890565b6107cb565b6101116101343660046117cb565b6109ac565b6100db610147366004611618565b610b65565b6100f0610bd5565b6101676101623660046118e0565b610e12565b6040519081526020016100bf565b6001818154811061018557600080fd5b600091825260209091206009909102018054600182015460028301546003840180546001600160a01b039094169550919390929091906101c49061195f565b80601f01602080910402602001604051908101604052809291908181526020018280546101f09061195f565b801561023d5780601f106102125761010080835404028352916020019161023d565b820191906000526020600020905b81548152906001019060200180831161022057829003601f168201915b5050505050908060040180546102529061195f565b80601f016020809104026020016040519081016040528092919081815260200182805461027e9061195f565b80156102cb5780601f106102a0576101008083540402835291602001916102cb565b820191906000526020600020905b8154815290600101906020018083116102ae57829003601f168201915b50505050600583015460068401546007909401549293909290915060ff1688565b6060816001838154811061030257610302611999565b9060005260206000209060090201600101541461033a5760405162461bcd60e51b8152600401610331906119af565b60405180910390fd5b336001600160a01b03166001838154811061035757610357611999565b60009182526020909120600990910201546001600160a01b0316146103be5760405162461bcd60e51b815260206004820152601960248201527f496e76616c6964206163636573732028626f6f6b696e677329000000000000006044820152606401610331565b6103f0600183815481106103d4576103d4611999565b600091825260209091206007600990920201015460ff16611231565b92915050565b60005460609081906001600160a01b031633146104255760405162461bcd60e51b8152600401610331906119f1565b60015461046d5760405162461bcd60e51b8152602060048201526016602482015275656d7074792061727261792028626f6f6b696e67732960501b6044820152606401610331565b60015460009067ffffffffffffffff81111561048b5761048b6117ed565b6040519080825280602002602001820160405280156104b4578160200160208202803683370190505b5060015490915060009067ffffffffffffffff8111156104d6576104d66117ed565b60405190808252806020026020018201604052801561050957816020015b60608152602001906001900390816104f45790505b50905060005b6001548110156105a2576001818154811061052c5761052c611999565b90600052602060002090600902016001015483828151811061055057610550611999565b602002602001018181525050610572600182815481106103d4576103d4611999565b82828151811061058457610584611999565b6020026020010181905250808061059a90611a18565b91505061050f565b5090925090509091565b81600182815481106105c0576105c0611999565b906000526020600020906009020160020154146105ef5760405162461bcd60e51b815260040161033190611a3f565b806001828154811061060357610603611999565b906000526020600020906009020160010154146106325760405162461bcd60e51b8152600401610331906119af565b336001600160a01b03166001828154811061064f5761064f611999565b60009182526020909120600990910201546001600160a01b0316146106b65760405162461bcd60e51b815260206004820152601860248201527f4163636573732064656e6965642028626f6f6b696e67732900000000000000006044820152606401610331565b6004600182815481106106cb576106cb611999565b60009182526020909120600760099092020101805460ff191660018360048111156106f8576106f8611681565b021790555060008051602061201783398151915281836001848154811061072157610721611999565b90600052602060002090600902016003016001858154811061074557610745611999565b90600052602060002090600902016004016001868154811061076957610769611999565b9060005260206000209060090201600501546001878154811061078e5761078e611999565b9060005260206000209060090201600601546107aa6004611231565b426040516107bf989796959493929190611afd565b60405180910390a15050565b6000546001600160a01b031633146107f55760405162461bcd60e51b8152600401610331906119f1565b826001838154811061080957610809611999565b906000526020600020906009020160020154146108385760405162461bcd60e51b815260040161033190611a3f565b816001838154811061084c5761084c611999565b9060005260206000209060090201600101541461087b5760405162461bcd60e51b8152600401610331906119af565b6001828154811061088e5761088e611999565b6000918252602080832060086009909302019190910180546001810182559083529120016108bc8282611bec565b507fc21b8b77b0a6a011e5be1188bf0a7b9d5169b26df31960bc05d2ddd6f184b6648284600185815481106108f3576108f3611999565b90600052602060002090600902016003016001868154811061091757610917611999565b90600052602060002090600902016004016001878154811061093b5761093b611999565b9060005260206000209060090201600501546001888154811061096057610960611999565b90600052602060002090600902016006015461098860018a815481106103d4576103d4611999565b884260405161099f99989796959493929190611cac565b60405180910390a1505050565b6000546001600160a01b031633146109d65760405162461bcd60e51b8152600401610331906119f1565b81600182815481106109ea576109ea611999565b90600052602060002090600902016002015414610a195760405162461bcd60e51b815260040161033190611a3f565b8060018281548110610a2d57610a2d611999565b90600052602060002090600902016001015414610a5c5760405162461bcd60e51b8152600401610331906119af565b600260018281548110610a7157610a71611999565b60009182526020909120600760099092020101805460ff19166001836004811115610a9e57610a9e611681565b0217905550600080516020612017833981519152818360018481548110610ac757610ac7611999565b906000526020600020906009020160030160018581548110610aeb57610aeb611999565b906000526020600020906009020160040160018681548110610b0f57610b0f611999565b90600052602060002090600902016005015460018781548110610b3457610b34611999565b906000526020600020906009020160060154610b506002611231565b426040516107bf989796959493929190611d2c565b6000546060906001600160a01b03163314610b925760405162461bcd60e51b8152600401610331906119f1565b8160018381548110610ba657610ba6611999565b906000526020600020906009020160010154146103be5760405162461bcd60e51b8152600401610331906119af565b6001546060908190610c225760405162461bcd60e51b8152602060048201526016602482015275656d7074792061727261792028626f6f6b696e67732960501b6044820152606401610331565b6000805b600154811015610c8c57336001600160a01b031660018281548110610c4d57610c4d611999565b60009182526020909120600990910201546001600160a01b031603610c7a5781610c7681611a18565b9250505b80610c8481611a18565b915050610c26565b5060008167ffffffffffffffff811115610ca857610ca86117ed565b604051908082528060200260200182016040528015610cd1578160200160208202803683370190505b50905060008267ffffffffffffffff811115610cef57610cef6117ed565b604051908082528060200260200182016040528015610d2257816020015b6060815260200190600190039081610d0d5790505b5090506000805b600154811015610e0657336001600160a01b031660018281548110610d5057610d50611999565b60009182526020909120600990910201546001600160a01b031603610df45760018181548110610d8257610d82611999565b906000526020600020906009020160010154848381518110610da657610da6611999565b602002602001018181525050610dc8600182815481106103d4576103d4611999565b838381518110610dda57610dda611999565b60200260200101819052508180610df090611a18565b9250505b80610dfe81611a18565b915050610d29565b50919590945092505050565b600154600090815b600154811015610e665760018181548110610e3757610e37611999565b906000526020600020906009020160010154600003610e54578091505b80610e5e81611a18565b915050610e1a565b506000604051806101200160405280336001600160a01b0316815260200183815260200189815260200188815260200187815260200186815260200185815260200160016004811115610ebb57610ebb611681565b81526020016000604051908082528060200260200182016040528015610ef557816020015b6060815260200190600190039081610ee05790505b5090529050337f0e1bb2f317d9542bcb573fee924d642b568af6dbedf21ad2c485e8a1214c7800838a8a8a8a8a610f2c6001611231565b42604051610f41989796959493929190611dcc565b60405180910390a2610f5587878787611388565b15610fa557600060e0820181905250600080516020612017833981519152828989898989610f836000611231565b42604051610f98989796959493929190611e46565b60405180910390a1610fec565b600260e0820181905250600080516020612017833981519152828989898989610fce6002611231565b42604051610fe3989796959493929190611eda565b60405180910390a15b60015482036111485760018054808201825560009190915281517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf6600990920291820180546001600160a01b0319166001600160a01b0390921691909117815560208301517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf783015560408301517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf8830155606083015183927fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf901906110d29082611bec565b50608082015160048201906110e79082611bec565b5060a0820151600582015560c0820151600682015560e082015160078201805460ff1916600183600481111561111f5761111f611681565b02179055506101008201518051611140916008840191602090910190611553565b505050611226565b806001838154811061115c5761115c611999565b600091825260209182902083516009929092020180546001600160a01b0319166001600160a01b0390921691909117815590820151600182015560408201516002820155606082015160038201906111b49082611bec565b50608082015160048201906111c99082611bec565b5060a0820151600582015560c0820151600682015560e082015160078201805460ff1916600183600481111561120157611201611681565b02179055506101008201518051611222916008840191602090910190611553565b5050505b509695505050505050565b6060600082600481111561124757611247611681565b03611270575050604080518082019091526008815267185c1c1c9bdd995960c21b602082015290565b600182600481111561128457611284611681565b036112ac57505060408051808201909152600781526670656e64696e6760c81b602082015290565b60028260048111156112c0576112c0611681565b036112e95750506040805180820190915260088152671c995a9958dd195960c21b602082015290565b60038260048111156112fd576112fd611681565b0361132757505060408051808201909152600981526818dbdb5c1b195d195960ba1b602082015290565b600482600481111561133b5761133b611681565b0361136557505060408051808201909152600981526818d85b98d95b1b195960ba1b602082015290565b50506040805180820190915260078152663ab735b737bbb760c91b602082015290565b6000805b60015481101561154557856040516020016113a79190611f84565b60405160208183030381529060405280519060200120600182815481106113d0576113d0611999565b90600052602060002090600902016003016040516020016113f19190611fa0565b604051602081830303815290604052805190602001201480156114825750846040516020016114209190611f84565b604051602081830303815290604052805190602001206001828154811061144957611449611999565b906000526020600020906009020160040160405160200161146a9190611fa0565b60405160208183030381529060405280519060200120145b1561153357826001828154811061149b5761149b611999565b9060005260206000209060090201600501541080156114dd575083600182815481106114c9576114c9611999565b906000526020600020906009020160060154115b801561152457506003600182815481106114f9576114f9611999565b600091825260209091206007600990920201015460ff16600481111561152157611521611681565b14155b1561153357600091505061154b565b8061153d81611a18565b91505061138c565b50600190505b949350505050565b828054828255906000526020600020908101928215611599579160200282015b8281111561159957825182906115899082611bec565b5091602001919060010190611573565b506115a59291506115a9565b5090565b808211156115a55760006115bd82826115c6565b506001016115a9565b5080546115d29061195f565b6000825580601f106115e2575050565b601f0160209004906000526020600020908101906116009190611603565b50565b5b808211156115a55760008155600101611604565b60006020828403121561162a57600080fd5b5035919050565b60005b8381101561164c578181015183820152602001611634565b50506000910152565b6000815180845261166d816020860160208601611631565b601f01601f19169290920160200192915050565b634e487b7160e01b600052602160045260246000fd5b600061010060018060a01b038b1683528960208401528860408401528060608401526116c581840189611655565b905082810360808401526116d98188611655565b9150508460a08301528360c08301526005831061170657634e487b7160e01b600052602160045260246000fd5b8260e08301529998505050505050505050565b60208152600061172c6020830184611655565b9392505050565b604080825283519082018190526000906020906060840190828701845b8281101561176c57815184529284019290840190600101611750565b50505083810382850152845180825282820190600581901b8301840187850160005b838110156117bc57601f198684030185526117aa838351611655565b9487019492509086019060010161178e565b50909998505050505050505050565b600080604083850312156117de57600080fd5b50508035926020909101359150565b634e487b7160e01b600052604160045260246000fd5b600082601f83011261181457600080fd5b813567ffffffffffffffff8082111561182f5761182f6117ed565b604051601f8301601f19908116603f01168101908282118183101715611857576118576117ed565b8160405283815286602085880101111561187057600080fd5b836020870160208301376000602085830101528094505050505092915050565b6000806000606084860312156118a557600080fd5b8335925060208401359150604084013567ffffffffffffffff8111156118ca57600080fd5b6118d686828701611803565b9150509250925092565b600080600080600060a086880312156118f857600080fd5b85359450602086013567ffffffffffffffff8082111561191757600080fd5b61192389838a01611803565b9550604088013591508082111561193957600080fd5b5061194688828901611803565b9598949750949560608101359550608001359392505050565b600181811c9082168061197357607f821691505b60208210810361199357634e487b7160e01b600052602260045260246000fd5b50919050565b634e487b7160e01b600052603260045260246000fd5b60208082526022908201527f626f6f6b696e67496420646f65736e2774206d617463682028626f6f6b696e67604082015261732960f01b606082015260800190565b6020808252600d908201526c1058d8d95cdcc819195b9a5959609a1b604082015260600190565b600060018201611a3857634e487b7160e01b600052601160045260246000fd5b5060010190565b60208082526021908201527f697066734861736820646f65736e2774206d617463682028626f6f6b696e67736040820152602960f81b606082015260800190565b60008154611a8d8161195f565b808552602060018381168015611aaa5760018114611ac457611af2565b60ff1985168884015283151560051b880183019550611af2565b866000528260002060005b85811015611aea5781548a8201860152908301908401611acf565b890184019650505b505050505092915050565b60006101208a8352896020840152806040840152611b1d8184018a611a80565b90508281036060840152611b318189611a80565b90508660808401528560a084015282810360c0840152611b518186611655565b83810360e0850152601a81527f43616e63656c6c65642062792075736572206d616e75616c6c790000000000006020820152610100909301939093525060400198975050505050505050565b601f821115611be757600081815260208120601f850160051c81016020861015611bc45750805b601f850160051c820191505b81811015611be357828155600101611bd0565b5050505b505050565b815167ffffffffffffffff811115611c0657611c066117ed565b611c1a81611c14845461195f565b84611b9d565b602080601f831160018114611c4f5760008415611c375750858301515b600019600386901b1c1916600185901b178555611be3565b600085815260208120601f198616915b82811015611c7e57888601518255948401946001909101908401611c5f565b5085821015611c9c5787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b60006101208b83528a6020840152806040840152611ccc8184018b611a80565b90508281036060840152611ce0818a611a80565b90508760808401528660a084015282810360c0840152611d008187611655565b905082810360e0840152611d148186611655565b915050826101008301529a9950505050505050505050565b60006101208a8352896020840152806040840152611d4c8184018a611a80565b90508281036060840152611d608189611a80565b90508660808401528560a084015282810360c0840152611d808186611655565b83810360e0850152601a81527f52656a65637465642062792061646d696e206d616e75616c6c790000000000006020820152610100909301939093525060400198975050505050505050565b60006101208a8352896020840152806040840152611dec8184018a611655565b90508281036060840152611e008189611655565b90508660808401528560a084015282810360c0840152611e208186611655565b83810360e085015260008152610100909301939093525060200198975050505050505050565b60006101208a8352896020840152806040840152611e668184018a611655565b90508281036060840152611e7a8189611655565b90508660808401528560a084015282810360c0840152611e9a8186611655565b83810360e08501526011815270417070726f766564202873797374656d2960781b6020820152610100909301939093525060400198975050505050505050565b60006101208a8352896020840152806040840152611efa8184018a611655565b90508281036060840152611f0e8189611655565b90508660808401528560a084015282810360c0840152611f2e8186611655565b83810360e0850152602181527f52656a65637465642064756520746f20636f6e666c696374202873797374656d6020820152602960f81b6040820152610100909301939093525060600198975050505050505050565b60008251611f96818460208701611631565b9190910192915050565b6000808354611fae8161195f565b60018281168015611fc65760018114611fdb5761200a565b60ff198416875282151583028701945061200a565b8760005260208060002060005b858110156120015781548a820152908401908201611fe8565b50505082870194505b5092969550505050505056feec4c4e08cd6f7bdf950c2e24c35f3bd83f121e67c8c09ff33e9b072e25ffe8d0a26469706673582212209bc3492177acb92e9854dbea7c0dcf1a621b0770f0acf30e1ca90b8c3024a16964736f6c63430008130033";

    private static String librariesLinkedBinary;

    public static final String FUNC_APPENDBOOKINGNOTE = "appendBookingNote";

    public static final String FUNC_BOOKINGS = "bookings";

    public static final String FUNC_CANCELBOOKING = "cancelBooking";

    public static final String FUNC_CREATEBOOKING = "createBooking";

    public static final String FUNC_GETALLBOOKINGSTATUS = "getAllBookingStatus";

    public static final String FUNC_GETALLBOOKINGSTATUS_ = "getAllBookingStatus_";

    public static final String FUNC_GETBOOKINGSTATUS = "getBookingStatus";

    public static final String FUNC_GETBOOKINGSTATUS_ = "getBookingStatus_";

    public static final String FUNC_REJECTBOOKING = "rejectBooking";

    public static final Event BOOKINGCREATED_EVENT = new Event("bookingCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event BOOKINGDELETED_EVENT = new Event("bookingDeleted", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event BOOKINGNOTEAPPENDED_EVENT = new Event("bookingNoteAppended", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event BOOKINGSTATUSUPDATED_EVENT = new Event("bookingStatusUpdated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected Booking(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Booking(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Booking(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Booking(String contractAddress, Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<BookingCreatedEventResponse> getBookingCreatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BOOKINGCREATED_EVENT, transactionReceipt);
        ArrayList<BookingCreatedEventResponse> responses = new ArrayList<BookingCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BookingCreatedEventResponse typedResponse = new BookingCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.bookingId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.sportFacility = (String) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.court = (String) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            typedResponse.status = (String) eventValues.getNonIndexedValues().get(6).getValue();
            typedResponse.note = (String) eventValues.getNonIndexedValues().get(7).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(8).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BookingCreatedEventResponse getBookingCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BOOKINGCREATED_EVENT, log);
        BookingCreatedEventResponse typedResponse = new BookingCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.bookingId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.sportFacility = (String) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.court = (String) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
        typedResponse.status = (String) eventValues.getNonIndexedValues().get(6).getValue();
        typedResponse.note = (String) eventValues.getNonIndexedValues().get(7).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(8).getValue();
        return typedResponse;
    }

    public Flowable<BookingCreatedEventResponse> bookingCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBookingCreatedEventFromLog(log));
    }

    public Flowable<BookingCreatedEventResponse> bookingCreatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BOOKINGCREATED_EVENT));
        return bookingCreatedEventFlowable(filter);
    }

    public static List<BookingDeletedEventResponse> getBookingDeletedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BOOKINGDELETED_EVENT, transactionReceipt);
        ArrayList<BookingDeletedEventResponse> responses = new ArrayList<BookingDeletedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BookingDeletedEventResponse typedResponse = new BookingDeletedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.sportFacility = (String) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.court = (String) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            typedResponse.status = (String) eventValues.getNonIndexedValues().get(6).getValue();
            typedResponse.note = (String) eventValues.getNonIndexedValues().get(7).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(8).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BookingDeletedEventResponse getBookingDeletedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BOOKINGDELETED_EVENT, log);
        BookingDeletedEventResponse typedResponse = new BookingDeletedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.sportFacility = (String) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.court = (String) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
        typedResponse.status = (String) eventValues.getNonIndexedValues().get(6).getValue();
        typedResponse.note = (String) eventValues.getNonIndexedValues().get(7).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(8).getValue();
        return typedResponse;
    }

    public Flowable<BookingDeletedEventResponse> bookingDeletedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBookingDeletedEventFromLog(log));
    }

    public Flowable<BookingDeletedEventResponse> bookingDeletedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BOOKINGDELETED_EVENT));
        return bookingDeletedEventFlowable(filter);
    }

    public static List<BookingNoteAppendedEventResponse> getBookingNoteAppendedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BOOKINGNOTEAPPENDED_EVENT, transactionReceipt);
        ArrayList<BookingNoteAppendedEventResponse> responses = new ArrayList<BookingNoteAppendedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BookingNoteAppendedEventResponse typedResponse = new BookingNoteAppendedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.sportFacility = (String) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.court = (String) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            typedResponse.status = (String) eventValues.getNonIndexedValues().get(6).getValue();
            typedResponse.note = (String) eventValues.getNonIndexedValues().get(7).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(8).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BookingNoteAppendedEventResponse getBookingNoteAppendedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BOOKINGNOTEAPPENDED_EVENT, log);
        BookingNoteAppendedEventResponse typedResponse = new BookingNoteAppendedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.sportFacility = (String) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.court = (String) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
        typedResponse.status = (String) eventValues.getNonIndexedValues().get(6).getValue();
        typedResponse.note = (String) eventValues.getNonIndexedValues().get(7).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(8).getValue();
        return typedResponse;
    }

    public Flowable<BookingNoteAppendedEventResponse> bookingNoteAppendedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBookingNoteAppendedEventFromLog(log));
    }

    public Flowable<BookingNoteAppendedEventResponse> bookingNoteAppendedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BOOKINGNOTEAPPENDED_EVENT));
        return bookingNoteAppendedEventFlowable(filter);
    }

    public static List<BookingStatusUpdatedEventResponse> getBookingStatusUpdatedEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(BOOKINGSTATUSUPDATED_EVENT, transactionReceipt);
        ArrayList<BookingStatusUpdatedEventResponse> responses = new ArrayList<BookingStatusUpdatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BookingStatusUpdatedEventResponse typedResponse = new BookingStatusUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.bookingId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.sportFacility = (String) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.court = (String) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
            typedResponse.status = (String) eventValues.getNonIndexedValues().get(6).getValue();
            typedResponse.note = (String) eventValues.getNonIndexedValues().get(7).getValue();
            typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(8).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static BookingStatusUpdatedEventResponse getBookingStatusUpdatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(BOOKINGSTATUSUPDATED_EVENT, log);
        BookingStatusUpdatedEventResponse typedResponse = new BookingStatusUpdatedEventResponse();
        typedResponse.log = log;
        typedResponse.bookingId = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.ipfsHash = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.sportFacility = (String) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.court = (String) eventValues.getNonIndexedValues().get(3).getValue();
        typedResponse.startTime = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
        typedResponse.endTime = (BigInteger) eventValues.getNonIndexedValues().get(5).getValue();
        typedResponse.status = (String) eventValues.getNonIndexedValues().get(6).getValue();
        typedResponse.note = (String) eventValues.getNonIndexedValues().get(7).getValue();
        typedResponse.time = (BigInteger) eventValues.getNonIndexedValues().get(8).getValue();
        return typedResponse;
    }

    public Flowable<BookingStatusUpdatedEventResponse> bookingStatusUpdatedEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getBookingStatusUpdatedEventFromLog(log));
    }

    public Flowable<BookingStatusUpdatedEventResponse> bookingStatusUpdatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(BOOKINGSTATUSUPDATED_EVENT));
        return bookingStatusUpdatedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> appendBookingNote(byte[] ipfsHash,
            BigInteger bookingId, String note) {
        final Function function = new Function(
                FUNC_APPENDBOOKINGNOTE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash), 
                new org.web3j.abi.datatypes.generated.Uint256(bookingId), 
                new org.web3j.abi.datatypes.Utf8String(note)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple8<String, BigInteger, byte[], String, String, BigInteger, BigInteger, BigInteger>> bookings(
            BigInteger param0) {
        final Function function = new Function(FUNC_BOOKINGS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Bytes32>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}));
        return new RemoteFunctionCall<Tuple8<String, BigInteger, byte[], String, String, BigInteger, BigInteger, BigInteger>>(function,
                new Callable<Tuple8<String, BigInteger, byte[], String, String, BigInteger, BigInteger, BigInteger>>() {
                    @Override
                    public Tuple8<String, BigInteger, byte[], String, String, BigInteger, BigInteger, BigInteger> call(
                            ) throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple8<String, BigInteger, byte[], String, String, BigInteger, BigInteger, BigInteger>(
                                (String) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                (byte[]) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (String) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue(), 
                                (BigInteger) results.get(7).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> cancelBooking(byte[] ipfsHash,
            BigInteger bookingId) {
        final Function function = new Function(
                FUNC_CANCELBOOKING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash), 
                new org.web3j.abi.datatypes.generated.Uint256(bookingId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createBooking(byte[] ipfsHash,
            String sportFacility, String court, BigInteger startTime, BigInteger endTime) {
        final Function function = new Function(
                FUNC_CREATEBOOKING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash), 
                new org.web3j.abi.datatypes.Utf8String(sportFacility), 
                new org.web3j.abi.datatypes.Utf8String(court), 
                new org.web3j.abi.datatypes.generated.Uint256(startTime), 
                new org.web3j.abi.datatypes.generated.Uint256(endTime)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple2<List<BigInteger>, List<String>>> getAllBookingStatus() {
        final Function function = new Function(FUNC_GETALLBOOKINGSTATUS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}, new TypeReference<DynamicArray<Utf8String>>() {}));
        return new RemoteFunctionCall<Tuple2<List<BigInteger>, List<String>>>(function,
                new Callable<Tuple2<List<BigInteger>, List<String>>>() {
                    @Override
                    public Tuple2<List<BigInteger>, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<List<BigInteger>, List<String>>(
                                convertToNative((List<Uint256>) results.get(0).getValue()), 
                                convertToNative((List<Utf8String>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<Tuple2<List<BigInteger>, List<String>>> getAllBookingStatus_() {
        final Function function = new Function(FUNC_GETALLBOOKINGSTATUS_, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint256>>() {}, new TypeReference<DynamicArray<Utf8String>>() {}));
        return new RemoteFunctionCall<Tuple2<List<BigInteger>, List<String>>>(function,
                new Callable<Tuple2<List<BigInteger>, List<String>>>() {
                    @Override
                    public Tuple2<List<BigInteger>, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<List<BigInteger>, List<String>>(
                                convertToNative((List<Uint256>) results.get(0).getValue()), 
                                convertToNative((List<Utf8String>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteFunctionCall<String> getBookingStatus(BigInteger bookingId) {
        final Function function = new Function(FUNC_GETBOOKINGSTATUS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(bookingId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<String> getBookingStatus_(BigInteger bookingId) {
        final Function function = new Function(FUNC_GETBOOKINGSTATUS_, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(bookingId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> rejectBooking(byte[] ipfsHash,
            BigInteger bookingId) {
        final Function function = new Function(
                FUNC_REJECTBOOKING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash), 
                new org.web3j.abi.datatypes.generated.Uint256(bookingId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static Booking load(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return new Booking(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Booking load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Booking(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static Booking load(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return new Booking(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Booking load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Booking(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<Booking> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider, String admin) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, admin)));
        return deployRemoteCall(Booking.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    public static RemoteCall<Booking> deploy(Web3j web3j, TransactionManager transactionManager,
            ContractGasProvider contractGasProvider, String admin) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, admin)));
        return deployRemoteCall(Booking.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Booking> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit, String admin) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, admin)));
        return deployRemoteCall(Booking.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<Booking> deploy(Web3j web3j, TransactionManager transactionManager,
            BigInteger gasPrice, BigInteger gasLimit, String admin) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, admin)));
        return deployRemoteCall(Booking.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), encodedConstructor);
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

    public static class BookingCreatedEventResponse extends BaseEventResponse {
        public String from;

        public BigInteger bookingId;

        public byte[] ipfsHash;

        public String sportFacility;

        public String court;

        public BigInteger startTime;

        public BigInteger endTime;

        public String status;

        public String note;

        public BigInteger time;
    }

    public static class BookingDeletedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;

        public byte[] ipfsHash;

        public String sportFacility;

        public String court;

        public BigInteger startTime;

        public BigInteger endTime;

        public String status;

        public String note;

        public BigInteger time;
    }

    public static class BookingNoteAppendedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;

        public byte[] ipfsHash;

        public String sportFacility;

        public String court;

        public BigInteger startTime;

        public BigInteger endTime;

        public String status;

        public String note;

        public BigInteger time;
    }

    public static class BookingStatusUpdatedEventResponse extends BaseEventResponse {
        public BigInteger bookingId;

        public byte[] ipfsHash;

        public String sportFacility;

        public String court;

        public BigInteger startTime;

        public BigInteger endTime;

        public String status;

        public String note;

        public BigInteger time;
    }
}
