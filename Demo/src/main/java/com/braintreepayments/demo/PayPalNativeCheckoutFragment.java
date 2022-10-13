package com.braintreepayments.demo;

import static com.braintreepayments.demo.PayPalNativeCheckoutRequestFactory.createPayPalCheckoutRequest;
import static com.braintreepayments.demo.PayPalNativeCheckoutRequestFactory.createPayPalVaultRequest;
import static com.braintreepayments.demo.factories.BraintreeClientFactory.createBraintreeClient;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.DataCollector;
import com.braintreepayments.api.PayPalNativeCheckoutAccountNonce;
import com.braintreepayments.api.PayPalNativeCheckoutListener;
import com.braintreepayments.api.PayPalNativeCheckoutClient;
import com.braintreepayments.api.PaymentMethodNonce;

public class PayPalNativeCheckoutFragment extends Fragment implements PayPalNativeCheckoutListener {

    private final String TAG = PayPalNativeCheckoutFragment.class.getName();
    private String deviceData;
    private BraintreeClient braintreeClient;
    private PayPalNativeCheckoutClient payPalClient;
    private DataCollector dataCollector;

    public Button launchPayPalNativeCheckoutButton;
    private AlertPresenter alertPresenter = new AlertPresenter();

    public PayPalNativeCheckoutFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_paypal_native_checkout, container, false);

        launchPayPalNativeCheckoutButton = view.findViewById(R.id.paypal_native_checkout_launch);
        launchPayPalNativeCheckoutButton.setOnClickListener(v -> launchPayPalNativeCheckout(false));
        braintreeClient = createBraintreeClient(requireContext());
        payPalClient = new PayPalNativeCheckoutClient(this, braintreeClient);
        payPalClient.setListener(this);
        return view;
    }

    private void launchPayPalNativeCheckout(boolean isBillingAgreement) {
        FragmentActivity activity = getActivity();
        activity.setProgressBarIndeterminateVisibility(true);

        dataCollector = new DataCollector(braintreeClient);

        braintreeClient.getConfiguration((configuration, configError) -> {
            if (Settings.shouldCollectDeviceData(requireActivity())) {
                dataCollector.collectDeviceData(requireActivity(), (deviceDataResult, error) -> {
                    if (deviceDataResult != null) {
                        deviceData = deviceDataResult;
                    }
                    try {
                        payPalClient.tokenizePayPalAccount(activity, createPayPalCheckoutRequest(activity, "1.00"));
                    } catch (Exception e) {
                        Log.i(TAG, "Unsupported type");
                    }
                });
            } else {
                try {
                    payPalClient.tokenizePayPalAccount(activity, createPayPalCheckoutRequest(activity, "1.00"));
                } catch (Exception e) {
                    Log.i(TAG, "Unsupported type");
                }

            }
        });
    }

    private void handlePayPalResult(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce != null) {

            PayPalNativeCheckoutFragmentDirections.ActionPayPalNativeCheckoutFragmentToDisplayNonceFragment action =
                PayPalNativeCheckoutFragmentDirections.actionPayPalNativeCheckoutFragmentToDisplayNonceFragment(paymentMethodNonce);
            action.setDeviceData(deviceData);

            NavHostFragment.findNavController(this).navigate(action);
        }
    }

    @Override
    public void onPayPalSuccess(@NonNull PayPalNativeCheckoutAccountNonce payPalAccountNonce) {
        handlePayPalResult(payPalAccountNonce);
    }

    @Override
    public void onPayPalFailure(@NonNull Exception error) {
        alertPresenter.showErrorDialog(this, error);
    }
}
