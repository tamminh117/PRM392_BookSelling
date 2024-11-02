package com.example.bookstore.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.bookstore.R;
import com.example.bookstore.adapter.ProductAdapter;
import com.example.bookstore.model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ListView listView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private DatabaseReference databaseReference;

    //public HomeFragment(){};
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        listView = root.findViewById(R.id.product_list_view);

        // Khởi tạo Firebase Database Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        adapter = new ProductAdapter(getContext(), R.layout.list_item_product, productList);
        listView.setAdapter(adapter);

        loadProductsFromFirebase();

        // Xử lý khi nhấn vào một sản phẩm
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product selectedProduct = productList.get(position);

            // Truyền dữ liệu sản phẩm qua ProductDetailFragment
            Bundle bundle = new Bundle();
            bundle.putSerializable("product", selectedProduct);
            Navigation.findNavController(root).navigate(R.id.action_navigation_home_to_productDetailFragment, bundle);
        });
        return root;
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
                Toast.makeText(getContext(), "Không tải được dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
