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
    public static final String BINARY = "6080604052348015600e575f5ffd5b5060405161244f38038061244f833981016040819052602b91604e565b5f80546001600160a01b0319166001600160a01b03929092169190911790556079565b5f60208284031215605d575f5ffd5b81516001600160a01b03811681146072575f5ffd5b9392505050565b6123c9806100865f395ff3fe608060405234801561000f575f5ffd5b506004361061009b575f3560e01c8063939793f311610063578063939793f31461012d578063bf7ddd5b14610140578063f245ab9b14610153578063f663e3ac14610166578063f67e44441461016e575f5ffd5b80631dab301e1461009f5780633c15b31c146100cf5780635d874a91146100ef5780637c8d6d9e1461010457806392153eff1461011a575b5f5ffd5b6100b26100ad3660046118f8565b61018f565b6040516100c6989796959493929190611951565b60405180910390f35b6100e26100dd3660046118f8565b610300565b6040516100c691906119cf565b6101026100fd3660046119e8565b610406565b005b61010c6107fc565b6040516100c6929190611a08565b6101026101283660046119e8565b6109a1565b61010261013b366004611b47565b610c8f565b61010261014e3660046119e8565b610e62565b6100e26101613660046118f8565b61100c565b61010c611079565b61018161017c366004611b93565b61129a565b6040519081526020016100c6565b6001818154811061019e575f80fd5b5f91825260209091206009909102018054600182015460028301546003840180546001600160a01b039094169550919390929091906101dc90611c11565b80601f016020809104026020016040519081016040528092919081815260200182805461020890611c11565b80156102535780601f1061022a57610100808354040283529160200191610253565b820191905f5260205f20905b81548152906001019060200180831161023657829003601f168201915b50505050509080600401805461026890611c11565b80601f016020809104026020016040519081016040528092919081815260200182805461029490611c11565b80156102df5780601f106102b6576101008083540402835291602001916102df565b820191905f5260205f20905b8154815290600101906020018083116102c257829003601f168201915b50505050600583015460068401546007909401549293909290915060ff1688565b6060816001838154811061031657610316611c49565b905f5260205f209060090201600101541461034c5760405162461bcd60e51b815260040161034390611c5d565b60405180910390fd5b336001600160a01b03166001838154811061036957610369611c49565b5f9182526020909120600990910201546001600160a01b0316146103cf5760405162461bcd60e51b815260206004820152601960248201527f496e76616c6964206163636573732028626f6f6b696e677329000000000000006044820152606401610343565b610400600183815481106103e5576103e5611c49565b5f91825260209091206007600990920201015460ff16611559565b92915050565b5f546001600160a01b0316331461042f5760405162461bcd60e51b815260040161034390611c9f565b60015481106104805760405162461bcd60e51b815260206004820152601d60248201527f496e646578206f7574206f6620626f756e642028626f6f6b696e6773290000006044820152606401610343565b816001828154811061049457610494611c49565b905f5260205f20906009020160020154146104c15760405162461bcd60e51b815260040161034390611cc6565b80600182815481106104d5576104d5611c49565b905f5260205f20906009020160010154146105025760405162461bcd60e51b815260040161034390611c5d565b6001818154811061051557610515611c49565b905f5260205f2090600902016006015442101561057f5760405162461bcd60e51b815260206004820152602260248201527f626f6f6b696e67206e6f742079657420657870697265642028626f6f6b696e67604482015261732960f01b6064820152608401610343565b5f6001828154811061059357610593611c49565b905f5260205f20906009020160030180546105ad90611c11565b80601f01602080910402602001604051908101604052809291908181526020018280546105d990611c11565b80156106245780601f106105fb57610100808354040283529160200191610624565b820191905f5260205f20905b81548152906001019060200180831161060757829003601f168201915b505050505090505f6001838154811061063f5761063f611c49565b905f5260205f209060090201600401805461065990611c11565b80601f016020809104026020016040519081016040528092919081815260200182805461068590611c11565b80156106d05780601f106106a7576101008083540402835291602001916106d0565b820191905f5260205f20905b8154815290600101906020018083116106b357829003601f168201915b505050505090505f600184815481106106eb576106eb611c49565b905f5260205f2090600902016005015490505f6001858154811061071157610711611c49565b905f5260205f2090600902016006015490506001858154811061073657610736611c49565b5f9182526020822060099091020180546001600160a01b031916815560018101829055600281018290559061076e600383018261181f565b61077b600483015f61181f565b5f600583018190556006830181905560078301805460ff191690556107a4906008840190611859565b50507f95ba5894d801086326249705bc97694be8d367c97e40c84be6382252f92778358587868686866107d76003611559565b426040516107ec989796959493929190611d07565b60405180910390a1505050505050565b5f5460609081906001600160a01b0316331461082a5760405162461bcd60e51b815260040161034390611c9f565b6001546108725760405162461bcd60e51b8152602060048201526016602482015275656d7074792061727261792028626f6f6b696e67732960501b6044820152606401610343565b6001545f9067ffffffffffffffff81111561088f5761088f611aa8565b6040519080825280602002602001820160405280156108b8578160200160208202803683370190505b506001549091505f9067ffffffffffffffff8111156108d9576108d9611aa8565b60405190808252806020026020018201604052801561090c57816020015b60608152602001906001900390816108f75790505b5090505f5b600154811015610997576001818154811061092e5761092e611c49565b905f5260205f2090600902016001015483828151811061095057610950611c49565b602002602001018181525050610972600182815481106103e5576103e5611c49565b82828151811061098457610984611c49565b6020908102919091010152600101610911565b5090925090509091565b81600182815481106109b5576109b5611c49565b905f5260205f20906009020160020154146109e25760405162461bcd60e51b815260040161034390611cc6565b80600182815481106109f6576109f6611c49565b905f5260205f2090600902016001015414610a235760405162461bcd60e51b815260040161034390611c5d565b336001600160a01b031660018281548110610a4057610a40611c49565b5f9182526020909120600990910201546001600160a01b031614610aa65760405162461bcd60e51b815260206004820152601860248201527f4163636573732064656e6965642028626f6f6b696e67732900000000000000006044820152606401610343565b600460018281548110610abb57610abb611c49565b5f9182526020909120600760099092020101805460ff19166001836004811115610ae757610ae761193d565b02179055505f5160206123745f395f51905f52818360018481548110610b0f57610b0f611c49565b905f5260205f20906009020160030160018581548110610b3157610b31611c49565b905f5260205f20906009020160040160018681548110610b5357610b53611c49565b905f5260205f2090600902016005015460018781548110610b7657610b76611c49565b905f5260205f20906009020160060154610b906004611559565b42604051610ba5989796959493929190611e24565b60405180910390a1610bb78282610406565b7f95ba5894d801086326249705bc97694be8d367c97e40c84be6382252f9277835818360018481548110610bed57610bed611c49565b905f5260205f20906009020160030160018581548110610c0f57610c0f611c49565b905f5260205f20906009020160040160018681548110610c3157610c31611c49565b905f5260205f2090600902016005015460018781548110610c5457610c54611c49565b905f5260205f20906009020160060154610c6e6003611559565b42604051610c83989796959493929190611ec2565b60405180910390a15050565b5f546001600160a01b03163314610cb85760405162461bcd60e51b815260040161034390611c9f565b8260018381548110610ccc57610ccc611c49565b905f5260205f2090600902016002015414610cf95760405162461bcd60e51b815260040161034390611cc6565b8160018381548110610d0d57610d0d611c49565b905f5260205f2090600902016001015414610d3a5760405162461bcd60e51b815260040161034390611c5d565b60018281548110610d4d57610d4d611c49565b5f91825260208083206008600990930201919091018054600181018255908352912001610d7a8282611f40565b507fc21b8b77b0a6a011e5be1188bf0a7b9d5169b26df31960bc05d2ddd6f184b664828460018581548110610db157610db1611c49565b905f5260205f20906009020160030160018681548110610dd357610dd3611c49565b905f5260205f20906009020160040160018781548110610df557610df5611c49565b905f5260205f2090600902016005015460018881548110610e1857610e18611c49565b905f5260205f20906009020160060154610e3e60018a815481106103e5576103e5611c49565b8842604051610e5599989796959493929190611ffb565b60405180910390a1505050565b5f546001600160a01b03163314610e8b5760405162461bcd60e51b815260040161034390611c9f565b8160018281548110610e9f57610e9f611c49565b905f5260205f2090600902016002015414610ecc5760405162461bcd60e51b815260040161034390611cc6565b8060018281548110610ee057610ee0611c49565b905f5260205f2090600902016001015414610f0d5760405162461bcd60e51b815260040161034390611c5d565b600260018281548110610f2257610f22611c49565b5f9182526020909120600760099092020101805460ff19166001836004811115610f4e57610f4e61193d565b02179055505f5160206123745f395f51905f52818360018481548110610f7657610f76611c49565b905f5260205f20906009020160030160018581548110610f9857610f98611c49565b905f5260205f20906009020160040160018681548110610fba57610fba611c49565b905f5260205f2090600902016005015460018781548110610fdd57610fdd611c49565b905f5260205f20906009020160060154610ff76002611559565b42604051610c83989796959493929190612079565b5f546060906001600160a01b031633146110385760405162461bcd60e51b815260040161034390611c9f565b816001838154811061104c5761104c611c49565b905f5260205f20906009020160010154146103cf5760405162461bcd60e51b815260040161034390611c5d565b60015460609081906110c65760405162461bcd60e51b8152602060048201526016602482015275656d7074792061727261792028626f6f6b696e67732960501b6044820152606401610343565b5f805b60015481101561112457336001600160a01b0316600182815481106110f0576110f0611c49565b5f9182526020909120600990910201546001600160a01b03160361111c578161111881612117565b9250505b6001016110c9565b505f8167ffffffffffffffff81111561113f5761113f611aa8565b604051908082528060200260200182016040528015611168578160200160208202803683370190505b5090505f8267ffffffffffffffff81111561118557611185611aa8565b6040519080825280602002602001820160405280156111b857816020015b60608152602001906001900390816111a35790505b5090505f805b60015481101561128e57336001600160a01b0316600182815481106111e5576111e5611c49565b5f9182526020909120600990910201546001600160a01b031603611286576001818154811061121657611216611c49565b905f5260205f2090600902016001015484838151811061123857611238611c49565b60200260200101818152505061125a600182815481106103e5576103e5611c49565b83838151811061126c5761126c611c49565b6020026020010181905250818061128290612117565b9250505b6001016111be565b50919590945092505050565b60018054604080516101208101825233815260208082018490528183018a9052606082018990526080820188905260a0820187905260c0820186905260e082019490945281515f80825294810190925283916101008201908361130d565b60608152602001906001900390816112f85790505b5090529050337f0e1bb2f317d9542bcb573fee924d642b568af6dbedf21ad2c485e8a1214c7800838a8a8a8a8a6113446001611559565b4260405161135998979695949392919061213b565b60405180910390a261136d878787876116af565b156113ba575f60e08201819052505f5160206123745f395f51905f528289898989896113985f611559565b426040516113ad9897969594939291906121b2565b60405180910390a1611400565b600260e08201819052505f5160206123745f395f51905f528289898989896113e26002611559565b426040516113f7989796959493929190612244565b60405180910390a15b6001805480820182555f9190915281517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf6600990920291820180546001600160a01b0319166001600160a01b0390921691909117815560208301517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf783015560408301517fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf8830155606083015183927fb10e2d527612073b26eecdfd717e6a320cf44b4afac2b0732d9fcbe2b7fa0cf901906114dc9082611f40565b50608082015160048201906114f19082611f40565b5060a0820151600582015560c0820151600682015560e082015160078201805460ff191660018360048111156115295761152961193d565b0217905550610100820151805161154a916008840191602090910190611874565b50929998505050505050505050565b60605f82600481111561156e5761156e61193d565b03611597575050604080518082019091526008815267185c1c1c9bdd995960c21b602082015290565b60018260048111156115ab576115ab61193d565b036115d357505060408051808201909152600781526670656e64696e6760c81b602082015290565b60028260048111156115e7576115e761193d565b036116105750506040805180820190915260088152671c995a9958dd195960c21b602082015290565b60038260048111156116245761162461193d565b0361164e57505060408051808201909152600981526818dbdb5c1b195d195960ba1b602082015290565b60048260048111156116625761166261193d565b0361168c57505060408051808201909152600981526818d85b98d95b1b195960ba1b602082015290565b50506040805180820190915260078152663ab735b737bbb760c91b602082015290565b5f805b60015481101561181157856040516020016116cd91906122ec565b60405160208183030381529060405280519060200120600182815481106116f6576116f6611c49565b905f5260205f2090600902016003016040516020016117159190612302565b604051602081830303815290604052805190602001201480156117a457508460405160200161174491906122ec565b604051602081830303815290604052805190602001206001828154811061176d5761176d611c49565b905f5260205f20906009020160040160405160200161178c9190612302565b60405160208183030381529060405280519060200120145b156118095782600182815481106117bd576117bd611c49565b905f5260205f209060090201600501541080156117fb575083600182815481106117e9576117e9611c49565b905f5260205f20906009020160060154115b15611809575f915050611817565b6001016116b2565b50600190505b949350505050565b50805461182b90611c11565b5f825580601f1061183a575050565b601f0160209004905f5260205f209081019061185691906118c8565b50565b5080545f8255905f5260205f209081019061185691906118dc565b828054828255905f5260205f209081019282156118b8579160200282015b828111156118b857825182906118a89082611f40565b5091602001919060010190611892565b506118c49291506118dc565b5090565b5b808211156118c4575f81556001016118c9565b808211156118c4575f6118ef828261181f565b506001016118dc565b5f60208284031215611908575f5ffd5b5035919050565b5f81518084528060208401602086015e5f602082860101526020601f19601f83011685010191505092915050565b634e487b7160e01b5f52602160045260245ffd5b60018060a01b038916815287602082015286604082015261010060608201525f61197f61010083018861190f565b8281036080840152611991818861190f565b9150508460a08301528360c0830152600583106119bc57634e487b7160e01b5f52602160045260245ffd5b8260e08301529998505050505050505050565b602081525f6119e1602083018461190f565b9392505050565b5f5f604083850312156119f9575f5ffd5b50508035926020909101359150565b604080825283519082018190525f9060208501906060840190835b81811015611a41578351835260209384019390920191600101611a23565b50508381036020850152809150845180825260208201925060208160051b830101602087015f5b83811015611a9a57601f19858403018652611a8483835161190f565b6020968701969093509190910190600101611a68565b509098975050505050505050565b634e487b7160e01b5f52604160045260245ffd5b5f82601f830112611acb575f5ffd5b813567ffffffffffffffff811115611ae557611ae5611aa8565b604051601f8201601f19908116603f0116810167ffffffffffffffff81118282101715611b1457611b14611aa8565b604052818152838201602001851015611b2b575f5ffd5b816020850160208301375f918101602001919091529392505050565b5f5f5f60608486031215611b59575f5ffd5b8335925060208401359150604084013567ffffffffffffffff811115611b7d575f5ffd5b611b8986828701611abc565b9150509250925092565b5f5f5f5f5f60a08688031215611ba7575f5ffd5b85359450602086013567ffffffffffffffff811115611bc4575f5ffd5b611bd088828901611abc565b945050604086013567ffffffffffffffff811115611bec575f5ffd5b611bf888828901611abc565b9598949750949560608101359550608001359392505050565b600181811c90821680611c2557607f821691505b602082108103611c4357634e487b7160e01b5f52602260045260245ffd5b50919050565b634e487b7160e01b5f52603260045260245ffd5b60208082526022908201527f626f6f6b696e67496420646f65736e2774206d617463682028626f6f6b696e67604082015261732960f01b606082015260800190565b6020808252600d908201526c1058d8d95cdcc819195b9a5959609a1b604082015260600190565b60208082526021908201527f697066734861736820646f65736e2774206d617463682028626f6f6b696e67736040820152602960f81b606082015260800190565b88815287602082015261012060408201525f611d2761012083018961190f565b8281036060840152611d39818961190f565b90508660808401528560a084015282810360c0840152611d59818661190f565b83810360e0850152601981527f44656c65746564206f6e2d636861696e202873797374656d29000000000000006020820152610100909301939093525060400198975050505050505050565b5f8154611db181611c11565b808552600182168015611dcb5760018114611de757611e1b565b60ff1983166020870152602082151560051b8701019350611e1b565b845f5260205f205f5b83811015611e125781546020828a010152600182019150602081019050611df0565b87016020019450505b50505092915050565b88815287602082015261012060408201525f611e44610120830189611da5565b8281036060840152611e568189611da5565b90508660808401528560a084015282810360c0840152611e76818661190f565b83810360e0850152601a81527f43616e63656c6c65642062792075736572206d616e75616c6c790000000000006020820152610100909301939093525060400198975050505050505050565b88815287602082015261012060408201525f611ee2610120830189611da5565b8281036060840152611d398189611da5565b601f821115611f3b57805f5260205f20601f840160051c81016020851015611f195750805b601f840160051c820191505b81811015611f38575f8155600101611f25565b50505b505050565b815167ffffffffffffffff811115611f5a57611f5a611aa8565b611f6e81611f688454611c11565b84611ef4565b6020601f821160018114611fa0575f8315611f895750848201515b5f19600385901b1c1916600184901b178455611f38565b5f84815260208120601f198516915b82811015611fcf5787850151825560209485019460019092019101611faf565b5084821015611fec57868401515f19600387901b60f8161c191681555b50505050600190811b01905550565b89815288602082015261012060408201525f61201b61012083018a611da5565b828103606084015261202d818a611da5565b90508760808401528660a084015282810360c084015261204d818761190f565b905082810360e0840152612061818661190f565b915050826101008301529a9950505050505050505050565b88815287602082015261012060408201525f612099610120830189611da5565b82810360608401526120ab8189611da5565b90508660808401528560a084015282810360c08401526120cb818661190f565b83810360e0850152601a81527f52656a65637465642062792061646d696e206d616e75616c6c790000000000006020820152610100909301939093525060400198975050505050505050565b5f6001820161213457634e487b7160e01b5f52601160045260245ffd5b5060010190565b88815287602082015261012060408201525f61215b61012083018961190f565b828103606084015261216d818961190f565b90508660808401528560a084015282810360c084015261218d818661190f565b83810360e08501525f8152610100909301939093525060200198975050505050505050565b88815287602082015261012060408201525f6121d261012083018961190f565b82810360608401526121e4818961190f565b90508660808401528560a084015282810360c0840152612204818661190f565b83810360e08501526011815270417070726f766564202873797374656d2960781b6020820152610100909301939093525060400198975050505050505050565b88815287602082015261012060408201525f61226461012083018961190f565b8281036060840152612276818961190f565b90508660808401528560a084015282810360c0840152612296818661190f565b83810360e0850152602181527f52656a65637465642064756520746f20636f6e666c696374202873797374656d6020820152602960f81b6040820152610100909301939093525060600198975050505050505050565b5f82518060208501845e5f920191825250919050565b5f5f835461230f81611c11565b600182168015612326576001811461233b57612368565b60ff1983168652811515820286019350612368565b865f5260205f205f5b8381101561236057815488820152600190910190602001612344565b505081860193505b50919594505050505056feec4c4e08cd6f7bdf950c2e24c35f3bd83f121e67c8c09ff33e9b072e25ffe8d0a264697066735822122048ba470c732c68c689cee71d97a9fab0ca1463c41f559d12bd68322e32851ab564736f6c634300081d0033";

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
