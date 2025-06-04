package com.marketplace.product.DTO;


public class UpdateBlockchainInfoDto {
    public String blockchainTransactionId;
    public String buyerEthAddress;
    public String sellerEthAddress;
    public String blockchainRegisteredAmount;
    public String blockchainState;

    public UpdateBlockchainInfoDto() {
    }

    public UpdateBlockchainInfoDto(String blockchainTransactionId, String buyerEthAddress, String sellerEthAddress,
                                  String blockchainRegisteredAmount, String blockchainState) {
        this.blockchainTransactionId = blockchainTransactionId;
        this.buyerEthAddress = buyerEthAddress;
        this.sellerEthAddress = sellerEthAddress;
        this.blockchainRegisteredAmount = blockchainRegisteredAmount;
        this.blockchainState = blockchainState;
    }
}
