package intelink.modules.payment.services;

import intelink.configs.VNPayConfig;
import intelink.dto.payment.BillingInfo;
import intelink.dto.payment.CreateVNPayPaymentRequest;
import intelink.dto.payment.CreateVNPayPaymentResponse;
import intelink.models.Payment;
import intelink.models.Subscription;
import intelink.models.enums.PaymentProvider;
import intelink.models.enums.PaymentStatus;
import intelink.modules.payment.repositories.PaymentRepository;
import intelink.modules.subscription.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public CreateVNPayPaymentResponse createVNPayPayment(CreateVNPayPaymentRequest request) {
        try {
            // Find subscription
            Subscription subscription = subscriptionRepository.findById(request.subscriptionId())
                    .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + request.subscriptionId()));

            // Create payment record
            Payment payment = Payment.builder()
                    .subscription(subscription)
                    .provider(PaymentProvider.VNPAY)
                    .status(PaymentStatus.PENDING)
                    .amount(request.amount())
                    .currency(request.currency())
                    .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                    .build();
            
            payment = paymentRepository.save(payment);

            // Generate VNPay payment URL
            String paymentUrl = generateVNPayUrl(payment, request.billingInfo());

            return CreateVNPayPaymentResponse.builder()
                    .code("00")
                    .message("Success")
                    .paymentUrl(paymentUrl)
                    .build();

        } catch (IllegalArgumentException e) {
            log.error("[PaymentService.createVNPayPayment] Error: {}", e.getMessage());
            return CreateVNPayPaymentResponse.builder()
                    .code("01")
                    .message(e.getMessage())
                    .paymentUrl(null)
                    .build();
        } catch (Exception e) {
            log.error("[PaymentService.createVNPayPayment] Unexpected error: {}", e.getMessage(), e);
            return CreateVNPayPaymentResponse.builder()
                    .code("99")
                    .message("System error: " + e.getMessage())
                    .paymentUrl(null)
                    .build();
        }
    }

    private String generateVNPayUrl(Payment payment, BillingInfo billingInfo) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String vnp_OrderInfo = "Payment for subscription #" + payment.getSubscription().getId();
        String vnp_IpAddr = billingInfo != null && billingInfo.ip() != null ? billingInfo.ip() : "127.0.0.1";

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        
        // Convert amount to VND cents (multiply by 100)
        long amountInCents = (long) (payment.getAmount() * 100);
        vnp_Params.put("vnp_Amount", String.valueOf(amountInCents));
        
        vnp_Params.put("vnp_CurrCode", payment.getCurrency());
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Create date and expire date
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(calendar.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        calendar.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(calendar.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Add billing info
        if (billingInfo != null) {
            vnp_Params.put("vnp_Bill_Mobile", billingInfo.mobile() != null ? billingInfo.mobile() : "");
            vnp_Params.put("vnp_Bill_Email", billingInfo.email() != null ? billingInfo.email() : "");
            vnp_Params.put("vnp_Bill_FirstName", billingInfo.firstName() != null ? billingInfo.firstName() : "");
            vnp_Params.put("vnp_Bill_LastName", billingInfo.lastName() != null ? billingInfo.lastName() : "");
            vnp_Params.put("vnp_Bill_Address", billingInfo.address() != null ? billingInfo.address() : "");
            vnp_Params.put("vnp_Bill_City", billingInfo.city() != null ? billingInfo.city() : "");
            vnp_Params.put("vnp_Bill_Country", billingInfo.country() != null ? billingInfo.country() : "");
        }

        // Build hash data and query string
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnp_Params.get(fieldName);
            
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                if (i < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        // Generate secure hash
        String vnp_SecureHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        // Update payment with transaction ID and metadata
        payment.setTransactionId(vnp_TxnRef);
        payment.setMetadata(query.toString());
        paymentRepository.save(payment);

        log.info("[PaymentService.generateVNPayUrl] Generated payment URL for payment ID: {}, txnRef: {}", 
                payment.getId(), vnp_TxnRef);

        return vnPayConfig.getPayUrl() + "?" + query;
    }

    @Transactional(readOnly = true)
    public Payment findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Payment findByTransactionIdWithSubscription(String transactionId) {
        return paymentRepository.findByTransactionIdWithSubscription(transactionId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found with id: " + id));
    }

    @Transactional
    public Payment updatePaymentStatus(UUID paymentId, PaymentStatus status) {
        Payment payment = findById(paymentId);
        payment.setStatus(status);
        
        if (status == PaymentStatus.COMPLETED || status == PaymentStatus.FAILED) {
            payment.setProcessedAt(Instant.now());
        }
        
        return paymentRepository.save(payment);
    }

    /**
     * Verify VNPay callback signature
     */
    public boolean verifyVNPayCallback(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
            log.warn("[PaymentService.verifyVNPayCallback] Missing vnp_SecureHash");
            return false;
        }

        // Remove secure hash from params
        Map<String, String> filteredParams = new HashMap<>(params);
        filteredParams.remove("vnp_SecureHash");
        filteredParams.remove("vnp_SecureHashType");

        // Sort and build hash data
        List<String> fieldNames = new ArrayList<>(filteredParams.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = filteredParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build in URL format WITH URL encoding
                try {
                    hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                } catch (Exception e) {
                    log.error("[PaymentService.verifyVNPayCallback] Error encoding: {}", e.getMessage());
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(fieldValue);
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // Calculate hash
        String calculatedHash = vnPayConfig.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        log.info("[PaymentService.verifyVNPayCallback] Hash data: {}", hashData.toString());
        log.info("[PaymentService.verifyVNPayCallback] Expected hash: {}", vnpSecureHash);
        log.info("[PaymentService.verifyVNPayCallback] Calculated hash: {}", calculatedHash);

        return calculatedHash.equalsIgnoreCase(vnpSecureHash);
    }
}
