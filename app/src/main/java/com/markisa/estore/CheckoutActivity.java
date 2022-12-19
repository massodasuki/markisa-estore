package com.markisa.estore;

import android.content.Context;
import android.content.Intent;
import android.icu.text.DecimalFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;

import rms.mobile.myapplication.R;

import com.markisa.estore.viewmodel.CheckoutViewModel;
import com.markisa.estore.model.Product;

import java.io.Serializable;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import rms.mobile.myapplication.databinding.ActivityCheckoutBinding;

import rms.library.googlepay.Helper.RMSGooglePay;


/**
 * Checkout implementation for the app
 */
public class CheckoutActivity extends AppCompatActivity {

    // Arbitrarily-picked constant integer you define to track a request for payment data activity.
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private static int COUNT = 0;

    private CheckoutViewModel model;

    private ActivityCheckoutBinding layoutBinding;
    private View googlePayButton;
    public Product myProduct = new Product();

    JSONObject paymentInputObj = new JSONObject();



    void processValue(String myValue) {
        //handle value
        //Update GUI, show toast, etc..
        Toast.makeText(
                this, getString(R.string.payments_show_name, myValue),
                Toast.LENGTH_LONG).show();
    }

    /**
     * Initialize the Google Pay API on creation of the activity
     *
     * @see AppCompatActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUi();

        model = new ViewModelProvider(this).get(CheckoutViewModel.class);
        model.canUseGooglePay.observe(this, this::setGooglePayAvailable);
    }

    private void initializeUi() {

//        Cart myCart = new Cart();
        // Use view binding to access the UI elements
        layoutBinding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());
        TextView quantity = layoutBinding.quantityTextView;

        Button incrementButton = layoutBinding.increaseButton;
        incrementButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                COUNT += 1;
                myProduct.setProductQty(String.valueOf(COUNT));
                quantity.setText(String.valueOf(COUNT));

                myProduct.setProductId(1);
                myProduct.setProductName("Pink Shirt");
                myProduct.setProductPrice(10);
                myProduct.setImageUrl("pink_shirt");

                Context context = getApplicationContext();
//                CharSequence text = String.valueOf(COUNT);
                CharSequence text = myProduct.getProductName();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        Button decrementButton = layoutBinding.decreaseButton;
        decrementButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (COUNT != 0){
                    COUNT -= 1;
                } else if (COUNT == 0 ) {
                    myProduct.setProductId(0);
                    myProduct.setProductName("");
                    myProduct.setProductPrice(0);
                    myProduct.setImageUrl("");
                    myProduct.setProductQty("0");
                }
                myProduct.setProductQty(String.valueOf(COUNT));
                quantity.setText(String.valueOf(COUNT));
                // Do something in response to button click
                Context context = getApplicationContext();
                CharSequence text = String.valueOf(COUNT);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        });

        Button addToCart = layoutBinding.addToCartButton;
        addToCart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
//                startActivity(new Intent(CheckoutActivity.this, ReviewOrderActivity.class));
                if (COUNT == 0 ) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please add quantity";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    Intent i = new Intent(CheckoutActivity.this, ReviewOrderActivity.class);
                    i.putExtra("product", (Serializable) myProduct);
                    startActivity(i);
                }

            }
        });

        // The Google Pay button is a layout file – take the root view
        googlePayButton = layoutBinding.googlePayButton.getRoot();
        googlePayButton.setOnClickListener(this::requestPayment);
    }

    /**
     * If isReadyToPay returned {@code true}, show the button and hide the "checking" text.
     * Otherwise, notify the user that Google Pay is not available. Please adjust to fit in with
     * your current user flow. You are not required to explicitly let the user know if isReadyToPay
     * returns {@code false}.
     *
     * @param available isReadyToPay API response.
     */
    private void setGooglePayAvailable(boolean available) {
        if (available) {
            googlePayButton.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, R.string.googlepay_status_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    public void requestPayment(View view) {
        final DecimalFormat df = new DecimalFormat("0.00");
        // Disables the button to prevent multiple clicks.
        googlePayButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.

        // Value for transaction insert here
        double priceCents = 1.10;
        long shippingCostCents = 0;
        double totalPriceCents = priceCents + shippingCostCents;
//        JSONObject paymentInputObj = new JSONObject();
        try {
            paymentInputObj.put("orderId", "order111");
            paymentInputObj.put("amount", df.format(totalPriceCents));
            paymentInputObj.put("currency", "MYR");
            paymentInputObj.put("billName", "Abd Qayyum");
            paymentInputObj.put("billEmail", "qayyum.ishak@razer.com");
            paymentInputObj.put("billPhone", "60133833895");
            paymentInputObj.put("billDesc", "Google Pay Testing");
            paymentInputObj.put("merchantId", "rmsxdk_mobile_Dev");
            paymentInputObj.put("verificationKey", "ee738b541eff7b6b495e44771f71c0ec");
            paymentInputObj.put("isSandbox", "false");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Task<PaymentData> task = model.getLoadPaymentDataTask(totalPriceCents);

        // Shows the payment sheet and forwards the result to the onActivityResult method.
        AutoResolveHelper.resolveTask(task, this, LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode  Result code returned by the Google Pay API.
     * @param data        Intent from the Google Pay API containing payment or error data.
     * @see <a href="https://developer.android.com/training/basics/intents/result">Getting a result
     * from an Activity</a>
     */
    @SuppressWarnings("deprecation")
    // Suppressing deprecation until `registerForActivityResult` can be used with the Google Pay API.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            // value passed in AutoResolveHelper
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {

                    case AppCompatActivity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        handlePaymentSuccess(paymentData);
                        setContentView(R.layout.purchased_success);
                        break;

                    case AppCompatActivity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        break;

                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        handleError(status);
                        break;
                }

                // Re-enables the Google Pay payment button.
                googlePayButton.setClickable(true);
        }
    }




    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see <a href="https://developers.google.com/pay/api/android/reference/
     * object#PaymentData">PaymentData</a>
     */


    private void handlePaymentSuccess(@Nullable PaymentData paymentData) {
        final String paymentInfo = paymentData.toJson();
        Log.i("loadPaymentData", paymentInfo);
        String paymentInput = paymentInputObj.toString();
        PaymentTaskRunner runner = new PaymentTaskRunner();
        runner.execute(paymentInput, paymentInfo);
    }

    private class PaymentTaskRunner extends AsyncTask<String, String, String> {

        private String resp;
        @Override
        protected String doInBackground(String... params) {
            try {
                RMSGooglePay pay = new RMSGooglePay();
                JSONObject result = (JSONObject) pay.requestPayment(
                        params[0],
                        params[1]
                );

                resp = result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }
            Log.i("PaymentTaskRunner doInBackground", resp);
            return resp;
        }
        @Override
        protected void onPostExecute(String result) {
            Log.i("PaymentTaskRunner onPostExecute", "Done");
            processValue(result);
        }
        @Override
        protected void onPreExecute() {
            Log.i("PaymentTaskRunner onPreExecute", "preExecute");
        }
        @Override
        protected void onProgressUpdate(String... text) {
            Log.i("PaymentTaskRunner onProgressUpdate", "progressUpdate");
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param status will hold the value of any constant from CommonStatusCode or one of the
     *               WalletConstants.ERROR_CODE_* constants.
     * @see <a href="https://developers.google.com/android/reference/com/google/android/gms/wallet/
     * WalletConstants#constant-summary">Wallet Constants Library</a>
     */
    private void handleError(@Nullable Status status) {
        String errorString = "Unknown error.";
        if (status != null) {
            int statusCode = status.getStatusCode();
            Log.e("loadPaymentData failed", String.valueOf(statusCode));
            errorString = String.format(Locale.getDefault(), "Error code: %d", statusCode);
        }

        Log.e("loadPaymentData failed", errorString);
    }
}