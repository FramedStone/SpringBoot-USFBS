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
    public static final String BINARY = "608060405234801561001057600080fd5b50604051620025673803806200256783398101604081905261003191610056565b600080546001600160a01b0319166001600160a01b0392909216919091179055610086565b60006020828403121561006857600080fd5b81516001600160a01b038116811461007f57600080fd5b9392505050565b6124d180620000966000396000f3fe608060405234801561001057600080fd5b506004361061009e5760003560e01c8063939793f311610066578063939793f314610131578063bf7ddd5b14610144578063f245ab9b14610157578063f663e3ac1461016a578063f67e44441461017257600080fd5b80631dab301e146100a35780633c15b31c146100d35780635d874a91146100f35780637c8d6d9e1461010857806392153eff1461011e575b600080fd5b6100b66100b13660046119a9565b610193565b6040516100ca989796959493929190611a28565b60405180910390f35b6100e66100e13660046119a9565b61030a565b6040516100ca9190611aaa565b610106610101366004611ac4565b610414565b005b610110610824565b6040516100ca929190611ae6565b61010661012c366004611ac4565b6109da565b61010661013f366004611c21565b610cdf565b610106610152366004611ac4565b610ec0565b6100e66101653660046119a9565b611079565b6101106110e9565b610185610180366004611c71565b611326565b6040519081526020016100ca565b600181815481106101a357600080fd5b600091825260209091206009909102018054600182015460028301546003840180546001600160a01b039094169550919390929091906101e290611cf0565b80601f016020809104026020016040519081016040528092919081815260200182805461020e90611cf0565b801561025b5780601f106102305761010080835404028352916020019161025b565b820191906000526020600020905b81548152906001019060200180831161023e57829003601f168201915b50505050509080600401805461027090611cf0565b80601f016020809104026020016040519081016040528092919081815260200182805461029c90611cf0565b80156102e95780601f106102be576101008083540402835291602001916102e9565b820191906000526020600020905b8154815290600101906020018083116102cc57829003601f168201915b50505050600583015460068401546007909401549293909290915060ff1688565b6060816001838154811061032057610320611d2a565b906000526020600020906009020160010154146103585760405162461bcd60e51b815260040161034f90611d40565b60405180910390fd5b336001600160a01b03166001838154811061037557610375611d2a565b60009182526020909120600990910201546001600160a01b0316146103dc5760405162461bcd60e51b815260206004820152601960248201527f496e76616c6964206163636573732028626f6f6b696e67732900000000000000604482015260640161034f565b61040e600183815481106103f2576103f2611d2a565b600091825260209091206007600990920201015460ff166115eb565b92915050565b6000546001600160a01b0316331461043e5760405162461bcd60e51b815260040161034f90611d82565b600154811061048f5760405162461bcd60e51b815260206004820152601d60248201527f496e646578206f7574206f6620626f756e642028626f6f6b696e677329000000604482015260640161034f565b81600182815481106104a3576104a3611d2a565b906000526020600020906009020160020154146104d25760405162461bcd60e51b815260040161034f90611da9565b80600182815481106104e6576104e6611d2a565b906000526020600020906009020160010154146105155760405162461bcd60e51b815260040161034f90611d40565b6001818154811061052857610528611d2a565b9060005260206000209060090201600601544210156105945760405162461bcd60e51b815260206004820152602260248201527f626f6f6b696e67206e6f742079657420657870697265642028626f6f6b696e67604482015261732960f01b606482015260840161034f565b6000600182815481106105a9576105a9611d2a565b906000526020600020906009020160030180546105c590611cf0565b80601f01602080910402602001604051908101604052809291908181526020018280546105f190611cf0565b801561063e5780601f106106135761010080835404028352916020019161063e565b820191906000526020600020905b81548152906001019060200180831161062157829003601f168201915b5050505050905060006001838154811061065a5761065a611d2a565b9060005260206000209060090201600401805461067690611cf0565b80601f01602080910402602001604051908101604052809291908181526020018280546106a290611cf0565b80156106ef5780601f106106c4576101008083540402835291602001916106ef565b820191906000526020600020905b8154815290600101906020018083116106d257829003601f168201915b5050505050905060006001848154811061070b5761070b611d2a565b906000526020600020906009020160050154905060006001858154811061073457610734611d2a565b90600052602060002090600902016006015490506001858154811061075b5761075b611d2a565b60009182526020822060099091020180546001600160a01b031916815560018101829055600281018290559061079460038301826118c6565b6107a26004830160006118c6565b6000600583018190556006830181905560078301805460ff191690556107cc906008840190611903565b50507f95ba5894d801086326249705bc97694be8d367c97e40c84be6382252f92778358587868686866107ff60036115eb565b42604051610814989796959493929190611dea565b60405180910390a1505050505050565b60005460609081906001600160a01b031633146108535760405162461bcd60e51b815260040161034f90611d82565b60015461089b5760405162461bcd60e51b8152602060048201526016602482015275656d7074792061727261792028626f6f6b696e67732960501b604482015260640161034f565b60015460009067ffffffffffffffff8111156108b9576108b9611b7e565b6040519080825280602002602001820160405280156108e2578160200160208202803683370190505b5060015490915060009067ffffffffffffffff81111561090457610904611b7e565b60405190808252806020026020018201604052801561093757816020015b60608152602001906001900390816109225790505b50905060005b6001548110156109d0576001818154811061095a5761095a611d2a565b90600052602060002090600902016001015483828151811061097e5761097e611d2a565b6020026020010181815250506109a0600182815481106103f2576103f2611d2a565b8282815181106109b2576109b2611d2a565b602002602001018190525080806109c890611e8a565b91505061093d565b5090925090509091565b81600182815481106109ee576109ee611d2a565b90600052602060002090600902016002015414610a1d5760405162461bcd60e51b815260040161034f90611da9565b8060018281548110610a3157610a31611d2a565b90600052602060002090600902016001015414610a605760405162461bcd60e51b815260040161034f90611d40565b336001600160a01b031660018281548110610a7d57610a7d611d2a565b60009182526020909120600990910201546001600160a01b031614610ae45760405162461bcd60e51b815260206004820152601860248201527f4163636573732064656e6965642028626f6f6b696e6773290000000000000000604482015260640161034f565b600460018281548110610af957610af9611d2a565b60009182526020909120600760099092020101805460ff19166001836004811115610b2657610b26611a12565b021790555060008051602061247c833981519152818360018481548110610b4f57610b4f611d2a565b906000526020600020906009020160030160018581548110610b7357610b73611d2a565b906000526020600020906009020160040160018681548110610b9757610b97611d2a565b90600052602060002090600902016005015460018781548110610bbc57610bbc611d2a565b906000526020600020906009020160060154610bd860046115eb565b42604051610bed989796959493929190611f2e565b60405180910390a1610bff8282610414565b7f95ba5894d801086326249705bc97694be8d367c97e40c84be6382252f9277835818360018481548110610c3557610c35611d2a565b906000526020600020906009020160030160018581548110610c5957610c59611d2a565b906000526020600020906009020160040160018681548110610c7d57610c7d611d2a565b90600052602060002090600902016005015460018781548110610ca257610ca2611d2a565b906000526020600020906009020160060154610cbe60036115eb565b42604051610cd3989796959493929190611fce565b60405180910390a15050565b6000546001600160a01b03163314610d095760405162461bcd60e51b815260040161034f90611d82565b8260018381548110610d1d57610d1d611d2a565b90600052602060002090600902016002015414610d4c5760405162461bcd60e51b815260040161034f90611da9565b8160018381548110610d6057610d60611d2a565b90600052602060002090600902016001015414610d8f5760405162461bcd60e51b815260040161034f90611d40565b60018281548110610da257610da2611d2a565b600091825260208083206008600990930201919091018054600181018255908352912001610dd08282612051565b507fc21b8b77b0a6a011e5be1188bf0a7b9d5169b26df31960bc05d2ddd6f184b664828460018581548110610e0757610e07611d2a565b906000526020600020906009020160030160018681548110610e2b57610e2b611d2a565b906000526020600020906009020160040160018781548110610e4f57610e4f611d2a565b90600052602060002090600902016005015460018881548110610e7457610e74611d2a565b906000526020600020906009020160060154610e9c60018a815481106103f2576103f2611d2a565b8842604051610eb399989796959493929190612111565b60405180910390a1505050565b6000546001600160a01b03163314610eea5760405162461bcd60e51b815260040161034f90611d82565b8160018281548110610efe57610efe611d2a565b90600052602060002090600902016002015414610f2d5760405162461bcd60e51b815260040161034f90611da9565b8060018281548110610f4157610f41611d2a565b90600052602060002090600902016001015414610f705760405162461bcd60e51b815260040161034f90611d40565b600260018281548110610f8557610f85611d2a565b60009182526020909120600760099092020101805460ff19166001836004811115610fb257610fb2611a12565b021790555060008051602061247c833981519152818360018481548110610fdb57610fdb611d2a565b906000526020600020906009020160030160018581548110610fff57610fff611d2a565b90600052602060002090600902016004016001868154811061102357611023611d2a565b9060005260206000209060090201600501546001878154811061104857611048611d2a565b90600052602060002090600902016006015461106460026115eb565b42604051610cd3989796959493929190612191565b6000546060906001600160a01b031633146110a65760405162461bcd60e51b815260040161034f90611d82565b81600183815481106110ba576110ba611d2a565b906000526020600020906009020160010154146103dc5760405162461bcd60e51b815260040161034f90611d40565b60015460609081906111365760405162461bcd60e51b8152602060048201526016602482015275656d7074792061727261792028626f6f6b696e67732960501b604482015260640161034f565b6000805b6001548110156111a057336001600160a01b03166001828154811061116157611161611d2a565b60009182526020909120600990910201546001600160a01b03160361118e578161118a81611e8a565b9250505b8061119881611e8a565b91505061113a565b5060008167ffffffffffffffff8111156111bc576111bc611b7e565b6040519080825280602002602001820160405280156111e5578160200160208202803683370190505b50905060008267ffffffffffffffff81111561120357611203611b7e565b60405190808252806020026020018201604052801561123657816020015b60608152602001906001900390816112215790505b5090506000805b60015481101561131a57336001600160a01b03166001828154811061126457611264611d2a565b60009182526020909120600990910201546001600160a01b031603611308576001818154811061129657611296611d2a565b9060005260206000209060090201600101548483815181106112ba576112ba611d2a565b6020026020010181815250506112dc600182815481106103f2576103f2611d2a565b8383815181106112ee576112ee611d2a565b6020026020010181905250818061130490611e8a565b9250505b8061131281611e8a565b91505061123d565b50919590945092505050565b60018054604080516101208101825233815260208082018490528183018a9052606082018990526080820188905260a0820187905260c0820186905260e08201949094528151600080825294810190925283916101008201908361139a565b60608152602001906001900390816113855790505b5090529050337f0e1bb2f317d9542bcb573fee924d642b568af6dbedf21ad2c485e8a1214c7800838a8a8a8a8a6113d160016115eb565b426040516113e6989796959493929190612231565b60405180910390a26113fa87878787611742565b1561144a57600060e082018190525060008051602061247c83398151915282898989898961142860006115eb565b4260405161143d9897969594939291906122ab565b60405180910390a1611491565b600260e082018190525060008051602061247c83398151915282898989898961147360026115eb565b4260405161148898979695949392919061233f565b60405180910390a15b60018054808201825560009190915281517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf6600990920291820180546001600160a01b0319166001600160a01b0390921691909117815560208301517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf783015560408301517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf8830155606083015183927fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf9019061156e9082612051565b50608082015160048201906115839082612051565b5060a0820151600582015560c0820151600682015560e082015160078201805460ff191660018360048111156115bb576115bb611a12565b021790555061010082015180516115dc916008840191602090910190611921565b50929998505050505050505050565b6060600082600481111561160157611601611a12565b0361162a575050604080518082019091526008815267185c1c1c9bdd995960c21b602082015290565b600182600481111561163e5761163e611a12565b0361166657505060408051808201909152600781526670656e64696e6760c81b602082015290565b600282600481111561167a5761167a611a12565b036116a35750506040805180820190915260088152671c995a9958dd195960c21b602082015290565b60038260048111156116b7576116b7611a12565b036116e157505060408051808201909152600981526818dbdb5c1b195d195960ba1b602082015290565b60048260048111156116f5576116f5611a12565b0361171f57505060408051808201909152600981526818d85b98d95b1b195960ba1b602082015290565b50506040805180820190915260078152663ab735b737bbb760c91b602082015290565b6000805b6001548110156118b8578560405160200161176191906123e9565b604051602081830303815290604052805190602001206001828154811061178a5761178a611d2a565b90600052602060002090600902016003016040516020016117ab9190612405565b6040516020818303038152906040528051906020012014801561183c5750846040516020016117da91906123e9565b604051602081830303815290604052805190602001206001828154811061180357611803611d2a565b90600052602060002090600902016004016040516020016118249190612405565b60405160208183030381529060405280519060200120145b156118a657826001828154811061185557611855611d2a565b9060005260206000209060090201600501541080156118975750836001828154811061188357611883611d2a565b906000526020600020906009020160060154115b156118a65760009150506118be565b806118b081611e8a565b915050611746565b50600190505b949350505050565b5080546118d290611cf0565b6000825580601f106118e2575050565b601f0160209004906000526020600020908101906119009190611977565b50565b5080546000825590600052602060002090810190611900919061198c565b828054828255906000526020600020908101928215611967579160200282015b8281111561196757825182906119579082612051565b5091602001919060010190611941565b5061197392915061198c565b5090565b5b808211156119735760008155600101611978565b808211156119735760006119a082826118c6565b5060010161198c565b6000602082840312156119bb57600080fd5b5035919050565b60005b838110156119dd5781810151838201526020016119c5565b50506000910152565b600081518084526119fe8160208601602086016119c2565b601f01601f19169290920160200192915050565b634e487b7160e01b600052602160045260246000fd5b600061010060018060a01b038b168352896020840152886040840152806060840152611a56818401896119e6565b90508281036080840152611a6a81886119e6565b9150508460a08301528360c083015260058310611a9757634e487b7160e01b600052602160045260246000fd5b8260e08301529998505050505050505050565b602081526000611abd60208301846119e6565b9392505050565b60008060408385031215611ad757600080fd5b50508035926020909101359150565b604080825283519082018190526000906020906060840190828701845b82811015611b1f57815184529284019290840190600101611b03565b50505083810382850152845180825282820190600581901b8301840187850160005b83811015611b6f57601f19868403018552611b5d8383516119e6565b94870194925090860190600101611b41565b50909998505050505050505050565b634e487b7160e01b600052604160045260246000fd5b600082601f830112611ba557600080fd5b813567ffffffffffffffff80821115611bc057611bc0611b7e565b604051601f8301601f19908116603f01168101908282118183101715611be857611be8611b7e565b81604052838152866020858801011115611c0157600080fd5b836020870160208301376000602085830101528094505050505092915050565b600080600060608486031215611c3657600080fd5b8335925060208401359150604084013567ffffffffffffffff811115611c5b57600080fd5b611c6786828701611b94565b9150509250925092565b600080600080600060a08688031215611c8957600080fd5b85359450602086013567ffffffffffffffff80821115611ca857600080fd5b611cb489838a01611b94565b95506040880135915080821115611cca57600080fd5b50611cd788828901611b94565b9598949750949560608101359550608001359392505050565b600181811c90821680611d0457607f821691505b602082108103611d2457634e487b7160e01b600052602260045260246000fd5b50919050565b634e487b7160e01b600052603260045260246000fd5b60208082526022908201527f626f6f6b696e67496420646f65736e2774206d617463682028626f6f6b696e67604082015261732960f01b606082015260800190565b6020808252600d908201526c1058d8d95cdcc819195b9a5959609a1b604082015260600190565b60208082526021908201527f697066734861736820646f65736e2774206d617463682028626f6f6b696e67736040820152602960f81b606082015260800190565b60006101208a8352896020840152806040840152611e0a8184018a6119e6565b90508281036060840152611e1e81896119e6565b90508660808401528560a084015282810360c0840152611e3e81866119e6565b83810360e0850152601981527f44656c65746564206f6e2d636861696e202873797374656d29000000000000006020820152610100909301939093525060400198975050505050505050565b600060018201611eaa57634e487b7160e01b600052601160045260246000fd5b5060010190565b60008154611ebe81611cf0565b808552602060018381168015611edb5760018114611ef557611f23565b60ff1985168884015283151560051b880183019550611f23565b866000528260002060005b85811015611f1b5781548a8201860152908301908401611f00565b890184019650505b505050505092915050565b60006101208a8352896020840152806040840152611f4e8184018a611eb1565b90508281036060840152611f628189611eb1565b90508660808401528560a084015282810360c0840152611f8281866119e6565b83810360e0850152601a81527f43616e63656c6c65642062792075736572206d616e75616c6c790000000000006020820152610100909301939093525060400198975050505050505050565b60006101208a8352896020840152806040840152611fee8184018a611eb1565b90508281036060840152611e1e8189611eb1565b601f82111561204c57600081815260208120601f850160051c810160208610156120295750805b601f850160051c820191505b8181101561204857828155600101612035565b5050505b505050565b815167ffffffffffffffff81111561206b5761206b611b7e565b61207f816120798454611cf0565b84612002565b602080601f8311600181146120b4576000841561209c5750858301515b600019600386901b1c1916600185901b178555612048565b600085815260208120601f198616915b828110156120e3578886015182559484019460019091019084016120c4565b50858210156121015787850151600019600388901b60f8161c191681555b5050505050600190811b01905550565b60006101208b83528a60208401528060408401526121318184018b611eb1565b90508281036060840152612145818a611eb1565b90508760808401528660a084015282810360c084015261216581876119e6565b905082810360e084015261217981866119e6565b915050826101008301529a9950505050505050505050565b60006101208a83528960208401528060408401526121b18184018a611eb1565b905082810360608401526121c58189611eb1565b90508660808401528560a084015282810360c08401526121e581866119e6565b83810360e0850152601a81527f52656a65637465642062792061646d696e206d616e75616c6c790000000000006020820152610100909301939093525060400198975050505050505050565b60006101208a83528960208401528060408401526122518184018a6119e6565b9050828103606084015261226581896119e6565b90508660808401528560a084015282810360c084015261228581866119e6565b83810360e085015260008152610100909301939093525060200198975050505050505050565b60006101208a83528960208401528060408401526122cb8184018a6119e6565b905082810360608401526122df81896119e6565b90508660808401528560a084015282810360c08401526122ff81866119e6565b83810360e08501526011815270417070726f766564202873797374656d2960781b6020820152610100909301939093525060400198975050505050505050565b60006101208a835289602084015280604084015261235f8184018a6119e6565b9050828103606084015261237381896119e6565b90508660808401528560a084015282810360c084015261239381866119e6565b83810360e0850152602181527f52656a65637465642064756520746f20636f6e666c696374202873797374656d6020820152602960f81b6040820152610100909301939093525060600198975050505050505050565b600082516123fb8184602087016119c2565b9190910192915050565b600080835461241381611cf0565b6001828116801561242b57600181146124405761246f565b60ff198416875282151583028701945061246f565b8760005260208060002060005b858110156124665781548a82015290840190820161244d565b50505082870194505b5092969550505050505056feec4c4e08cd6f7bdf950c2e24c35f3bd83f121e67c8c09ff33e9b072e25ffe8d0a2646970667358221220ae0b1d0c793ca1518301937e267bdcefbb33e433ed8a5f0e128af689fa3733d364736f6c63430008130033";

    private static String librariesLinkedBinary;

    public static final String FUNC_APPENDBOOKINGNOTE = "appendBookingNote";

    public static final String FUNC_BOOKINGS = "bookings";

    public static final String FUNC_CANCELBOOKING = "cancelBooking";

    public static final String FUNC_CREATEBOOKING = "createBooking";

    public static final String FUNC_DELETEBOOKING = "deleteBooking";

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

    public RemoteFunctionCall<TransactionReceipt> deleteBooking(byte[] ipfsHash,
            BigInteger bookingId) {
        final Function function = new Function(
                FUNC_DELETEBOOKING, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(ipfsHash), 
                new org.web3j.abi.datatypes.generated.Uint256(bookingId)), 
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
