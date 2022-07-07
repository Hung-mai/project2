package com.tuanhung.project2.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tuanhung.project2.adapters.UserAdapter;
import com.tuanhung.project2.databinding.ActivityUsersBinding;
import com.tuanhung.project2.listeners.UserListener;
import com.tuanhung.project2.models.User;
import com.tuanhung.project2.ultilities.Constants;
import com.tuanhung.project2.ultilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;
    List<User> users = new ArrayList<>();
    List<User> users2 = new ArrayList<>();
    UserListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        userListener = this;
        setListener();
        getUsers();
    }

    private void setListener() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.inputName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loading(true);
                users2.clear();
                if(!charSequence.toString().equals("") ) {
                    //do your work here
                    for (int j = 0; j < users.size(); j++) {
                        if(users.get(j).name.contains(charSequence.toString()) || users.get(j).email.contains(charSequence.toString())) {
                            users2.add(users.get(j));
                        }
                    }

                    if(users2.size() > 0) {  // neeus co user thif ms hienj ra
                        UserAdapter userAdapter = new UserAdapter(users2, userListener);
                        binding.usersRecyclerView.setAdapter(userAdapter);
                        binding.usersRecyclerView.setVisibility(View.VISIBLE);
                    }


                } else {
                    if(users.size() > 0) {  // neeus co user thif ms hienj ra
                        UserAdapter userAdapter = new UserAdapter(users, userListener);
                        binding.usersRecyclerView.setAdapter(userAdapter);
                        binding.usersRecyclerView.setVisibility(View.VISIBLE);
                    }
                }

                loading(false);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null) {

                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            // nếu user đc lấy ra trong data này mà trùng vs user hiện đang đăng nhập thì bỏ qua
                            if(currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);  // them vao list
                        }

                        if(users.size() > 0) {  // neeus co user thif ms hienj ra
                            UserAdapter userAdapter = new UserAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(userAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No users available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}