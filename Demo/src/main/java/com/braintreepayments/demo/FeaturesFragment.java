package com.braintreepayments.demo;

import static com.braintreepayments.demo.DemoFeature.CREDIT_OR_DEBIT_CARDS;
import static com.braintreepayments.demo.DemoFeature.GOOGLE_PAY;
import static com.braintreepayments.demo.DemoFeature.LOCAL_PAYMENT;
import static com.braintreepayments.demo.DemoFeature.PAYPAL;
import static com.braintreepayments.demo.DemoFeature.PREFERRED_PAYMENT_METHODS;
import static com.braintreepayments.demo.DemoFeature.SAMSUNG_PAY;
import static com.braintreepayments.demo.DemoFeature.VENMO;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class FeaturesFragment extends Fragment implements FeaturesAdapter.ItemClickListener {

    static final String EXTRA_COLLECT_DEVICE_DATA = "collect_device_data";

    public FeaturesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_features, container, false);

        Context context = requireContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration =
            new DividerItemDecoration(context, layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        List<DemoFeature> demoFeatures = Arrays.asList(
                CREDIT_OR_DEBIT_CARDS,
                PAYPAL,
                VENMO,
                GOOGLE_PAY,
                SAMSUNG_PAY,
                // TODO: re-enable visa checkout once it works without Jetifier
                // VISA_CHECKOUT,
                LOCAL_PAYMENT,
                PREFERRED_PAYMENT_METHODS
        );

        recyclerView.setAdapter(new FeaturesAdapter(demoFeatures, this));
        return view;
    }

    @Override
    public void onFeatureSelected(DemoFeature feature) {
        switch (feature) {
            case CREDIT_OR_DEBIT_CARDS:
                launchCards();
                break;
            case PAYPAL:
                launchPayPal();
                break;
            case VENMO:
                launchVenmo();
                break;
            case GOOGLE_PAY:
                launchGooglePay();
                break;
            case SAMSUNG_PAY:
                launchSamsungPay();
                break;
            case VISA_CHECKOUT:
                launchVisaCheckout();
                break;
            case LOCAL_PAYMENT:
                launchLocalPayment();
                break;
            case PREFERRED_PAYMENT_METHODS:
                launchPreferredPaymentMethods();
                break;
        }
    }

    private void launchCards() {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_COLLECT_DEVICE_DATA, Settings.shouldCollectDeviceData(getActivity()));

        FeaturesFragmentDirections.ActionFeaturesFragmentToCardFragment action =
            FeaturesFragmentDirections.actionFeaturesFragmentToCardFragment();
        action.setShouldCollectDeviceData(Settings.shouldCollectDeviceData(getActivity()));

        navigateWith(action);
    }

    private void launchPayPal() {
        FeaturesFragmentDirections.ActionFeaturesFragmentToPayPalFragment action =
                FeaturesFragmentDirections.actionFeaturesFragmentToPayPalFragment();
        action.setShouldCollectDeviceData(Settings.shouldCollectDeviceData(getActivity()));

        navigateWith(action);
    }

    private void launchVenmo() {
        NavDirections action = FeaturesFragmentDirections.actionFeaturesFragmentToVenmoFragment();
        navigateWith(action);
    }

    private void launchGooglePay() {
        NavDirections action =
                FeaturesFragmentDirections.actionFeaturesFragmentToGooglePayFragment();
        navigateWith(action);
    }

    private void launchSamsungPay() {
        NavDirections action =
                FeaturesFragmentDirections.actionFeaturesFragmentToSamsungPayFragment();
        navigateWith(action);
    }

    private void launchVisaCheckout() {
        NavDirections action = FeaturesFragmentDirections.actionFeaturesFragmentToVisaCheckoutFragment();
        navigateWith(action);
    }

    private void launchLocalPayment() {
        NavDirections action =
                FeaturesFragmentDirections.actionFeaturesFragmentToLocalPaymentFragment();
        navigateWith(action);
    }

    private void launchPreferredPaymentMethods() {
        NavDirections action =
                FeaturesFragmentDirections.actionFeaturesFragmentToPreferredPaymentMethodsFragment();
        navigateWith(action);
    }

    private void navigateWith(NavDirections action) {
        Fragment parentFragment = getParentFragment();
        if (parentFragment != null) {
            NavController navController = NavHostFragment.findNavController(parentFragment);
            navController.navigate(action);
        }
    }
}