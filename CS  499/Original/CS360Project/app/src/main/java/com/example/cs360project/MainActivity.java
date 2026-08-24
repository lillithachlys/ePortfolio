package com.example.cs360project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.*;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private AppDatabase db;
    private RecyclerView weightRecyclerView;
    private WeightAdapter weightAdapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        db = AppDatabase.getInstance(this);
        createDefaultUser();
    }

    private void createDefaultUser() {
        executorService.execute(() -> {
            UserDao userDao = db.userDao();
            WeightDao weightDao = db.weightDao();
            User defaultUser = userDao.findByName("admin", "1234");

            if (defaultUser == null) {
                User user = new User();
                user.userName = "admin";
                user.userPassword = "1234";
                userDao.insertAll(user);
                defaultUser = userDao.findByName("admin", "1234");
            }

            if (defaultUser != null) {
                List<Weight> existingWeights = weightDao.getWeightsForUser(defaultUser.uid);

                if (existingWeights.size() < 3) {
                    Weight weight1 = new Weight();
                    weight1.uid = defaultUser.uid;
                    weight1.day = "Day 1";
                    weight1.weightValue = "180";

                    Weight weight2 = new Weight();
                    weight2.uid = defaultUser.uid;
                    weight2.day = "Day 2";
                    weight2.weightValue = "178.5";

                    Weight weight3 = new Weight();
                    weight3.uid = defaultUser.uid;
                    weight3.day = "Day 3";
                    weight3.weightValue = "177";

                    weightDao.insert(weight1);
                    weightDao.insert(weight2);
                    weightDao.insert(weight3);
                }
            }
        });
    }

    private void setupWeightRecyclerView() {
        weightRecyclerView = findViewById(R.id.weightRecyclerView);

        // DEBUG
        if (weightRecyclerView == null) {
            Toast.makeText(this, "RecyclerView NOT FOUND!", Toast.LENGTH_LONG).show();
            return;
        }

        weightRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weightAdapter = new WeightAdapter(new ArrayList<>());
        weightRecyclerView.setAdapter(weightAdapter);

        // DEBUG
        Toast.makeText(this, "RecyclerView Found",Toast.LENGTH_SHORT).show();
    }

    private void loadWeights(int uid) {
        executorService.execute(() -> {
            WeightDao weightDao = db.weightDao();
            List<Weight> weights = weightDao.getWeightsForUser(uid);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Weights found: " + weights.size(), Toast.LENGTH_LONG).show();

                if (weightAdapter != null) {
                    weightAdapter.updateWeights(weights);
                };
            });
        });
    }

    public void RegisterClick(View v){
        EditText usernameText = (EditText) findViewById(R.id.editUserName);
        EditText passwordText = (EditText) findViewById(R.id.editPassword);
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        executorService.execute(() -> {
            UserDao userDao = db.userDao();
            User existingUser = userDao.findByName(username, password);

            if (existingUser == null) {
                User user = new User();
                user.userName = username;
                user.userPassword = password;
                userDao.insertAll(user);

                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Account Registered!", Toast.LENGTH_SHORT).show()
                );
            }else {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "User Already Exists!", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    public void LoginClick(View v){
        EditText usernameText = (EditText) findViewById(R.id.editUserName);
        EditText passwordText = (EditText) findViewById(R.id.editPassword);
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        executorService.execute(() -> {
            UserDao userDao = db.userDao();
            User user = userDao.findByName(username, password);

            runOnUiThread(() -> {
                if (user != null) {
                    getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().putInt("uid", user.uid).apply();
                    setContentView(R.layout.activity_main);
                    Toast.makeText(MainActivity.this, "ACTIVITY_MAIN LOADED", Toast.LENGTH_LONG).show();
                    setupWeightRecyclerView();
                    loadWeights(user.uid);
                } else {
                    Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public void LogoutClick(View v) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
        setContentView(R.layout.activity_login);
        Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
    }

    public void AddWeightClick(View v) {
        EditText weightText = findViewById(R.id.editWeight);
        String weightValue = weightText.getText().toString().trim();

        if (weightValue.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter A Weight", Toast.LENGTH_SHORT).show();
            return;
        }
        int uid = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("uid", -1);

        if (uid == -1) {
            Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }
        executorService.execute(() -> {
            WeightDao weightDao = db.weightDao();
            List<Weight> weights = weightDao.getWeightsForUser(uid);
            int nextDay = weights.size() + 1;
            Weight newWeight = new Weight();
            newWeight.uid = uid;
            newWeight.day = "Day " + nextDay;
            newWeight.weightValue = weightValue;
            weightDao.insert(newWeight);
            List<Weight> updatedWeights = weightDao.getWeightsForUser(uid);
            runOnUiThread(() -> {
                weightAdapter.updateWeights(updatedWeights);
                weightText.setText("");
                Toast.makeText(MainActivity.this, "Weight aded for Day " + nextDay, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        executorService.shutdown();
    }
}
