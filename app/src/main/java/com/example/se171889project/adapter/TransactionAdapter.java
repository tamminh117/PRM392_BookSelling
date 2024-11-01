package com.example.se171889project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.se171889project.R;
import com.example.se171889project.model.Transaction;

import java.util.Date;
import java.util.List;

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private final Context context;
    private final List<Transaction> transactions;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        super(context, R.layout.transaction_item, transactions);
        this.context = context;
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.transaction_item, parent, false);
        }

        Transaction transaction = transactions.get(position);

        TextView totalPriceTextView = convertView.findViewById(R.id.total_price_text);
        TextView timestampTextView = convertView.findViewById(R.id.timestamp_text);
        TextView paymentID = convertView.findViewById(R.id.payment_id);

        totalPriceTextView.setText("Total price: " + transaction.getTotalPrice() + " $");
        timestampTextView.setText("Date: " + new Date(transaction.getTransactionDate()).toString()); // Bạn có thể định dạng ngày theo cách bạn muốn
        paymentID.setText("ID: " + transaction.getTransactionId());

        return convertView;
    }
}
