package com.example.bookstore.ui.payment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bookstore.R;
import com.example.bookstore.adapter.TransactionAdapter;
import com.example.bookstore.model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PaymentHistoryFragment extends Fragment {

    private ListView paymentHistoryListView;
    private TextView emptyHistoryText;
    private List<Transaction> transactions;
    private TransactionAdapter transactionAdapter;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference transactionsRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_payment_history, container, false);
        paymentHistoryListView = root.findViewById(R.id.payment_history_list_view);
        emptyHistoryText = root.findViewById(R.id.empty_history_text);

        // Khởi tạo Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid(); // Lấy userId của người dùng hiện tại
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(userId);

        // Khởi tạo danh sách và adapter
        transactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(getContext(), transactions);
        paymentHistoryListView.setAdapter(transactionAdapter);

        // Tải lịch sử giao dịch từ Firebase
        loadTransactionHistory();

        return root;
    }

    private void loadTransactionHistory() {
        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transactions.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    if (transaction != null) {
                        transactions.add(transaction);
                    }
                }

                transactionAdapter.notifyDataSetChanged();

                // Cập nhật giao diện dựa trên lịch sử giao dịch
                if (transactions.isEmpty()) {
                    emptyHistoryText.setVisibility(View.VISIBLE);
                    paymentHistoryListView.setVisibility(View.GONE);
                } else {
                    emptyHistoryText.setVisibility(View.GONE);
                    paymentHistoryListView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
