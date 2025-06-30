package com.marketplace.product.DTO;

import com.marketplace.productservice.contracts.OrderStatusTracker;

import java.math.BigInteger;

public class BlockchainOrderStateDTO {

    private String blockchainOrderId;
    private String buyerAddress;
    private String sellerAddress;
    private String orderStatus;
    private String trackingNumber;

    private BlockchainOrderStateDTO() {}

    public static BlockchainOrderStateDTO fromContractOrder(OrderStatusTracker.Order contractOrder) {
        BlockchainOrderStateDTO dto = new BlockchainOrderStateDTO();
        dto.blockchainOrderId = contractOrder.id.toString();
        dto.buyerAddress = contractOrder.buyer;
        dto.sellerAddress = contractOrder.seller;
        dto.trackingNumber = contractOrder.trackingNumber;
        dto.orderStatus = mapStateToString(contractOrder.state);
        return dto;
    }

    private static String mapStateToString(BigInteger state) {
        int stateValue = state.intValue();
        switch (stateValue) {
            case 0: return "Created";
            case 1: return "Paid";
            case 2: return "Shipped";
            case 3: return "Delivered";
            case 4: return "Disputed";
            case 5: return "Completed";
            case 6: return "Refunded";
            default: return "Unknown";
        }
    }

    // Getters
    public String getBlockchainOrderId() { return blockchainOrderId; }
    public String getBuyerAddress() { return buyerAddress; }
    public String getSellerAddress() { return sellerAddress; }
    public String getOrderStatus() { return orderStatus; }
    public String getTrackingNumber() { return trackingNumber; }
}
