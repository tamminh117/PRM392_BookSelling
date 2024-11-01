package com.example.se171889project.ui.cart;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.se171889project.R;
import com.example.se171889project.adapter.CartAdapter;
import com.example.se171889project.model.CartItem;
import com.example.se171889project.model.Transaction;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartActionListener {

    private ListView cartListView;
    private Button checkoutButton;
    private List<CartItem> cartItems;
    private CartAdapter cartAdapter;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference cartRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cart, container, false);
        cartListView = root.findViewById(R.id.cart_list_view);
        checkoutButton = root.findViewById(R.id.checkout_button);
        TextView emptyCartText = root.findViewById(R.id.empty_cart_text);
        TextView totalPriceText = root.findViewById(R.id.total_price_text);

        // Khởi tạo Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid(); // Lấy userId của người dùng hiện tại
        cartRef = FirebaseDatabase.getInstance().getReference("carts").child(userId).child("cartItems");

        // Khởi tạo danh sách và adaptera
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(getContext(), cartItems, this);
        cartListView.setAdapter(cartAdapter);

        // Thêm sự kiện cho nút thanh toán
//        checkoutButton.setOnClickListener(v -> {
//            // Thực hiện thanh toán
//            // TODO: Thêm logic thanh toán ở đây
//            Toast.makeText(getContext(), "Chức năng thanh toán chưa được triển khai", Toast.LENGTH_SHORT).show();
//        });
        checkoutButton.setOnClickListener(v -> showDeliveryAddressDialog());

        // Tải giỏ hàng từ Firebase
        //loadCartItems();
        // Load cart items from Firebase
        loadCartItems(emptyCartText, totalPriceText);

        return root;
    }

    private void loadCartItems(TextView emptyCartText, TextView totalPriceText) {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartItems.clear();
                double totalPrice = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CartItem cartItem = snapshot.getValue(CartItem.class);
                    cartItems.add(cartItem);
                    totalPrice += cartItem.getPrice() * cartItem.getQuantity(); // Assuming `getPrice` and `getQuantity` methods
                }

                cartAdapter.notifyDataSetChanged();

                // Update UI based on cart content
                if (cartItems.isEmpty()) {
                    emptyCartText.setVisibility(View.VISIBLE);
                    checkoutButton.setVisibility(View.GONE);
                    totalPriceText.setVisibility(View.GONE);
                } else {
                    emptyCartText.setVisibility(View.GONE);
                    checkoutButton.setVisibility(View.VISIBLE);
                    totalPriceText.setVisibility(View.VISIBLE);
                    totalPriceText.setText("Total price: " + totalPrice + " $");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRemoveCartItem(CartItem cartItem) {
        // Xóa sản phẩm khỏi giỏ hàng
        cartRef.child(cartItem.getProductId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    cartItems.remove(cartItem);
                    cartAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onUpdateQuantity(CartItem cartItem, int quantity) {
        // Cập nhật số lượng sản phẩm trong giỏ hàng
        cartRef.child(cartItem.getProductId()).child("quantity").setValue(quantity)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã cập nhật số lượng", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show());
    }
    private void showDeliveryAddressDialog() {
        // Tạo AlertDialog với một thông báo và nút để mở MapActivity
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn địa chỉ giao hàng");

        builder.setMessage("Nhấn OK để chọn địa chỉ trên bản đồ");

        // Nút xác nhận
        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent(getContext(), MapActivity.class);
            startActivityForResult(intent, 1); // Gọi MapActivity với mã yêu cầu là 1
        });

        // Nút hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            String deliveryAddress = "Lat: " + latitude + ", Lng: " + longitude; // Hoặc chuyển đổi tọa độ sang địa chỉ

            createTransaction(deliveryAddress); // Gọi hàm tạo giao dịch và truyền địa chỉ
        }
    }

    private void createTransaction(String deliveryAddress) {
        String userId = firebaseAuth.getCurrentUser().getUid();
        String transactionId = cartRef.push().getKey(); // Tạo ID duy nhất cho giao dịch
        double totalPrice = calculateTotalPrice(); // Hàm tính tổng giá trị giỏ hàng

        // Tạo đối tượng giao dịch và bao gồm địa chỉ giao hàng
        Transaction transaction = new Transaction(transactionId, userId, totalPrice, new ArrayList<>(cartItems), System.currentTimeMillis(), deliveryAddress);

        // Tham chiếu đến node "transactions" trong Firebase
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(userId).child(transactionId);

        // Lưu giao dịch vào Firebase
        transactionsRef.setValue(transaction)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                    clearCart(); // Xóa giỏ hàng sau khi thanh toán thành công
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi tạo giao dịch", Toast.LENGTH_SHORT).show());
    }

    private double calculateTotalPrice() {
        double totalPrice = 0;
        for (CartItem item : cartItems) {
            totalPrice += item.getPrice() * item.getQuantity(); // Tính tổng giá trị giỏ hàng
        }
        return totalPrice;
    }

    private void clearCart() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        cartRef.removeValue() // Xóa giỏ hàng
                .addOnSuccessListener(aVoid -> {
                    cartItems.clear();
                    cartAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Giỏ hàng đã được xóa", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi khi xóa giỏ hàng", Toast.LENGTH_SHORT).show());
    }

}
