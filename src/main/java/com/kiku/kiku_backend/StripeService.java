package com.kiku.kiku_backend;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String apiKey;

    @Value("${stripe.price.standard}")
    private String standardPriceId;

    @Value("${stripe.price.night}")
    private String nightPriceId;

    public String createCheckoutSession(String sessionType, String bookingId, String customerEmail)
            throws StripeException {
        Stripe.apiKey = apiKey;

        String priceId = sessionType.equals("night") ? nightPriceId : standardPriceId;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(customerEmail)
                .setSuccessUrl("https://kiku-support.com/booking/success?id=" + bookingId)
                .setCancelUrl("https://kiku-support.com/booking/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setPrice(priceId)
                        .setQuantity(1L)
                        .build())
                .putMetadata("booking_id", bookingId)
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}
