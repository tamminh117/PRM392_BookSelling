package com.example.bookstore.ui.user;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bookstore.MainActivity;
import com.example.bookstore.R;
import com.example.bookstore.admin.ProductManagementActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserInfoFragment extends Fragment implements OnMapReadyCallback {

    private TextView tvUserId, tvUserEmail;
    private Button btnLogout, btnChangePassword;
    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_user_info, container, false);

        Button adminButton = root.findViewById(R.id.btn_admin_management);

        tvUserId = root.findViewById(R.id.tv_user_id);
        tvUserEmail = root.findViewById(R.id.tv_user_email);
        btnLogout = root.findViewById(R.id.btn_logout);
        btnChangePassword = root.findViewById(R.id.btn_change_password);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            tvUserId.setText("User ID: " + user.getUid());
            tvUserEmail.setText("Email: " + user.getEmail());
        }

        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            // Chuyển hướng về trang đăng nhập
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish(); // Đóng activity hiện tại
        });

        btnChangePassword.setOnClickListener(v -> {
            // Xử lý logic đổi mật khẩu
            if (user != null) {
                String email = user.getEmail();
                if (email != null) {
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Thông báo thành công
                                    Toast.makeText(getContext(), "Email đổi mật khẩu đã được gửi!", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Thông báo thất bại
                                    Toast.makeText(getContext(), "Có lỗi xảy ra, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "Không tìm thấy email của người dùng.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để thực hiện thao tác này.", Toast.LENGTH_SHORT).show();
            }
        });

        // Check if user is admin
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userEmail = auth.getCurrentUser().getEmail();

        // Show admin button only for admin email
        if (userEmail != null && userEmail.equals("admin@gmail.com")) {
            adminButton.setVisibility(View.VISIBLE);
        }

        adminButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProductManagementActivity.class);
            startActivity(intent);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng storeLocation = new LatLng(10.87531, 106.80071);
        googleMap.addMarker(new MarkerOptions()
                .position(storeLocation)
                .title("Watch Store FPT")
                .snippet("Nhà Văn hóa Sinh viên TP.HCM, tầng 4 phòng 409"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 15f));
    }
}
