package com.knubisoft.cott.runner.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerRetrieveParams;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class StripeService {

    private static final String COTT_ACCOUNT = "acct_1MEAdcDGir2utiID";
    private static final String STRIPE_API_KEY = "sk_test_"
            + "51MEAdcDGir2utiIDLA9ZTN9oFe9nOzAblhCBHv728aePnb5Ms5ZHyLA7JxRT9994C6iwf1ZPxmguepq2bIZTnZ6V00yzfqNv1r";
    private static final int CONNECT_TIMEOUT_MILLIS = 30 * 1000;
    private static final int READ_TIMEOUT_MILLIS = 40 * 1000;

    public void checkSubscription() {
        String customerId = getStripeCustomerId();
        Customer customer = retrieveCustomer(customerId);
        List<Subscription> subscriptions = customer.getSubscriptions().getData();

        boolean isActiveSubscription = subscriptions.stream()
                .map(Subscription::getStatus)
                .anyMatch("active"::equalsIgnoreCase);

        if (!isActiveSubscription) {
            throw new DefaultFrameworkException("No active subscription found for"
                    + " customer with id='%s' email='%s'", customerId, customer.getEmail());
        }
    }

    private String getStripeCustomerId() {
        String stripeCustomerId = GlobalTestConfigurationProvider.provide().getStripeCustomerId();
        if (StringUtils.isBlank(stripeCustomerId)) {
            throw new DefaultFrameworkException("Cannot find StripeCustomerId configuration");
        }
        return stripeCustomerId;
    }

    private Customer retrieveCustomer(final String customerId) {
        RequestOptions requestOptions = buildRequestOptions();
        CustomerRetrieveParams expandParams = CustomerRetrieveParams.builder()
                .addExpand("subscriptions").build();
        try {
            return Customer.retrieve(customerId, expandParams, requestOptions);
        } catch (StripeException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private RequestOptions buildRequestOptions() {
        return RequestOptions.builder()
                .setApiKey(STRIPE_API_KEY)
                .setStripeAccount(COTT_ACCOUNT)
                .setMaxNetworkRetries(2)
                .setConnectTimeout(CONNECT_TIMEOUT_MILLIS)
                .setReadTimeout(READ_TIMEOUT_MILLIS)
                .build();
    }
}
