package com.resume.backend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // Define your Base Prices in INR (Change these to fit your business model)
    private static final int PRICE_1_MONTH = 299;
    private static final int PRICE_6_MONTHS = 1499;
    private static final int PRICE_PAYG_50_CREDITS = 99;

    public Order createOrder(String planId, int discountPercentage) throws RazorpayException {
        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        // 1. Fetch the base price based on the selected plan
        int basePriceInRupees;
        switch (planId) {
            case "1_MONTH":
                basePriceInRupees = PRICE_1_MONTH;
                break;
            case "6_MONTHS":
                basePriceInRupees = PRICE_6_MONTHS;
                break;
            case "PAYG_50":
                basePriceInRupees = PRICE_PAYG_50_CREDITS;
                break;
            default:
                // Defaulting to Lifetime/6-Months as safety fallback
                basePriceInRupees = PRICE_6_MONTHS;
        }

        // 2. Apply the spinning wheel discount
        double discountMultiplier = (100.0 - discountPercentage) / 100.0;
        int finalPriceInRupees = (int) Math.max(10, (basePriceInRupees * discountMultiplier)); // Minimum 10 INR

        // Razorpay requires the amount in paise (1 INR = 100 Paise)
        int amountInPaise = finalPriceInRupees * 100;

        // 3. Build the order request
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        // Attach metadata so we know what they bought when the webhook/verification fires
        JSONObject notes = new JSONObject();
        notes.put("planId", planId);
        orderRequest.put("notes", notes);

        // 4. Create order on Razorpay servers
        return razorpay.orders.create(orderRequest);
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException e) {
            System.err.println("Signature verification failed: " + e.getMessage());
            return false;
        }
    }
}