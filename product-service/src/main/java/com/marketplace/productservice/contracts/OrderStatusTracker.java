package com.marketplace.productservice.contracts;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
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
 * <p>Generated with web3j version 4.10.0.
 */
@SuppressWarnings("rawtypes")
@RegisterForReflection(targets = {
        OrderStatusTracker.Order.class,
        OrderStatusTracker.OrderCreatedEventResponse.class,
        OrderStatusTracker.OrderStatusChangedEventResponse.class
})
public class OrderStatusTracker extends Contract {
    public static final String BINARY = "Bin file was not provided";

    public static final String FUNC_CHANGEORACLEADDRESS = "changeOracleAddress";

    public static final String FUNC_CONFIRMDELIVERED = "confirmDelivered";

    public static final String FUNC_CREATEANDPAYORDER = "createAndPayOrder";

    public static final String FUNC_GETORDER = "getOrder";

    public static final String FUNC_MARKASPAID = "markAsPaid";

    public static final String FUNC_MARKASSHIPPED = "markAsShipped";

    public static final String FUNC_NEXTORDERID = "nextOrderId";

    public static final String FUNC_OPENDISPUTE = "openDispute";

    public static final String FUNC_ORACLE = "oracle";

    public static final String FUNC_ORDERS = "orders";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RESOLVEDISPUTE = "resolveDispute";

    public static final Event ORDERCREATED_EVENT = new Event("OrderCreated", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event ORDERSTATUSCHANGED_EVENT = new Event("OrderStatusChanged", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>(true) {}, new TypeReference<Uint8>() {}));
    ;

    @Deprecated
    protected OrderStatusTracker(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected OrderStatusTracker(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected OrderStatusTracker(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected OrderStatusTracker(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<OrderCreatedEventResponse> getOrderCreatedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ORDERCREATED_EVENT, transactionReceipt);
        ArrayList<OrderCreatedEventResponse> responses = new ArrayList<OrderCreatedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OrderCreatedEventResponse typedResponse = new OrderCreatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.orderId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
            typedResponse.itemId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.stripePaymentIntentId = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OrderCreatedEventResponse getOrderCreatedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ORDERCREATED_EVENT, log);
        OrderCreatedEventResponse typedResponse = new OrderCreatedEventResponse();
        typedResponse.log = log;
        typedResponse.orderId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.buyer = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.seller = (String) eventValues.getIndexedValues().get(2).getValue();
        typedResponse.itemId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.stripePaymentIntentId = (String) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<OrderCreatedEventResponse> orderCreatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOrderCreatedEventFromLog(log));
    }

