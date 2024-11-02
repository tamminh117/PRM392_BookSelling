package com.example.bookstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.bookstore.R;
import com.example.bookstore.model.Product;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Product> {

    private Context context;
    private int resource;
    private List<Product> products;

    public ProductAdapter(Context context, int resource, List<Product> products) {
        super(context, resource, products);
        this.context = context;
        this.resource = resource;
        this.products = products;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the layout if it's not already created
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        // Get the current product from the list
        Product product = products.get(position);

        // Find the views in the layout
        TextView nameTextView = convertView.findViewById(R.id.product_name);
        TextView priceTextView = convertView.findViewById(R.id.product_price);
        ImageView imageView = convertView.findViewById(R.id.product_image);

        // Set product data to the views
        nameTextView.setText(product.getName());
        priceTextView.setText(String.format("$%.2f", product.getPrice()));  // Display price as $xx.xx

        Glide.with(context)
                .load(product.getImageUrl())  // URL of the product image
                .placeholder(R.drawable.ic_launcher_foreground)  // Placeholder image when loading
                .error(R.drawable.error_image)  // Error image if loading fails
                .into(imageView);  // Load the image into the ImageView

        return convertView;
    }
}
