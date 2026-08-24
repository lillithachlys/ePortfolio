package com.example.cs499project;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.*;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {
    private AppDatabase db;
    private RecyclerView weightRecyclerView;
    private WeightAdapter weightAdapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Calendar selectedDate = Calendar.getInstance();
    private static final String DATE_FORMAT = "MM/dd/yyyy";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);

        createDefaultUser();
        setupPasswordVisibility();
    }

    // <--- LOGIN --->
    // creates the default admin user
    private void createDefaultUser() {
        executorService.execute(() -> {
            UserDao userDao = db.userDao();

            // find if the admin user exists
            User defaultUser = userDao.findByLogin(
                    "admin",
                    "ADMINPASSWORD");

            // if the admin user does not exist, create one
            if (defaultUser == null) {
                User user = new User();
                user.userName = "admin"; // default username
                user.userPassword = "ADMINPASSWORD"; // default password

                userDao.insertAll(user);
            }
        });
    }

    // sets the visibility of the password depending on the checkbox
    private void setupPasswordVisibility() {
        EditText passwordText = findViewById(R.id.editPassword);
        CheckBox showPassword = findViewById(R.id.showPassword);

        if (passwordText == null ||
                showPassword == null) {
            return;
        }

        // determine if the password is to be shown
        showPassword.setOnCheckedChangeListener(
                (buttonView,
                        isChecked) -> {
                    int cursorPosition = passwordText.getSelectionStart();

                    // if box is checked, show password
                    if (isChecked) {
                        passwordText.setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        );
                        // if box is NOT checked, do not show password
                    } else {
                        passwordText.setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                        );
                    }

                    // matches password font with username
                    passwordText.setTypeface(
                            android.graphics.Typeface.create(
                                    "sans",
                                    android.graphics.Typeface.NORMAL
                            )
                    );

                    // show the last inputted character of the password
                    if (cursorPosition >= 0 && cursorPosition <= passwordText.length()) {
                        passwordText.setSelection(cursorPosition);
                    }
                }
        );
    }

    // checks to see if the login credentials are available to create
    // a user with once the register button is pressed
    public void RegisterClick(View v){
        EditText usernameText = (EditText) findViewById(R.id.editUserName);
        EditText passwordText = (EditText) findViewById(R.id.editPassword);

        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        executorService.execute(() -> {
            UserDao userDao = db.userDao();
            User existingUser = userDao.findByUsername(username);

            // if the user does not exist, create the user
            if (existingUser == null) {
                User user = new User();
                user.userName = username;
                user.userPassword = password;
                userDao.insertAll(user);

                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "Account Registered!",
                                Toast.LENGTH_SHORT
                        ).show()
                );
                // if the does exist, do not create the user
            }else {
                runOnUiThread(() ->
                        Toast.makeText(
                                MainActivity.this,
                                "User Already Exists!",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }

    // checks if the users login credentials are correct based on the
    // user database upon clicking the login button
    public void LoginClick(View v){
        EditText usernameText = (EditText) findViewById(R.id.editUserName);
        EditText passwordText = (EditText) findViewById(R.id.editPassword);

        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        executorService.execute(() -> {
            UserDao userDao = db.userDao();
            User user = userDao.findByLogin( // find the credentials matching the input
                    username,
                    password);

            runOnUiThread(() -> {
                // if the user exists, login properly
                if (user != null) {
                    getSharedPreferences(
                            "UserPrefs",
                            MODE_PRIVATE
                    ).edit()
                            .putInt("uid", user.uid)
                            .apply();
                    setContentView(R.layout.activity_main);
//                    Toast.makeText(
//                            MainActivity.this,
//                            "ACTIVITY_MAIN LOADED",
//                            Toast.LENGTH_LONG
//                    ).show();
                    setupWeightRecyclerView();
                    loadWeights(user.uid);

                    selectedDate = Calendar.getInstance();
                    setupDateButton();
                    // if the user does not exist, or credentials are incorrect
                    // fail to log in properly
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Invalid Username or Password!",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        });
    }

    // <--- WEIGHT TRACKING --->
    //
    // sets up the RecyclerView for the weight list
    private void setupWeightRecyclerView() {
        weightRecyclerView = findViewById(R.id.weightRecyclerView);

        // DEBUG
        // ANNOUNCE THAT RECYCLERVIEW DID NOT LOAD PROPERLY
//        if (weightRecyclerView == null) {
//            Toast.makeText(
//                    MainActivity.this,
//                    "RecyclerView NOT FOUND!",
//                    Toast.LENGTH_LONG
//            ).show();
//            return;
//        }

        weightRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weightAdapter = new WeightAdapter(
                new ArrayList<>(),
                this::deleteWeight
        );
        weightRecyclerView.setAdapter(weightAdapter);

        // DEBUG
        // CHECK IF THE RECYCLERVIEW LOADS PROPERLY
//        Toast.makeText(
//                MainActivity.this,
//                "RecyclerView Found!",
//                Toast.LENGTH_SHORT
//        ).show();
    }

    // loads the weights database
    private void loadWeights(int uid) {
        executorService.execute(() -> {
            WeightDao weightDao = db.weightDao();
            List<Weight> weights = weightDao.getWeightsForUser(uid);

            runOnUiThread(() -> {
                // DEBUG
                // FIND THE AMOUNT OF WEIGHTS LINKED TO AN ACCOUNT
//                Toast.makeText(
//                        MainActivity.this,
//                        "Weights found: " + weights.size(),
//                        Toast.LENGTH_LONG
//                ).show();

                if (weightAdapter != null) {
                    weightAdapter.updateWeights(weights);
                };
            });
        });
    }

    // sets the date on the date button
    private void setupDateButton() {
        Button dateButton = findViewById(R.id.selectDate);

        if (dateButton == null) {
            return;
        }

        selectedDate = Calendar.getInstance();
        updateDateButton();

        // grabs the date the user inputs
        dateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    MainActivity.this,
                    (view,
                     year,
                     month,
                     day) -> {
                        selectedDate.set(
                                year,
                                month,
                                day
                        );
                        updateDateButton();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    // updates the date of the date button
    private void updateDateButton() {
        Button dateButton = findViewById(R.id.selectDate);

        if (dateButton == null) {
            return;
        }

        // default to current date
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                DATE_FORMAT,
                Locale.getDefault()
        );

        String formattedDate = dateFormat.format(selectedDate.getTime());

        dateButton.setText("Date: " + formattedDate);
    }

    // adds the weight that the user inputs as well as the date
    public void AddWeightClick(View v) {
        EditText weightText = findViewById(R.id.editWeight);
        String weightValue = weightText.getText().toString().trim();

        // if no weight is entered
        if (weightValue.isEmpty()) {
            Toast.makeText(
                    MainActivity.this,
                    "Enter A Weight!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        int uid = getSharedPreferences(
                "UserPrefs",
                MODE_PRIVATE
        ).getInt(
                "uid",
                -1);

        if (uid == -1) {
            Toast.makeText(
                    MainActivity.this,
                    "User Not Found!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // defaults to current date
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                DATE_FORMAT,
                Locale.getDefault()
        );

        String dateValue = dateFormat.format(selectedDate.getTime());

        executorService.execute(() -> {
            WeightDao weightDao = db.weightDao();
            Weight newWeight = new Weight();

            List<Weight> currentWeights = weightDao.getWeightsForUser(uid);

            boolean dateAlreadyExists = false;

            for (Weight weight: currentWeights) {
                if (dateValue.equals(weight.day)) {
                    dateAlreadyExists = true;
                    break;
                }
            }

            // if a date is already used for on a specific date, fail
            if (dateAlreadyExists) {
                runOnUiThread(() -> {
                    Toast.makeText(
                            MainActivity.this,
                            "A weight has already been entered for " + dateValue,
                            Toast.LENGTH_SHORT
                    ).show();
                });
                return;
            }

            newWeight.uid = uid;
            newWeight.day = dateValue;
            newWeight.weightValue = weightValue;

            weightDao.insert(newWeight);

            List<Weight> updatedWeights = weightDao.getWeightsForUser(uid);

            runOnUiThread(() -> {
                if (weightAdapter != null) {
                    weightAdapter.updateWeights(updatedWeights);
                }

                weightText.setText("");
                selectedDate = Calendar.getInstance();

                updateDateButton();

                Toast.makeText(
                        MainActivity.this,
                        "Weight added for Day " + dateValue,
                        Toast.LENGTH_SHORT
                ).show();
            });
        });
    }

    // deletes a specific weight
    private void deleteWeight(Weight weight) {
        new AlertDialog.Builder(
                MainActivity.this
        ).setTitle("Delete Weight")
                .setMessage("Delete the weight recorded on " + weight.day + "?"
                ).setNegativeButton(
                        "Cancel",
                        null
                ).setPositiveButton(
                        "Delete",
                        (dialog,
                         which) -> {
                            executorService.execute(() -> {
                                WeightDao weightDao = db.weightDao();
                                weightDao.deleteById(weight.wid);

                                int uid =
                                        getSharedPreferences(
                                                "UserPrefs",
                                                MODE_PRIVATE
                                        ).getInt(
                                                "uid",
                                                -1
                                        );
                                List<Weight> updatedWeights = weightDao.getWeightsForUser(uid);

                                runOnUiThread(() -> {

                                    if (weightAdapter != null) {
                                        weightAdapter.updateWeights(updatedWeights);
                                    }

                                    Toast.makeText(
                                            MainActivity.this,
                                            "Weight Deleted!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                });
                            });
                        }
                )
                .show();
    }

    // <--- SETTINGS --->
    //
    // brings the user to the settings page upon being clicked
    public void SettingsClick(View v) {
        setContentView(R.layout.settings);
        setupSettings();
    }

    // brings the user to the main page upon being clicked
    public void BackToMainClick(View v) {
        setContentView(R.layout.activity_main);

        int uid = getSharedPreferences(
                "UserPrefs",
                MODE_PRIVATE
        ).getInt(
                "uid",
                -1
        );

        setupWeightRecyclerView();

        if (uid != -1) {
            loadWeights(uid);
        }

        setupDateButton();
    }

    // sets up the settings
    private void setupSettings() {
        Switch smsSwitch = findViewById(R.id.smsNotificationSwitch);

        if (smsSwitch == null) {
            return;
        }

        boolean smsEnabled = getSharedPreferences(
                "UserPrefs",
                MODE_PRIVATE
        ).getBoolean(
                "smsNotifications",
                true
        );

        smsSwitch.setChecked(smsEnabled);
        updateSmsSwitchText(smsSwitch, smsEnabled);
        smsSwitch.setOnCheckedChangeListener(
                (buttonView,
                isChecked) -> {
                    getSharedPreferences(
                            "UserPrefs",
                            MODE_PRIVATE
                    ).edit()
                            .putBoolean(
                                    "smsNotfications",
                                    isChecked
                            ).apply();

                    updateSmsSwitchText(
                            smsSwitch,
                            isChecked
                    );
                }
        );
    }

    // updates the SMS switch upon being enabled or disabled
    private void updateSmsSwitchText(
            Switch smsSwitch,
            boolean enabled) {
        if (enabled) {
            smsSwitch.setText("Enabled");
        } else {
            smsSwitch.setText("Disabled");
        }
    }

    // logs the user out upon being clicked, bringing them back to
    // the login screen
    public void LogoutClick(View v) {
        getSharedPreferences(
                "UserPrefs",
                MODE_PRIVATE
        ).edit()
                .clear()
                .apply();
        setContentView(R.layout.activity_login);
        setupPasswordVisibility();
        Toast.makeText(
                MainActivity.this,
                "Logged Out!",
                Toast.LENGTH_SHORT
        ).show();
    }

    // gives the user the option to proceed with deleting their data
    public void DeleteUserDataClick(View v) {
        new AlertDialog.Builder(
                MainActivity.this
        ).setTitle("Delete All Weight Data")
                .setMessage(
                        "Are you sure you want to delete ALL of your " +
                                "saved weight data?"
                ).setNegativeButton(
                        "Cancel",
                        null
                ).setPositiveButton(
                        "Delete Data",
                        (dialog,
                        which) ->
                            deleteUserData()
                ).show();
    }

    // deletes only the weights of a user
    private void deleteUserData() {
        int uid = getSharedPreferences(
                "UserPrefs",
                MODE_PRIVATE
        ).getInt(
                "uid",
                -1
        );

        if (uid == -1) {
            Toast.makeText(
                    MainActivity.this,
                    "User Not Found!",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        executorService.execute(() -> {
            WeightDao weightDao = db.weightDao();
            weightDao.deleteUserData(uid);
            List<Weight> updatedWeights = weightDao.getWeightsForUser(uid);

            runOnUiThread(() -> {
                if (weightAdapter != null) {
                    weightAdapter.updateWeights(updatedWeights);
                }

                Toast.makeText(
                        MainActivity.this,
                        "All Weight Data Deleted!",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });
    }

    // gives the user the option to proceed with deleting their profile
    public void DeleteUserClick(View v) {
        new AlertDialog.Builder(
                MainActivity.this
        ).setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user? " +
                        "This will also delete all of the saved weights!"
                ).setNegativeButton(
                        "Cancel",
                        null
                ).setPositiveButton(
                        "Delete",
                        (dialog,
                        which) -> deleteCurrentUser()
                ).show();
    }

    // deletes the user and all weights corresponding to them,
    // and brings them back to the login screen
    public void deleteCurrentUser() {
        int uid = getSharedPreferences(
                "UserPrefs",
                MODE_PRIVATE
        ).getInt(
                "uid",
                -1
        );

        if (uid == -1) {
            Toast.makeText(
                    MainActivity.this,
                    "User not found",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        executorService.execute(() -> {
            UserDao userDao = db.userDao();
            userDao.deleteById(uid);

            runOnUiThread(() -> {
                getSharedPreferences(
                        "User Prefs",
                        MODE_PRIVATE
                ).edit()
                        .clear()
                        .apply();

                setContentView(R.layout.activity_login);
                setupPasswordVisibility();
                Toast.makeText(
                        MainActivity.this,
                        "User Deleted!",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        executorService.shutdown();
    }
}
