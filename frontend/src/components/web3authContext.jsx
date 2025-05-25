import { WEB3AUTH_NETWORK, WALLET_CONNECTORS } from "@web3auth/modal";
import { CHAIN_NAMESPACES }  from "@web3auth/base";

const CLIENT_ID = import.meta.env.VITE_WEB3AUTH_CLIENT_ID;
const RPC_URL   = import.meta.env.VITE_QUORUM_RPC_URL;
const CHAIN_ID  = import.meta.env.VITE_QUORUM_CHAIN_ID;

const web3AuthContextConfig = {
  web3AuthOptions: {
    clientId: CLIENT_ID,
    web3AuthNetwork: WEB3AUTH_NETWORK.TESTNET,  
    modalConfig: {
      connectors: {
        [WALLET_CONNECTORS.METAMASK] : { showOnModal: false },
        [WALLET_CONNECTORS.AUTH] : { showOnModal: true }
      },
    },
  },
  chainConfig: {                               
    chainNamespace: CHAIN_NAMESPACES.EIP155,
    chainId:        CHAIN_ID,
    rpcTarget:      RPC_URL,
  },
};

export default web3AuthContextConfig;