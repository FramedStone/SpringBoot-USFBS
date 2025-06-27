import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";

import App from "./App.jsx";

import { Web3AuthProvider } from "@web3auth/modal/react";
import web3AuthContextConfig from "./components/web3authContext";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

import { Buffer } from "buffer";
window.Buffer = window.Buffer || Buffer;

const queryClient = new QueryClient();

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <Web3AuthProvider config={web3AuthContextConfig}>
      <QueryClientProvider client={queryClient}>
        <App />
      </QueryClientProvider>
    </Web3AuthProvider>
  </StrictMode>
);
