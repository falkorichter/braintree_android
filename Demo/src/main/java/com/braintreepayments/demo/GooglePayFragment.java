package com.braintreepayments.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.GooglePayCapabilities;
import com.braintreepayments.api.GooglePayClient;
import com.braintreepayments.api.GooglePayListener;
import com.braintreepayments.api.GooglePayRequest;
import com.braintreepayments.api.PaymentMethodNonce;
import com.google.android.gms.wallet.ShippingAddressRequirements;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;

public class GooglePayFragment extends BaseFragment implements GooglePayListener {

    private ImageButton googlePayButton;
    private GooglePayClient googlePayClient;

    private BraintreeClient braintreeClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_google_pay, container, false);
        googlePayButton = view.findViewById(R.id.google_pay_button);
        googlePayButton.setOnClickListener(this::launchGooglePay);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = requireContext();
        braintreeClient = new BraintreeClient(context, new DemoAuthorizationProvider(context));

        googlePayClient = new GooglePayClient(this, braintreeClient);
        googlePayClient.setGooglePayListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        braintreeClient.getConfiguration((configuration, error) -> {
            if (configuration == null) {
                return;
            }

            if (GooglePayCapabilities.isGooglePayEnabled(getActivity(), configuration)) {
                googlePayClient.isReadyToPay(getActivity(), (isReadyToPay, e) -> {
                    if (isReadyToPay) {
                        googlePayButton.setVisibility(View.VISIBLE);
                    } else {
                        showDialog("Google Payments are not available. The following issues could be the cause:\n\n" +
                                "No user is logged in to the device.\n\n" +
                                "Google Play Services is missing or out of date.");
                    }
                });
            } else {
                showDialog("Google Payments are not available. The following issues could be the cause:\n\n" +
                        "Google Payments are not enabled for the current merchant.\n\n" +
                        "Google Play Services is missing or out of date.");
            }
        });
    }

    @Override
    public void onPaymentMethodNonceCreated(PaymentMethodNonce paymentMethodNonce) {
        super.onPaymentMethodNonceCreated(paymentMethodNonce);

        GooglePayFragmentDirections.ActionGooglePayFragmentToDisplayNonceFragment action =
            GooglePayFragmentDirections.actionGooglePayFragmentToDisplayNonceFragment(paymentMethodNonce);

        NavHostFragment.findNavController(this).navigate(action);
    }

    public void launchGooglePay(View v) {
        FragmentActivity activity = getActivity();
        activity.setProgressBarIndeterminateVisibility(true);

        GooglePayRequest googlePayRequest = new GooglePayRequest();
        googlePayRequest.setTransactionInfo(TransactionInfo.newBuilder()
                .setCurrencyCode(Settings.getGooglePayCurrency(activity))
                .setTotalPrice("1.00")
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .build());
        googlePayRequest.setAllowPrepaidCards(Settings.areGooglePayPrepaidCardsAllowed(activity));
        googlePayRequest.setBillingAddressFormat(WalletConstants.BILLING_ADDRESS_FORMAT_FULL);
        googlePayRequest.setBillingAddressRequired(Settings.isGooglePayBillingAddressRequired(activity));
        googlePayRequest.setEmailRequired(Settings.isGooglePayEmailRequired(activity));
        googlePayRequest.setPhoneNumberRequired(Settings.isGooglePayPhoneNumberRequired(activity));
        googlePayRequest.setShippingAddressRequired(Settings.isGooglePayShippingAddressRequired(activity));
        googlePayRequest.setShippingAddressRequirements(ShippingAddressRequirements.newBuilder()
                .addAllowedCountryCodes(Settings.getGooglePayAllowedCountriesForShipping(activity))
                .build());

        googlePayClient.requestPayment(getActivity(), googlePayRequest, (requestPaymentError) -> {
            if (requestPaymentError != null) {
                handleError(requestPaymentError);
            }
        });
    }

    private void handleGooglePayActivityResult(PaymentMethodNonce paymentMethodNonce) {
        super.onPaymentMethodNonceCreated(paymentMethodNonce);

        NavDirections action =
                GooglePayFragmentDirections.actionGooglePayFragmentToDisplayNonceFragment(paymentMethodNonce);
        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public void onGooglePayTokenizeSuccess(@NonNull PaymentMethodNonce paymentMethodNonce) {
        handleGooglePayActivityResult(paymentMethodNonce);
    }

    @Override
    public void onGooglePayTokenizeError(@NonNull Exception error) {
        handleError(error);
    }
}
