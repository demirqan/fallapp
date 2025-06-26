package com.falapp.fortunetelle;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.falapp.fortunetelle.api.ChatGPTService;
import com.falapp.fortunetelle.models.FortuneReading;
import com.falapp.fortunetelle.models.FortuneTeller;
import com.falapp.fortunetelle.models.User;
import com.falapp.fortunetelle.utils.DatabaseHelper;
import com.falapp.fortunetelle.utils.ImageHelper;
import com.falapp.fortunetelle.utils.NotificationHelper;
import com.falapp.fortunetelle.utils.PermissionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReadingDetailActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private String readingType;
    private int fortuneTellerId;
    private DatabaseHelper dbHelper;
    private User currentUser;
    private FortuneTeller fortuneTeller;
    private ChatGPTService chatGPTService;
    private PermissionManager permissionManager;
    private ImageHelper imageHelper;
    private NotificationHelper notificationHelper;

    private MaterialToolbar toolbar;
    private ImageView ivPhoto;
    private TextView tvFortuneTellerName;
    private EditText etName;
    private TextView tvBirthDate;
    private Spinner spRelationshipStatus;
    private Spinner spJobStatus;
    private Button btnSend;
    private CardView cardPremium;
    private LinearLayout llTopics;

    private String selectedBirthDate;
    private String photoPath;
    private List<String> selectedTopics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_detail);

        // Get data from intent
        readingType = getIntent().getStringExtra("reading_type");
        fortuneTellerId = getIntent().getIntExtra("fortune_teller_id", -1);

        if (readingType == null || fortuneTellerId == -1) {
            Toast.makeText(this, "Geçersiz fal bilgisi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database helper and services
        dbHelper = new DatabaseHelper(this);
        chatGPTService = new ChatGPTService();
        permissionManager = new PermissionManager(this, this);
        imageHelper = new ImageHelper(this);
        notificationHelper = new NotificationHelper(this);

        // Get current user and fortune teller
        currentUser = dbHelper.getUser();
        fortuneTeller = dbHelper.getFortuneTeller(fortuneTellerId);

        if (currentUser == null || fortuneTeller == null) {
            Toast.makeText(this, "Kullanıcı veya falcı bilgisi bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initViews();

        // Set up the toolbar
        setupToolbar();

        // Set up the spinners
        setupSpinners();

        // Set up topic selection
        setupTopicSelection();

        // Set up premium card
        setupPremiumCard();

        // Set fortune teller info
        tvFortuneTellerName.setText(fortuneTeller.getName());

        // Initialize selected topics list
        selectedTopics = new ArrayList<>();

        // Check camera permission and open camera
        checkCameraPermissionAndOpenCamera();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivPhoto = findViewById(R.id.iv_photo);
        tvFortuneTellerName = findViewById(R.id.tv_fortune_teller_name);
        etName = findViewById(R.id.et_name);
        tvBirthDate = findViewById(R.id.tv_birth_date);
        spRelationshipStatus = findViewById(R.id.sp_relationship_status);
        spJobStatus = findViewById(R.id.sp_job_status);
        btnSend = findViewById(R.id.btn_send);
        cardPremium = findViewById(R.id.card_premium);
        llTopics = findViewById(R.id.ll_topics);

        // Set click listeners
        tvBirthDate.setOnClickListener(v -> showDatePickerDialog());
        btnSend.setOnClickListener(v -> validateAndSendReading());
    }

    private void setupToolbar() {
     //   //setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getReadingTypeTitle(readingType));

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinners() {
        // Set up relationship status spinner
        ArrayAdapter<CharSequence> relationshipAdapter = ArrayAdapter.createFromResource(this,
                R.array.relationship_status_array, android.R.layout.simple_spinner_item);
        relationshipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRelationshipStatus.setAdapter(relationshipAdapter);

        // Set up job status spinner
        ArrayAdapter<CharSequence> jobAdapter = ArrayAdapter.createFromResource(this,
                R.array.job_status_array, android.R.layout.simple_spinner_item);
        jobAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spJobStatus.setAdapter(jobAdapter);
    }

    private void setupTopicSelection() {
        // Create topic checkboxes
        String[] topics = {"aşk", "para", "sağlık", "arkadaş", "aile", "kariyer"};
        String[] emojis = {"❤️", "💰", "🏥", "👥", "👨‍👩‍👧‍👦", "💼"};

        for (int i = 0; i < topics.length; i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(emojis[i] + " " + topics[i]);
            checkBox.setTag(topics[i]);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String topic = (String) buttonView.getTag();
                if (isChecked) {
                    if (selectedTopics.size() < 3) {
                        selectedTopics.add(topic);
                    } else {
                        buttonView.setChecked(false);
                        Toast.makeText(ReadingDetailActivity.this,
                                "En fazla 3 konu seçebilirsiniz!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    selectedTopics.remove(topic);
                }
            });

            llTopics.addView(checkBox);
        }
    }

    private void setupPremiumCard() {
        cardPremium.setOnClickListener(v -> {
            startActivity(new Intent(ReadingDetailActivity.this, PremiumActivity.class));
        });
    }

    private void checkCameraPermissionAndOpenCamera() {
        permissionManager.requestCameraPermission(new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionGranted() {
                openCamera();
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(ReadingDetailActivity.this,
                        "Kamera izni olmadan fal bakılamaz!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Kamera uygulaması bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // Resmi yeniden boyutlandır ve döndür (gerekirse)
            imageBitmap = imageHelper.resizeBitmap(imageBitmap, 800, 800);

            ivPhoto.setImageBitmap(imageBitmap);

            // Save the image to a file
            photoPath = imageHelper.saveBitmapToFile(imageBitmap);
        } else if (resultCode != RESULT_OK) {
            // If the camera was cancelled, go back
            finish();
        }
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = null;

        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    selectedBirthDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    tvBirthDate.setText(selectedBirthDate);
                }, year, month, day);

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void validateAndSendReading() {
        // Validate name
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Lütfen isim girin");
            etName.requestFocus();
            return;
        }

        // Validate birth date
        if (selectedBirthDate == null || selectedBirthDate.isEmpty()) {
            Toast.makeText(this, "Lütfen doğum tarihi seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate topics
        if (selectedTopics.size() == 0) {
            Toast.makeText(this, "Lütfen en az bir konu seçin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate photo
        if (photoPath == null || photoPath.isEmpty()) {
            Toast.makeText(this, "Fotoğraf çekilemedi, lütfen tekrar deneyin", Toast.LENGTH_SHORT).show();
            checkCameraPermissionAndOpenCamera();
            return;
        }

        // Get relationship and job status
        String relationshipStatus = spRelationshipStatus.getSelectedItem().toString();
        String jobStatus = spJobStatus.getSelectedItem().toString();

        // Create topics string (comma-separated)
        StringBuilder topicsBuilder = new StringBuilder();
        for (int i = 0; i < selectedTopics.size(); i++) {
            if (i > 0) {
                topicsBuilder.append(",");
            }
            topicsBuilder.append(selectedTopics.get(i));
        }
        String topics = topicsBuilder.toString();

        // Deduct coins from user
        if (!currentUser.removeCoins(fortuneTeller.getPrice())) {
            Toast.makeText(this, "Yeterli altınınız yok!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CoinPurchaseActivity.class));
            return;
        }

        // Update user coins in database
        dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());

        // Create a new reading
        FortuneReading reading = new FortuneReading(
                readingType,
                fortuneTellerId,
                currentUser.getId(),
                topics,
                name,
                selectedBirthDate,
                relationshipStatus,
                jobStatus,
                photoPath
        );

        // Save reading to database
        long readingId = dbHelper.addReading(reading);

        if (readingId == -1) {
            Toast.makeText(this, "Fal kaydedilemedi, lütfen tekrar deneyin", Toast.LENGTH_SHORT).show();
            // Refund coins to user
            currentUser.addCoins(fortuneTeller.getPrice());
            dbHelper.updateUserCoins(currentUser.getId(), currentUser.getCoins());
            return;
        }

        // Start background service to get result from ChatGPT
        Intent serviceIntent = new Intent(this, ReadingProcessService.class);
        serviceIntent.putExtra("reading_id", (int) readingId);
        startService(serviceIntent);

        // Show notification when ready (handled by service)
        notificationHelper.createNotificationChannels();

        // Show success dialog
        showSuccessDialog();
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Fal Gönderildi")
                .setMessage("Falınız başarıyla gönderildi. Sonuç 3-5 dakika içinde Fallarım sayfasında görünecektir.")
                .setPositiveButton("Tamam", (dialog, which) -> {
                    // Go to My Readings activity
                    Intent intent = new Intent(ReadingDetailActivity.this, MyReadingsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String getReadingTypeTitle(String type) {
        switch (type) {
            case "coffee":
                return "Kahve Falı";
            case "tarot":
                return "Tarot Falı";
            case "palm":
                return "El Falı";
            case "face":
                return "Yüz Falı";
            default:
                return "Fal";
        }
    }
}