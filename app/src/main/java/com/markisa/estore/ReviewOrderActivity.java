package com.markisa.estore;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;

import rms.mobile.myapplication.databinding.ReviewOrderBinding;
import com.markisa.estore.model.Product;

public class ReviewOrderActivity extends AppCompatActivity implements Serializable {

    private ReviewOrderBinding layoutBinding;
    public Product myProduct = new Product();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeUi();

    }

    private void initializeUi() {


        // Use view binding to access the UI elements
        layoutBinding = ReviewOrderBinding.inflate(getLayoutInflater());
        setContentView(layoutBinding.getRoot());

        myProduct = (Product) getIntent().getSerializableExtra("product");
        TextView subtotal = layoutBinding.subtotalValueTexView;
        double price = myProduct.getProductPrice();
        subtotal.setText(String.valueOf(price));

        TextView shipping = layoutBinding.shippingValueTextView;
        shipping.setText("0.00");

        TextView tax = layoutBinding.taxValueTextView;
        tax.setText("0.00");

        TextView orderTotal = layoutBinding.orderTotalValueTextView;
        int qty = Integer.parseInt(myProduct.getProductQty());
        double total = price * qty;
        orderTotal.setText(String.valueOf(total));

        TextView productName = layoutBinding.productNameValueTextView;
        productName.setText(String.valueOf(myProduct.getProductName()));

        TextView productQtyPrice = layoutBinding.productQtyPriceValueTextView;
        String qtyAndPrice = qty + " X " + price;
        productQtyPrice.setText(qtyAndPrice);

        ImageView imgProduct = layoutBinding.productImageView;
        String variableValue = "pink_shirt";
        imgProduct.setImageResource(getResources().getIdentifier(variableValue, "drawable", getPackageName()));
        productQtyPrice.setText(qtyAndPrice);

        Log.e("loadPaymentData", String.valueOf(myProduct.getProductName()));
        Context context = getApplicationContext();

        CharSequence text = myProduct.getProductName();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }

}
