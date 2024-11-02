package com.example.bookstore.admin;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookstore.MainActivity;
import com.example.bookstore.R;
import com.example.bookstore.adapter.ProductAdapter;
import com.example.bookstore.model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {
    private ListView listViewProducts;
    private ProductAdapter adapter;
    private List<Product> productList;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private FirebaseAuth auth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri; // Biến để lưu URI của ảnh đã chọn

    private ImageView imageViewProduct; // Biến thành viên để giữ tham chiếu đến ImageView trong dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        // Initialize back button
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

        auth = FirebaseAuth.getInstance();

        // Initialize views
        listViewProducts = findViewById(R.id.product_list_view1);
        ImageButton logoutButton = findViewById(R.id.logout_button);

        productList = new ArrayList<>();
        adapter = new ProductAdapter(this, R.layout.list_item_product, productList);
        listViewProducts.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("products");
        storage = FirebaseStorage.getInstance();

        loadProductsFromFirebase();

        listViewProducts.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = productList.get(position);
            showProductDialog(selectedProduct);
        });

        // Initialize FAB
        FloatingActionButton addButton = findViewById(R.id.add_product_button);
        addButton.setOnClickListener(v -> showProductDialog(null));

        // Handle logout
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(ProductManagementActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadProductsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        product.setId(snapshot.getKey());
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Không tải được dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProductDialog(@Nullable Product product) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_product);

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Initialize exit button
        ImageButton exitButton = dialog.findViewById(R.id.buttonExit);
        exitButton.setOnClickListener(v -> dialog.dismiss());

        EditText editTextName = dialog.findViewById(R.id.editTextProductName);
        EditText editTextPrice = dialog.findViewById(R.id.editTextProductPrice);
        EditText editTextDescription = dialog.findViewById(R.id.editTextProductDescription);
        imageViewProduct = dialog.findViewById(R.id.imageViewProduct); // Gán ImageView vào biến thành viên
        Button buttonUploadImage = dialog.findViewById(R.id.buttonUploadImage);
        Button buttonSave = dialog.findViewById(R.id.buttonSave);
        Button buttonDelete = dialog.findViewById(R.id.buttonDelete);

        if (product != null) {
            editTextName.setText(product.getName());
            editTextPrice.setText(String.valueOf(product.getPrice()));
            editTextDescription.setText(product.getDescription());
            // Update the existing dialog's imageView
            if (product.getImageUrl() != null) {
                Picasso.get().load(product.getImageUrl()).into(imageViewProduct);
            }
        } else {
            buttonDelete.setVisibility(View.GONE); // Ẩn nút xóa cho sản phẩm mới
        }

        buttonUploadImage.setOnClickListener(v -> uploadImage(imageViewProduct)); // Truyền imageView vào uploadImage
        buttonSave.setOnClickListener(v -> {
            if (validateInputs(editTextName, editTextPrice)) {
                saveProduct(product, editTextName.getText().toString(),
                        Double.parseDouble(editTextPrice.getText().toString()),
                        editTextDescription.getText().toString());
                dialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(v -> {
            if (product != null) {
                deleteProduct(product);
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void uploadImage(ImageView imageView) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
//            // Tìm view dialog thay vì activity
//            Dialog dialog = new Dialog(this);
//            dialog.setContentView(R.layout.dialog_product);
//            ImageView imageViewProduct = dialog.findViewById(R.id.imageViewProduct);
            // Hiển thị ảnh đã chọn
            imageViewProduct.setImageURI(imageUri);

            // Tải ảnh lên Firebase Storage
            StorageReference storageReference = storage.getReference("product/" + System.currentTimeMillis() + ".jpg");
            storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUri = uri; // Lưu URL ảnh sau khi tải lên thành công
                        Toast.makeText(this, "Ảnh đã được tải lên thành công", Toast.LENGTH_SHORT).show();
                    })
            ).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


    private boolean validateInputs(EditText name, EditText price) {
        if (TextUtils.isEmpty(name.getText())) {
            Toast.makeText(this, "Please enter product name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(price.getText())) {
            Toast.makeText(this, "Please enter product price", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveProduct(@Nullable Product product, String name, double price, String description) {
        if(product == null) {
            if (imageUri == null) {
                Toast.makeText(this, "Vui lòng tải lên ảnh trước khi lưu sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        String imageUrl = imageUri.toString(); // Lấy URL ảnh
        if (product == null) {
            String id = databaseReference.push().getKey();
            Product newProduct = new Product(id, name, price, imageUrl, description); // Lưu URL ảnh vào product
            databaseReference.child(id).setValue(newProduct);
            Toast.makeText(this, "Sản phẩm đã được thêm thành công", Toast.LENGTH_SHORT).show();
        } else {
            product.setName(name);
            product.setPrice(price);
            product.setDescription(description);
            if(imageUri != null) {
                product.setImageUrl(imageUrl); // Cập nhật URL ảnh
            }
            databaseReference.child(product.getId()).setValue(product);
            Toast.makeText(this, "Sản phẩm đã được cập nhật thành công", Toast.LENGTH_SHORT).show();
        }
        loadProductsFromFirebase();
    }

    private void deleteProduct(Product product) {
        databaseReference.child(product.getId()).removeValue();
        Toast.makeText(this, "Sản phẩm đã được xóa thành công", Toast.LENGTH_SHORT).show();
        loadProductsFromFirebase();
    }
}
