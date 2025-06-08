package com.modfive.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnItemChangeListener {

    private ArrayList<InventoryItem> itemList = new ArrayList<>();
    private InventoryAdapter adapter;
    private UserDatabaseHelper dbHelper;

    private String username;

    // ActivityResultLauncher to handle SMS permission request result
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        dbHelper = new UserDatabaseHelper(this);

        // Get username passed from MainActivity
        username = getIntent().getStringExtra("username");

        RecyclerView recyclerView = findViewById(R.id.itemRecyclerView);
        findViewById(R.id.addItemButton).setOnClickListener(v -> showAddItemDialog());

        // Add this to handle logout button click:
        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            // Clear any session or user-specific data if needed

            // Return to login screen
            finish();  // Ends InventoryActivity and returns to previous activity (MainActivity)
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(itemList, this);  // Pass "this" as listener
        recyclerView.setAdapter(adapter);

        loadInventoryFromDatabase();

        // Initialize permission request launcher to handle user response
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "SMS permission denied. Notifications disabled.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void loadInventoryFromDatabase() {
        itemList.clear();
        itemList.addAll(dbHelper.getInventoryItems(username));
        adapter.notifyDataSetChanged();
    }

    private void showAddItemDialog() {
        EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setMessage("Enter item name:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        // Add item with quantity 1
                        boolean success = dbHelper.addInventoryItem(username, name, 1);
                        if (success) {
                            loadInventoryFromDatabase();
                        } else {
                            Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Item name can't be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Callback from adapter when quantity hits zero
    @Override
    public void onQuantityZero(int position) {
        InventoryItem item = itemList.get(position);

        if (hasSmsPermission()) {
            // Permission granted: send SMS notification
            sendSmsNotification("1234567890", "Inventory Alert: " + item.getName() + " is out of stock!");   // Phone number set here for sender of SMS
        } else {
            // Permission NOT granted: request permission from user
            requestSmsPermission();
        }
    }

    @Override
    public void onItemDeleted(int position) {
        InventoryItem item = itemList.get(position);
        boolean success = dbHelper.deleteInventoryItem(username, item.getName());
        if (success) {
            itemList.remove(position);
            adapter.notifyItemRemoved(position);
        } else {
            Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
        }
    }

    // Check if SEND_SMS permission is already granted
    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    // Launch the permission request UI
    private void requestSmsPermission() {
        requestPermissionLauncher.launch(Manifest.permission.SEND_SMS);
    }

    // Send the SMS notification
    private void sendSmsNotification(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS notification sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    // Update quantity and persist change to DB
    public void updateQuantity(int position, int newQuantity) {
        InventoryItem item = itemList.get(position);
        item.setQuantity(newQuantity);
        boolean success = dbHelper.updateInventoryItemQuantity(username, item.getName(), newQuantity);
        if (!success) {
            Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyItemChanged(position);
    }
}
