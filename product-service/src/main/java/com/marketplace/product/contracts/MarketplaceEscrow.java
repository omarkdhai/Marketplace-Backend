package com.marketplace.product.contracts;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.8.7.
 */
@SuppressWarnings("rawtypes")
public class MarketplaceEscrow extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_CONFIRMITEMRECEIVED = "confirmItemReceived";

    public static final String FUNC_CREATETRANSACTION = "createTransaction";

    public static final String FUNC_DEPOSITFUNDS = "depositFunds";

    public static final String FUNC_GETCONTRACTBALANCE = "getContractBalance";

    public static final String FUNC_MARKITEMSENT = "markItemSent";

    public static final String FUNC_NEXTTRANSACTIONID = "nextTransactionId";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_PLATFORMFEEPERCENT = "platformFeePercent";

    public static final String FUNC_REQUESTREFUND = "requestRefund";

    public static final String FUNC_RESOLVEDISPUTEANDPAYSELLER = "resolveDisputeAndPaySeller";

    public static final String FUNC_RESOLVEDISPUTEANDREFUND = "resolveDisputeAndRefund";

    public static final String FUNC_TRANSACTIONS = "transactions";

    public static final Event DISPUTEOPENED_EVENT = new Event("DisputeOpened", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event DISPUTERESOLVED_EVENT = new Event("DisputeResolved", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event FUNDSDEPOSITED_EVENT = new Event("FundsDeposited", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event FUNDSREFUNDEDTOBUYER_EVENT = new Event("FundsRefundedToBuyer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event FUNDSRELEASEDTOSELLER_EVENT = new Event("FundsReleasedToSeller", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event ITEMCONFIRMEDBYBUYER_EVENT = new Event("ItemConfirmedByBuyer", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}));
    ;

    public static final Event ITEMSENT_EVENT = new Event("ItemSent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event PLATFORMFEECOLLECTED_EVENT = new Event("PlatformFeeCollected", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event TRANSACTIONCREATED_EVENT = new Event("TransactionCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
    ;

    @Deprecated
    protected MarketplaceEscrow(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected MarketplaceEscrow(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected MarketplaceEscrow(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected MarketplaceEscrow(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<DisputeOpenedEventResponse> getDisputeOpenedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DISPUTEOPENED_EVENT, transactionReceipt);
        ArrayList<DisputeOpenedEventResponse> responses = new ArrayList<DisputeOpenedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DisputeOpenedEventResponse typedResponse = new DisputeOpenedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.requester = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<DisputeOpenedEventResponse> disputeOpenedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, DisputeOpenedEventResponse>() {
            @Override
            public DisputeOpenedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DISPUTEOPENED_EVENT, log);
                DisputeOpenedEventResponse typedResponse = new DisputeOpenedEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.requester = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DisputeOpenedEventResponse> disputeOpenedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DISPUTEOPENED_EVENT));
        return disputeOpenedEventFlowable(filter);
    }

    public List<DisputeResolvedEventResponse> getDisputeResolvedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DISPUTERESOLVED_EVENT, transactionReceipt);
        ArrayList<DisputeResolvedEventResponse> responses = new ArrayList<DisputeResolvedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DisputeResolvedEventResponse typedResponse = new DisputeResolvedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.refundedToBuyer = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amountToSeller = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.amountToBuyer = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<DisputeResolvedEventResponse> disputeResolvedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, DisputeResolvedEventResponse>() {
            @Override
            public DisputeResolvedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DISPUTERESOLVED_EVENT, log);
                DisputeResolvedEventResponse typedResponse = new DisputeResolvedEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.refundedToBuyer = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.amountToSeller = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.amountToBuyer = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DisputeResolvedEventResponse> disputeResolvedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DISPUTERESOLVED_EVENT));
        return disputeResolvedEventFlowable(filter);
    }

    public List<FundsDepositedEventResponse> getFundsDepositedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(FUNDSDEPOSITED_EVENT, transactionReceipt);
        ArrayList<FundsDepositedEventResponse> responses = new ArrayList<FundsDepositedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FundsDepositedEventResponse typedResponse = new FundsDepositedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<FundsDepositedEventResponse> fundsDepositedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, FundsDepositedEventResponse>() {
            @Override
            public FundsDepositedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(FUNDSDEPOSITED_EVENT, log);
                FundsDepositedEventResponse typedResponse = new FundsDepositedEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<FundsDepositedEventResponse> fundsDepositedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(FUNDSDEPOSITED_EVENT));
        return fundsDepositedEventFlowable(filter);
    }

    public List<FundsRefundedToBuyerEventResponse> getFundsRefundedToBuyerEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(FUNDSREFUNDEDTOBUYER_EVENT, transactionReceipt);
        ArrayList<FundsRefundedToBuyerEventResponse> responses = new ArrayList<FundsRefundedToBuyerEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FundsRefundedToBuyerEventResponse typedResponse = new FundsRefundedToBuyerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<FundsRefundedToBuyerEventResponse> fundsRefundedToBuyerEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, FundsRefundedToBuyerEventResponse>() {
            @Override
            public FundsRefundedToBuyerEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(FUNDSREFUNDEDTOBUYER_EVENT, log);
                FundsRefundedToBuyerEventResponse typedResponse = new FundsRefundedToBuyerEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.buyer = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<FundsRefundedToBuyerEventResponse> fundsRefundedToBuyerEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(FUNDSREFUNDEDTOBUYER_EVENT));
        return fundsRefundedToBuyerEventFlowable(filter);
    }

    public List<FundsReleasedToSellerEventResponse> getFundsReleasedToSellerEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(FUNDSRELEASEDTOSELLER_EVENT, transactionReceipt);
        ArrayList<FundsReleasedToSellerEventResponse> responses = new ArrayList<FundsReleasedToSellerEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FundsReleasedToSellerEventResponse typedResponse = new FundsReleasedToSellerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.seller = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<FundsReleasedToSellerEventResponse> fundsReleasedToSellerEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, FundsReleasedToSellerEventResponse>() {
            @Override
            public FundsReleasedToSellerEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(FUNDSRELEASEDTOSELLER_EVENT, log);
                FundsReleasedToSellerEventResponse typedResponse = new FundsReleasedToSellerEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.seller = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<FundsReleasedToSellerEventResponse> fundsReleasedToSellerEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(FUNDSRELEASEDTOSELLER_EVENT));
        return fundsReleasedToSellerEventFlowable(filter);
    }

    public List<ItemConfirmedByBuyerEventResponse> getItemConfirmedByBuyerEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(ITEMCONFIRMEDBYBUYER_EVENT, transactionReceipt);
        ArrayList<ItemConfirmedByBuyerEventResponse> responses = new ArrayList<ItemConfirmedByBuyerEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ItemConfirmedByBuyerEventResponse typedResponse = new ItemConfirmedByBuyerEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ItemConfirmedByBuyerEventResponse> itemConfirmedByBuyerEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ItemConfirmedByBuyerEventResponse>() {
            @Override
            public ItemConfirmedByBuyerEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ITEMCONFIRMEDBYBUYER_EVENT, log);
                ItemConfirmedByBuyerEventResponse typedResponse = new ItemConfirmedByBuyerEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ItemConfirmedByBuyerEventResponse> itemConfirmedByBuyerEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ITEMCONFIRMEDBYBUYER_EVENT));
        return itemConfirmedByBuyerEventFlowable(filter);
    }

    public List<ItemSentEventResponse> getItemSentEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(ITEMSENT_EVENT, transactionReceipt);
        ArrayList<ItemSentEventResponse> responses = new ArrayList<ItemSentEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            ItemSentEventResponse typedResponse = new ItemSentEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<ItemSentEventResponse> itemSentEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, ItemSentEventResponse>() {
            @Override
            public ItemSentEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ITEMSENT_EVENT, log);
                ItemSentEventResponse typedResponse = new ItemSentEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.seller = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<ItemSentEventResponse> itemSentEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ITEMSENT_EVENT));
        return itemSentEventFlowable(filter);
    }

    public List<PlatformFeeCollectedEventResponse> getPlatformFeeCollectedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(PLATFORMFEECOLLECTED_EVENT, transactionReceipt);
        ArrayList<PlatformFeeCollectedEventResponse> responses = new ArrayList<PlatformFeeCollectedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PlatformFeeCollectedEventResponse typedResponse = new PlatformFeeCollectedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.platform = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<PlatformFeeCollectedEventResponse> platformFeeCollectedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, PlatformFeeCollectedEventResponse>() {
            @Override
            public PlatformFeeCollectedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(PLATFORMFEECOLLECTED_EVENT, log);
                PlatformFeeCollectedEventResponse typedResponse = new PlatformFeeCollectedEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.platform = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<PlatformFeeCollectedEventResponse> platformFeeCollectedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PLATFORMFEECOLLECTED_EVENT));
        return platformFeeCollectedEventFlowable(filter);
    }

    public List<TransactionCreatedEventResponse> getTransactionCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(TRANSACTIONCREATED_EVENT, transactionReceipt);
        ArrayList<TransactionCreatedEventResponse> responses = new ArrayList<TransactionCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            TransactionCreatedEventResponse typedResponse = new TransactionCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.itemId = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<TransactionCreatedEventResponse> transactionCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, TransactionCreatedEventResponse>() {
            @Override
            public TransactionCreatedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(TRANSACTIONCREATED_EVENT, log);
                TransactionCreatedEventResponse typedResponse = new TransactionCreatedEventResponse();
                typedResponse.log = log;
                typedResponse.transactionId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
                typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.itemId = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<TransactionCreatedEventResponse> transactionCreatedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(TRANSACTIONCREATED_EVENT));
        return transactionCreatedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> confirmItemReceived(BigInteger _transactionId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CONFIRMITEMRECEIVED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createTransaction(String _seller, BigInteger _amount, String _itemId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_CREATETRANSACTION, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _seller), 
                new org.web3j.abi.datatypes.generated.Uint256(_amount), 
                new org.web3j.abi.datatypes.Utf8String(_itemId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> depositFunds(BigInteger _transactionId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_DEPOSITFUNDS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> getContractBalance() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_GETCONTRACTBALANCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> markItemSent(BigInteger _transactionId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_MARKITEMSENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> nextTransactionId() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_NEXTTRANSACTIONID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<String> owner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<BigInteger> platformFeePercent() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_PLATFORMFEEPERCENT, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> requestRefund(BigInteger _transactionId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_REQUESTREFUND, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> resolveDisputeAndPaySeller(BigInteger _transactionId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RESOLVEDISPUTEANDPAYSELLER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> resolveDisputeAndRefund(BigInteger _transactionId) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RESOLVEDISPUTEANDREFUND, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_transactionId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String>> transactions(BigInteger param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_TRANSACTIONS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteFunctionCall<Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String>>(function,
                new Callable<Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String>>() {
                    @Override
                    public Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (BigInteger) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (BigInteger) results.get(5).getValue(), 
                                (String) results.get(6).getValue());
                    }
                });
    }

    @Deprecated
    public static MarketplaceEscrow load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new MarketplaceEscrow(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static MarketplaceEscrow load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new MarketplaceEscrow(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static MarketplaceEscrow load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new MarketplaceEscrow(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static MarketplaceEscrow load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new MarketplaceEscrow(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class DisputeOpenedEventResponse extends BaseEventResponse {
        public BigInteger transactionId;

        public String requester;
    }

    public static class DisputeResolvedEventResponse extends BaseEventResponse {
        public BigInteger transactionId;

        public Boolean refundedToBuyer;

        public BigInteger amountToSeller;

        public BigInteger amountToBuyer;
    }

    public static class FundsDepositedEventResponse extends BaseEventResponse {
        public BigInteger transactionId;

        public BigInteger amount;
    }

    public static class FundsRefundedToBuyerEventResponse extends BaseEventResponse {
        public BigInteger transactionId;

        public String buyer;

        public BigInteger amount;
    }

    public static class FundsReleasedToSellerEventResponse extends BaseEventResponse {
        public BigInteger transactionId;

        public String seller;

        public BigInteger amount;
    }

    public static class ItemConfirmedByBuyerEventResponse extends BaseEventResponse {
        public BigInteger transactionId;
    }

    public static class ItemSentEventResponse extends BaseEventResponse {
        public BigInteger transactionId;

        public String seller;
    }

    public static class PlatformFeeCollectedEventResponse extends BaseEventResponse {
        public BigInteger transactionId;

        public String platform;

        public BigInteger amount;
    }

    public static class TransactionCreatedEventResponse extends BaseEventResponse {
        public BigInteger transactionId;

        public String buyer;

        public String seller;

        public BigInteger amount;

        public String itemId;
    }
}
