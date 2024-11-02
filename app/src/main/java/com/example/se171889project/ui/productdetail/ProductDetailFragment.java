package com.example.se171889project.ui.productdetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.se171889project.R;
import com.example.se171889project.model.CartItem;
import com.example.se171889project.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProductDetailFragment extends Fragment {

    private TextView nameTextView;
    private TextView priceTextView;
    private TextView descriptionTextView;
    private ImageView productImageView;
    private EditText quantityEditText; // EditText để nhập số lượng
    private Button addToCartButton; // Nút thêm vào giỏ hàng

    // Thêm vào đầu file
    private FirebaseAuth firebaseAuth;
    private DatabaseReference cartRef; // Tham chiếu đến giỏ hàng trong Firebase

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_detail, container, false);

        // Add this near the start of onCreateView
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(root).navigate(R.id.action_productDetailFragment_to_navigation_home);
            }
        });

        // Add this after your view initialization
        ImageButton backButton = root.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Navigation.findNavController(root).navigate(R.id.action_productDetailFragment_to_navigation_home);
        });

        // Khởi tạo Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid(); // Lấy userId
        cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId).child("cartItems");

        // Khởi tạo các View
        nameTextView = root.findViewById(R.id.product_name_detail);
        priceTextView = root.findViewById(R.id.product_price_detail);
        descriptionTextView = root.findViewById(R.id.product_description);
        productImageView = root.findViewById(R.id.product_image_detail);
        quantityEditText = root.findViewById(R.id.quantity_edit_text); // Nhận EditText
        addToCartButton = root.findViewById(R.id.add_to_cart_button); // Nhận nút

        // Nhận dữ liệu sản phẩm từ Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            Product product = (Product) bundle.getSerializable("product");
            if (product != null) {
                // Hiển thị thông tin sản phẩm
                nameTextView.setText(product.getName());
                priceTextView.setText(String.valueOf(product.getPrice()));
                descriptionTextView.setText(product.getDescription());

                // Sử dụng Glide để tải hình ảnh sản phẩm
                Glide.with(getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)  // Hình ảnh khi đang tải
                        .error(R.drawable.error_image)  // Hình ảnh lỗi nếu không tải được
                        .into(productImageView);

                // Thêm sự kiện cho nút "Thêm vào giỏ hàng"
                addToCartButton.setOnClickListener(v -> {
                    String quantityString = quantityEditText.getText().toString();
                    if (!quantityString.isEmpty()) {
                        int quantity = Integer.parseInt(quantityString);
                        if (quantity > 0) {
                            addToCart(product, quantity); // Gọi hàm thêm vào giỏ hàng
                        } else {
                            Toast.makeText(getContext(), "Số lượng phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return root;
    }

    // Phương thức thêm sản phẩm vào giỏ hàng
    private void addToCart(Product product, int quantity) {
        String productId = product.getId(); // Giả sử bạn có phương thức getId() để lấy ID sản phẩm
        CartItem newCartItem = new CartItem(productId, product.getName(), product.getPrice(), quantity, product.getImageUrl());

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        cartRef.child(productId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    // Sản phẩm đã có trong giỏ hàng, cập nhật số lượng
                    cartRef.child(productId).child("quantity").setValue(quantity)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Cập nhật giỏ hàng thành công", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show());
                } else {
                    // Sản phẩm chưa có, thêm mới vào giỏ hàng
                    cartRef.child(productId).setValue(newCartItem)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show());
                }
            } else {
                Toast.makeText(getContext(), "Lỗi truy vấn giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
