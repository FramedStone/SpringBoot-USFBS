import React, { useState } from 'react';
import Navbar from "@components/Navbar";
import Toast from "@components/Toast";
import '@styles/Cart.css';

const Cart = () => {
  const [activeTab, setActiveTab] = useState("cart");
  const [toast, setToast] = useState({ msg: "", type: "success" });

  return (
    <div className="cart-container">
      <Navbar activeTab={activeTab} setActiveTab={setActiveTab} navType="home" />
      <div className="cart-content">
        <div className="page-header">
          <h1>Shopping Cart</h1>
          <p>Review your selected bookings before checkout</p>
        </div>
        
        <div className="cart-placeholder">
          <h3>Cart Page</h3>
          <p>TODO: Implement shopping cart functionality</p>
        </div>
      </div>
      
      <Toast
        message={toast.msg}
        type={toast.type}
        onClose={() => setToast({ msg: "", type: "success" })}
      />
    </div>
  );
};

export default Cart;