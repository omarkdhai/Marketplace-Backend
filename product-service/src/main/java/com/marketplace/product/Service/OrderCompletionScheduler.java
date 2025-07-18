package com.marketplace.product.Service;

import com.marketplace.product.Entity.ProceedOrder;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class OrderCompletionScheduler {

    @Inject
    ProceedOrderService proceedOrderService;

    @Inject
    BlockchainService blockchainService;

    @Scheduled(every = "1h")
    void autoCompleteShippedOrders() {
        System.out.println("SCHEDULER: Running job to auto-complete old shipped orders...");

        Instant thresholdInstant = Instant.now().minus(2, ChronoUnit.DAYS);
        Date thresholdDate = Date.from(thresholdInstant);
        System.out.println("SCHEDULER: Searching for orders shipped before: " + thresholdDate);

        List<ProceedOrder> ordersToComplete = ProceedOrder.list(
                "paymentStatus = ?1 and shippedAt < ?2",
                "SHIPPED",
                thresholdDate
        );

        if (ordersToComplete.isEmpty()) {
            System.out.println("SCHEDULER: No orders found to auto-complete.");
            return;
        }

        System.out.println("SCHEDULER: Found " + ordersToComplete.size() + " order(s) to auto-complete.");

        for (ProceedOrder order : ordersToComplete) {
            System.out.println("SCHEDULER: Auto-completing order " + order.id.toString());

            if (order.blockchainOrderId == null) {
                System.err.println("SCHEDULER: Skipping order " + order.id + " because it has no blockchain ID.");
                continue;
            }

            BigInteger blockchainOrderId = BigInteger.valueOf(order.blockchainOrderId);
            String mongoOrderId = order.id.toString();

            try {
                CompletableFuture<TransactionReceipt> futureReceipt = blockchainService.confirmOrderDelivered(blockchainOrderId);

                futureReceipt.thenAccept(receipt -> {
                    System.out.println("SCHEDULER: 'confirmDelivered' tx confirmed for " + mongoOrderId + "! TxHash: " + receipt.getTransactionHash());
                    proceedOrderService.updateOrderStatusToCompleted(mongoOrderId, receipt.getTransactionHash());
                }).exceptionally(ex -> {
                    System.err.println("SCHEDULER: 'confirmDelivered' tx FAILED for " + mongoOrderId);
                    ex.printStackTrace();
                    return null;
                });

            } catch (Exception e) {
                System.err.println("SCHEDULER: Failed to send 'confirmDelivered' transaction for order: " + mongoOrderId);
                e.printStackTrace();
            }
        }
    }
}
