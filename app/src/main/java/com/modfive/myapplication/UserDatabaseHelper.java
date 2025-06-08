package com.modfive.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "userInventory.db";
    private static final int DATABASE_VERSION = 2; // Increment for schema changes

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Inventory table
    private static final String TABLE_INVENTORY = "inventory";
    private static final String COLUMN_ITEM_ID = "id";
    private static final String COLUMN_ITEM_NAME = "name";
    private static final String COLUMN_ITEM_QUANTITY = "quantity";
    private static final String COLUMN_ITEM_USER = "username";  // foreign key (username)

    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create inventory table
        String CREATE_INVENTORY_TABLE = "CREATE TABLE " + TABLE_INVENTORY + "("
                + COLUMN_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ITEM_NAME + " TEXT, "
                + COLUMN_ITEM_QUANTITY + " INTEGER, "
                + COLUMN_ITEM_USER + " TEXT, "
                + "FOREIGN KEY(" + COLUMN_ITEM_USER + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USERNAME + "))";
        db.execSQL(CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // User related methods (existing)
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean userExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);
        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }

    // Inventory CRUD

    public boolean addInventoryItem(String username, String itemName, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if item already exists for user
        Cursor cursor = db.query(TABLE_INVENTORY,
                new String[]{COLUMN_ITEM_ID, COLUMN_ITEM_QUANTITY},
                COLUMN_ITEM_NAME + "=? AND " + COLUMN_ITEM_USER + "=?",
                new String[]{itemName, username},
                null, null, null);

        if (cursor.moveToFirst()) {
            // Update existing item's quantity
            int existingId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_ID));
            int existingQty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY));
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_QUANTITY, existingQty + quantity);
            int rows = db.update(TABLE_INVENTORY, values, COLUMN_ITEM_ID + "=?", new String[]{String.valueOf(existingId)});
            return rows > 0;
        } else {
            cursor.close();
            // Insert new item
            ContentValues values = new ContentValues();
            values.put(COLUMN_ITEM_NAME, itemName);
            values.put(COLUMN_ITEM_QUANTITY, quantity);
            values.put(COLUMN_ITEM_USER, username);
            long result = db.insert(TABLE_INVENTORY, null, values);
            return result != -1;
        }
    }

    public List<InventoryItem> getInventoryItems(String username) {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_INVENTORY,
                new String[]{COLUMN_ITEM_NAME, COLUMN_ITEM_QUANTITY},
                COLUMN_ITEM_USER + "=?",
                new String[]{username},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM_NAME));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ITEM_QUANTITY));
                InventoryItem item = new InventoryItem(name);
                item.setQuantity(quantity);
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public boolean updateInventoryItemQuantity(String username, String itemName, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ITEM_QUANTITY, newQuantity);

        int rows = db.update(TABLE_INVENTORY, values,
                COLUMN_ITEM_NAME + "=? AND " + COLUMN_ITEM_USER + "=?",
                new String[]{itemName, username});
        return rows > 0;
    }

    public boolean deleteInventoryItem(String username, String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_INVENTORY,
                COLUMN_ITEM_NAME + "=? AND " + COLUMN_ITEM_USER + "=?",
                new String[]{itemName, username});
        return rows > 0;
    }
}
