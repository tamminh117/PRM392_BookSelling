package com.example.bookstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.bookstore.R;
import com.example.bookstore.model.CartItem;

import java.util.List;

public class CartAdapter extends ArrayAdapter<CartItem> {
    private final Context context;
    private final List<CartItem> cartItems;
    private final OnCartActionListener listener;

    public interface OnCartActionListener {
        void onRemoveCartItem(CartItem cartItem);
        void onUpdateQuantity(CartItem cartItem, int quantity);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartActionListener listener) {
        super(context, R.layout.cart_item, cartItems);
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        CartItem cartItem = cartItems.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.cart_item_image);
        TextView nameTextView = convertView.findViewById(R.id.cart_item_name);
        TextView priceTextView = convertView.findViewById(R.id.cart_item_price);
        EditText quantityEditText = convertView.findViewById(R.id.cart_item_quantity);
        Button removeButton = convertView.findViewById(R.id.remove_button);
        Button updateButton = convertView.findViewById(R.id.update_button);

        // Hiển thị thông tin sản phẩm
        nameTextView.setText(cartItem.getName());
        priceTextView.setText(String.valueOf(cartItem.getPrice()));
        quantityEditText.setText(String.valueOf(cartItem.getQuantity()));
        Glide.with(context).load(cartItem.getImageUrl()).into(imageView);

        // Xử lý sự kiện xóa sản phẩm
        removeButton.setOnClickListener(v -> listener.onRemoveCartItem(cartItem));

        // Xử lý sự kiện cập nhật số lượng
        updateButton.setOnClickListener(v -> {
            int quantity = Integer.parseInt(quantityEditText.getText().toString());
            listener.onUpdateQuantity(cartItem, quantity);
        });

        return convertView;
    }
}