    public Flowable<OrderCreatedEventResponse> orderCreatedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ORDERCREATED_EVENT));
        return orderCreatedEventFlowable(filter);
    }

    public static List<OrderStatusChangedEventResponse> getOrderStatusChangedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ORDERSTATUSCHANGED_EVENT, transactionReceipt);
        ArrayList<OrderStatusChangedEventResponse> responses = new ArrayList<OrderStatusChangedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OrderStatusChangedEventResponse typedResponse = new OrderStatusChangedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.orderId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newState = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OrderStatusChangedEventResponse getOrderStatusChangedEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ORDERSTATUSCHANGED_EVENT, log);
        OrderStatusChangedEventResponse typedResponse = new OrderStatusChangedEventResponse();
        typedResponse.log = log;
        typedResponse.orderId = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.newState = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        return typedResponse;
    }

    public Flowable<OrderStatusChangedEventResponse> orderStatusChangedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOrderStatusChangedEventFromLog(log));
    }

    public Flowable<OrderStatusChangedEventResponse> orderStatusChangedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ORDERSTATUSCHANGED_EVENT));
        return orderStatusChangedEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> changeOracleAddress(String _newOracle) {
        final Function function = new Function(
                FUNC_CHANGEORACLEADDRESS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _newOracle)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> confirmDelivered(BigInteger _orderId) {
        final Function function = new Function(
                FUNC_CONFIRMDELIVERED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_orderId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> createAndPayOrder(BigInteger _orderId, String _buyer, String _seller, String _itemId, String _stripePaymentIntentId) {
        final Function function = new Function(
                FUNC_CREATEANDPAYORDER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_orderId), 
                new org.web3j.abi.datatypes.Address(160, _buyer), 
                new org.web3j.abi.datatypes.Address(160, _seller), 
                new org.web3j.abi.datatypes.Utf8String(_itemId), 
                new org.web3j.abi.datatypes.Utf8String(_stripePaymentIntentId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Order> getOrder(BigInteger _orderId) {
        final Function function = new Function(FUNC_GETORDER, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_orderId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Order>() {}));
        return executeRemoteCallSingleValueReturn(function, Order.class);
    }

    public RemoteFunctionCall<TransactionReceipt> markAsPaid(BigInteger _orderId) {
        final Function function = new Function(
                FUNC_MARKASPAID, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_orderId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> markAsShipped(BigInteger _orderId, String _trackingNumber) {
        final Function function = new Function(
                FUNC_MARKASSHIPPED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_orderId), 
                new org.web3j.abi.datatypes.Utf8String(_trackingNumber)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<BigInteger> nextOrderId() {
        final Function function = new Function(FUNC_NEXTORDERID, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> openDispute(BigInteger _orderId) {
        final Function function = new Function(
                FUNC_OPENDISPUTE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_orderId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> oracle() {
        final Function function = new Function(FUNC_ORACLE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<Tuple7<BigInteger, String, String, String, BigInteger, String, String>> orders(BigInteger param0) {
        final Function function = new Function(FUNC_ORDERS, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint8>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
        return new RemoteFunctionCall<Tuple7<BigInteger, String, String, String, BigInteger, String, String>>(function,
                new Callable<Tuple7<BigInteger, String, String, String, BigInteger, String, String>>() {
                    @Override
                    public Tuple7<BigInteger, String, String, String, BigInteger, String, String> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple7<BigInteger, String, String, String, BigInteger, String, String>(
                                (BigInteger) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (String) results.get(2).getValue(), 
                                (String) results.get(3).getValue(), 
                                (BigInteger) results.get(4).getValue(), 
                                (String) results.get(5).getValue(), 
                                (String) results.get(6).getValue());
                    }
                });
    }

    public RemoteFunctionCall<String> owner() {
        final Function function = new Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> resolveDispute(BigInteger _orderId, Boolean wasRefunded) {
        final Function function = new Function(
                FUNC_RESOLVEDISPUTE, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_orderId), 
                new org.web3j.abi.datatypes.Bool(wasRefunded)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static OrderStatusTracker load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new OrderStatusTracker(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static OrderStatusTracker load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new OrderStatusTracker(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static OrderStatusTracker load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new OrderStatusTracker(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static OrderStatusTracker load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new OrderStatusTracker(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class Order extends DynamicStruct {
        public BigInteger id;

        public String buyer;

        public String seller;

        public String stripePaymentIntentId;

        public BigInteger state;

        public String itemId;

        public String trackingNumber;

        public Order(BigInteger id, String buyer, String seller, String stripePaymentIntentId, BigInteger state, String itemId, String trackingNumber) {
            super(new org.web3j.abi.datatypes.generated.Uint256(id), 
                    new org.web3j.abi.datatypes.Address(160, buyer), 
                    new org.web3j.abi.datatypes.Address(160, seller), 
                    new org.web3j.abi.datatypes.Utf8String(stripePaymentIntentId), 
                    new org.web3j.abi.datatypes.generated.Uint8(state), 
                    new org.web3j.abi.datatypes.Utf8String(itemId), 
                    new org.web3j.abi.datatypes.Utf8String(trackingNumber));
            this.id = id;
            this.buyer = buyer;
            this.seller = seller;
            this.stripePaymentIntentId = stripePaymentIntentId;
            this.state = state;
            this.itemId = itemId;
            this.trackingNumber = trackingNumber;
        }

        public Order(Uint256 id, Address buyer, Address seller, Utf8String stripePaymentIntentId, Uint8 state, Utf8String itemId, Utf8String trackingNumber) {
            super(id, buyer, seller, stripePaymentIntentId, state, itemId, trackingNumber);
            this.id = id.getValue();
            this.buyer = buyer.getValue();
            this.seller = seller.getValue();
            this.stripePaymentIntentId = stripePaymentIntentId.getValue();
            this.state = state.getValue();
            this.itemId = itemId.getValue();
            this.trackingNumber = trackingNumber.getValue();
        }
    }

    public static class OrderCreatedEventResponse extends BaseEventResponse {
        public BigInteger orderId;

        public String buyer;

        public String seller;

        public String itemId;

        public String stripePaymentIntentId;
    }

    public static class OrderStatusChangedEventResponse extends BaseEventResponse {
        public BigInteger orderId;

        public BigInteger newState;
    }
}
