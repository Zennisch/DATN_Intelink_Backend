package intelink.services;

import intelink.config.ConfigPayment;
import intelink.models.Payment;
import intelink.models.Subscription;
import intelink.models.enums.PaymentProvider;
import intelink.models.enums.PaymentStatus;
import intelink.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ConfigPayment configPayment;
    private final PaymentRepository paymentRepository;

    public String createVnpayPayment(Subscription subscription, BigDecimal amount, String currency, Map<String, String> billingInfo) throws Exception {
        Payment payment = Payment.builder()
                .subscription(subscription)
                .provider(PaymentProvider.VNPAY)
                .status(PaymentStatus.PENDING)
                .amount(amount)
                .currency(currency)
                .createdAt(Instant.now())
                .build();
        paymentRepository.save(payment);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String vnp_OrderInfo = "Thanh toán đơn hàng #" + payment.getId();
        String vnp_TmnCode = configPayment.vnp_TmnCode;
        String vnp_IpAddr = billingInfo.getOrDefault("ip", "127.0.0.1");

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount.multiply(BigDecimal.valueOf(100)).intValue()));
        vnp_Params.put("vnp_CurrCode", currency);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", configPayment.vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        vnp_Params.put("vnp_Bill_Mobile", billingInfo.getOrDefault("mobile", ""));
        vnp_Params.put("vnp_Bill_Email", billingInfo.getOrDefault("email", ""));
        vnp_Params.put("vnp_Bill_FirstName", billingInfo.getOrDefault("firstName", ""));
        vnp_Params.put("vnp_Bill_LastName", billingInfo.getOrDefault("lastName", ""));
        vnp_Params.put("vnp_Bill_Address", billingInfo.getOrDefault("address", ""));
        vnp_Params.put("vnp_Bill_City", billingInfo.getOrDefault("city", ""));
        vnp_Params.put("vnp_Bill_Country", billingInfo.getOrDefault("country", ""));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
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
        String vnp_SecureHash = configPayment.hmacSHA512(configPayment.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        payment.setTransactionId(vnp_TxnRef);
        payment.setMetadata(query.toString());
        paymentRepository.save(payment);

        return configPayment.vnp_PayUrl + "?" + query.toString();
    }
}
