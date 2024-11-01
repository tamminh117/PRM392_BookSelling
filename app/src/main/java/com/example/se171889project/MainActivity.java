package com.example.se171889project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se171889project.admin.ProductManagementActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    EditText editTextUsername, editTextPassword;
    Button logIn;
    TextView signUp;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextUsername = findViewById(R.id.username_input);
        editTextPassword = findViewById(R.id.password_input);
        logIn = findViewById(R.id.login_btn);
        signUp = findViewById(R.id.signup_textview);

        // Set up the sign-up click listener
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();
        });

        // Set up the login click listener
        logIn.setOnClickListener(v -> {
            String username = String.valueOf(editTextUsername.getText());
            String password = String.valueOf(editTextPassword.getText());

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(MainActivity.this, "Enter Username", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(MainActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

//            // Kiểm tra tài khoản admin
//            if (username.equals("admin") && password.equals("123456")) {
//                // Chuyển đến trang admin
//                Intent intent = new Intent(MainActivity.this, ProductManagementActivity.class);
//                startActivity(intent);
//                finish();
//                return; // Kết thúc hàm để không tiếp tục kiểm tra với Firebase
//            }
            String hashedPassword = PasswordUtils.hashPassword(password);

            firebaseAuth.signInWithEmailAndPassword(username, hashedPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                //UserEntity user = new UserEntity(firebaseUser.getUid(), firebaseUser.getEmail(), firebaseUser.getDisplayName());
                                SharedPreferences sharedPref = getSharedPreferences("UserLoginData", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("uid", firebaseUser.getUid());
                                editor.putString("email", firebaseUser.getEmail());
                                editor.apply();
                                Toast.makeText(MainActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                if(username.equals("admin@gmail.com")) {
                                    Intent intent = new Intent(MainActivity.this, ProductManagementActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }
}
