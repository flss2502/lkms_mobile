package com.example.lkms.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Import tất cả các model cần thiết
import com.example.lkms.data.models.Booking;
import com.example.lkms.data.models.Equipment;
import com.example.lkms.data.models.Experiment;
import com.example.lkms.data.models.LabNote;
import com.example.lkms.data.models.Protocol;
import com.example.lkms.data.models.InventoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lkms.db";
    // Phiên bản 7 (Thêm Bảng Đặt chỗ)
    private static final int DATABASE_VERSION = 7;

    // ===== BẢNG USERS =====
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "uid";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_ROLE = "role";

    // ===== BẢNG EXPERIMENTS =====
    public static final String TABLE_EXPERIMENTS = "experiments";
    public static final String COLUMN_EXP_ID = "_id";
    public static final String COLUMN_EXP_NAME = "name";
    public static final String COLUMN_EXP_STATUS = "status";
    public static final String COLUMN_EXP_DUE_DATE = "due_date";
    public static final String COLUMN_EXP_CREATED_BY = "created_by";

    // ===== BẢNG LAB NOTES =====
    public static final String TABLE_LAB_NOTES = "lab_notes";
    public static final String COLUMN_NOTE_ID = "note_id";
    public static final String COLUMN_NOTE_EXP_ID = "experiment_id";
    public static final String COLUMN_NOTE_HTML = "html_content";
    public static final String COLUMN_NOTE_TIMESTAMP = "timestamp";

    // ===== BẢNG PROTOCOLS & SOPS =====
    public static final String TABLE_PROTOCOLS = "protocols";
    public static final String COLUMN_PROTO_ID = "proto_id";
    public static final String COLUMN_PROTO_TITLE = "title";
    public static final String COLUMN_PROTO_TYPE = "type";
    public static final String COLUMN_PROTO_VERSION = "version";
    public static final String COLUMN_PROTO_AUTHOR_ID = "author_id";
    public static final String COLUMN_PROTO_CONTENT_TYPE = "content_type";
    public static final String COLUMN_PROTO_CONTENT_DATA = "content_data";
    public static final String COLUMN_PROTO_CONTENT_MIMETYPE = "content_mimetype";

    // ===== BẢNG INVENTORY =====
    public static final String TABLE_INVENTORY = "inventory";
    public static final String COLUMN_INV_ID = "inv_id";
    public static final String COLUMN_INV_NAME = "name";
    public static final String COLUMN_INV_DESCRIPTION = "description";
    public static final String COLUMN_INV_QUANTITY = "quantity";
    public static final String COLUMN_INV_UNIT = "unit";
    public static final String COLUMN_INV_LOCATION = "location";
    public static final String COLUMN_INV_CATEGORY = "category";

    // ===== BẢNG MỚI: EQUIPMENT (Thiết bị) =====
    public static final String TABLE_EQUIPMENT = "equipment";
    public static final String COLUMN_EQUIP_ID = "equip_id";
    public static final String COLUMN_EQUIP_NAME = "name";
    public static final String COLUMN_EQUIP_LOCATION = "location";
    public static final String COLUMN_EQUIP_CATEGORY = "category";

    // ===== BẢNG MỚI: BOOKINGS (Đặt chỗ) =====
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String COLUMN_BOOK_ID = "book_id";
    public static final String COLUMN_BOOK_EQUIP_ID_FK = "equipment_id";
    public static final String COLUMN_BOOK_USER_ID_FK = "user_id";
    public static final String COLUMN_BOOK_START_TIME = "start_time";
    public static final String COLUMN_BOOK_END_TIME = "end_time";
    public static final String COLUMN_BOOK_NOTES = "notes";

    // ===== SQL CREATE TABLE =====
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " TEXT PRIMARY KEY, " +
                    COLUMN_USER_NAME + " TEXT, " +
                    COLUMN_USER_EMAIL + " TEXT, " +
                    COLUMN_USER_ROLE + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_EXPERIMENTS =
            "CREATE TABLE " + TABLE_EXPERIMENTS + " (" +
                    COLUMN_EXP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EXP_NAME + " TEXT, " +
                    COLUMN_EXP_STATUS + " TEXT, " +
                    COLUMN_EXP_DUE_DATE + " TEXT, " +
                    COLUMN_EXP_CREATED_BY + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_LAB_NOTES =
            "CREATE TABLE " + TABLE_LAB_NOTES + " (" +
                    COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOTE_EXP_ID + " INTEGER, " +
                    COLUMN_NOTE_HTML + " TEXT, " +
                    COLUMN_NOTE_TIMESTAMP + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_PROTOCOLS =
            "CREATE TABLE " + TABLE_PROTOCOLS + " (" +
                    COLUMN_PROTO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PROTO_TITLE + " TEXT, " +
                    COLUMN_PROTO_TYPE + " TEXT, " +
                    COLUMN_PROTO_VERSION + " INTEGER DEFAULT 1, " +
                    COLUMN_PROTO_AUTHOR_ID + " TEXT, " +
                    COLUMN_PROTO_CONTENT_TYPE + " TEXT DEFAULT 'HTML', " +
                    COLUMN_PROTO_CONTENT_DATA + " TEXT, " +
                    COLUMN_PROTO_CONTENT_MIMETYPE + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_INVENTORY =
            "CREATE TABLE " + TABLE_INVENTORY + " (" +
                    COLUMN_INV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_INV_NAME + " TEXT NOT NULL, " +
                    COLUMN_INV_DESCRIPTION + " TEXT, " +
                    COLUMN_INV_QUANTITY + " REAL DEFAULT 0, " +
                    COLUMN_INV_UNIT + " TEXT, " +
                    COLUMN_INV_LOCATION + " TEXT, " +
                    COLUMN_INV_CATEGORY + " TEXT" +
                    ");";

    // ===== SQL CREATE BẢNG MỚI =====
    private static final String CREATE_TABLE_EQUIPMENT =
            "CREATE TABLE " + TABLE_EQUIPMENT + " (" +
                    COLUMN_EQUIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_EQUIP_NAME + " TEXT NOT NULL, " +
                    COLUMN_EQUIP_LOCATION + " TEXT, " +
                    COLUMN_EQUIP_CATEGORY + " TEXT" +
                    ");";

    private static final String CREATE_TABLE_BOOKINGS =
            "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                    COLUMN_BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_BOOK_EQUIP_ID_FK + " INTEGER, " +
                    COLUMN_BOOK_USER_ID_FK + " TEXT, " +
                    COLUMN_BOOK_START_TIME + " TEXT NOT NULL, " +
                    COLUMN_BOOK_END_TIME + " TEXT NOT NULL, " +
                    COLUMN_BOOK_NOTES + " TEXT" +
                    ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_EXPERIMENTS);
        db.execSQL(CREATE_TABLE_LAB_NOTES);
        db.execSQL(CREATE_TABLE_PROTOCOLS);
        db.execSQL(CREATE_TABLE_INVENTORY);
        db.execSQL(CREATE_TABLE_EQUIPMENT);
        db.execSQL(CREATE_TABLE_BOOKINGS);

        Log.i("DatabaseHelper", "New database created (v" + DATABASE_VERSION + "). Seeding data...");
        seedDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPERIMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAB_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROTOCOLS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);

        onCreate(db); // Tạo lại tất cả
    }

    // ===== USER FUNCTIONS (Đã xóa db.close()) =====
    public void insertOrUpdateUser(String uid, String username, String email, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, uid);
        values.put(COLUMN_USER_NAME, username);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_ROLE, role);
        db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getUserRole(String uid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_USER_ROLE + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_ID + "=?", new String[]{uid});
        String role = null;
        if (cursor.moveToFirst()) {
            role = cursor.getString(0);
        }
        cursor.close();
        return role;
    }

    public void deleteUserData(String uid) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();

            List<Integer> experimentIds = new ArrayList<>();
            Cursor cursor = db.rawQuery(
                    "SELECT " + COLUMN_EXP_ID + " FROM " + TABLE_EXPERIMENTS + " WHERE " + COLUMN_EXP_CREATED_BY + "=?",
                    new String[]{uid}
            );
            if (cursor.moveToFirst()) {
                do {
                    experimentIds.add(cursor.getInt(0));
                } while (cursor.moveToNext());
            }
            cursor.close();

            for (int expId : experimentIds) {
                db.delete(TABLE_LAB_NOTES,
                        COLUMN_NOTE_EXP_ID + " = ?",
                        new String[]{String.valueOf(expId)});
            }

            db.delete(TABLE_EXPERIMENTS,
                    COLUMN_EXP_CREATED_BY + " = ?",
                    new String[]{uid});

            db.delete(TABLE_PROTOCOLS,
                    COLUMN_PROTO_AUTHOR_ID + " = ?",
                    new String[]{uid});

            db.delete(TABLE_BOOKINGS,
                    COLUMN_BOOK_USER_ID_FK + " = ?",
                    new String[]{uid});

            db.delete(TABLE_USERS,
                    COLUMN_USER_ID + " = ?",
                    new String[]{uid});

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi xóa dữ liệu người dùng", e);
        } finally {
            db.endTransaction();
        }
    }

    // ===== EXPERIMENT FUNCTIONS =====
    public long addExperiment(Experiment experiment, String createdBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXP_NAME, experiment.getName());
        values.put(COLUMN_EXP_STATUS, experiment.getStatus());
        values.put(COLUMN_EXP_DUE_DATE, experiment.getDueDate());
        values.put(COLUMN_EXP_CREATED_BY, createdBy);
        long newRowId = db.insert(TABLE_EXPERIMENTS, null, values);
        return newRowId;
    }

    public List<Experiment> getExperimentsByUser(String uid) {
        List<Experiment> experiments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_EXPERIMENTS + " WHERE " + COLUMN_EXP_CREATED_BY + "=? ORDER BY " + COLUMN_EXP_ID + " DESC",
                new String[]{uid}
        );

        if (cursor.moveToFirst()) {
            do {
                experiments.add(new Experiment(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EXP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXP_DUE_DATE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return experiments;
    }

    public int updateExperiment(Experiment experiment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXP_NAME, experiment.getName());
        values.put(COLUMN_EXP_STATUS, experiment.getStatus());
        values.put(COLUMN_EXP_DUE_DATE, experiment.getDueDate());

        int rowsAffected = db.update(TABLE_EXPERIMENTS, values,
                COLUMN_EXP_ID + " = ?",
                new String[]{String.valueOf(experiment.getId())});
        return rowsAffected;
    }

    public void deleteExperiment(int experimentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPERIMENTS,
                COLUMN_EXP_ID + " = ?",
                new String[]{String.valueOf(experimentId)});

        db.delete(TABLE_LAB_NOTES,
                COLUMN_NOTE_EXP_ID + " = ?",
                new String[]{String.valueOf(experimentId)});
    }

    // ===== LAB NOTE FUNCTIONS =====
    public void saveLabNote(int experimentId, String html, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_EXP_ID, experimentId);
        values.put(COLUMN_NOTE_HTML, html);
        values.put(COLUMN_NOTE_TIMESTAMP, timestamp);
        db.insert(TABLE_LAB_NOTES, null, values);
    }

    public List<LabNote> getNotesForExperiment(int experimentId) {
        List<LabNote> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_LAB_NOTES +
                        " WHERE " + COLUMN_NOTE_EXP_ID + "=? ORDER BY " + COLUMN_NOTE_TIMESTAMP + " ASC",
                new String[]{String.valueOf(experimentId)}
        );
        if (cursor.moveToFirst()) {
            do {
                notes.add(new LabNote(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_EXP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_HTML)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_TIMESTAMP))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }

    // ===== PROTOCOL & SOP FUNCTIONS =====
    public void addProtocol(Protocol protocol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROTO_TITLE, protocol.getTitle());
        values.put(COLUMN_PROTO_TYPE, protocol.getType());
        values.put(COLUMN_PROTO_VERSION, protocol.getVersion());
        values.put(COLUMN_PROTO_AUTHOR_ID, protocol.getAuthorId());
        values.put(COLUMN_PROTO_CONTENT_TYPE, protocol.getContentType());
        values.put(COLUMN_PROTO_CONTENT_DATA, protocol.getContentData());
        values.put(COLUMN_PROTO_CONTENT_MIMETYPE, protocol.getContentMimeType());
        db.insert(TABLE_PROTOCOLS, null, values);
    }

    public int updateProtocol(Protocol protocol) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROTO_TITLE, protocol.getTitle());
        values.put(COLUMN_PROTO_TYPE, protocol.getType());
        values.put(COLUMN_PROTO_VERSION, protocol.getVersion());
        values.put(COLUMN_PROTO_AUTHOR_ID, protocol.getAuthorId());
        values.put(COLUMN_PROTO_CONTENT_TYPE, protocol.getContentType());
        values.put(COLUMN_PROTO_CONTENT_DATA, protocol.getContentData());
        values.put(COLUMN_PROTO_CONTENT_MIMETYPE, protocol.getContentMimeType());

        int rowsAffected = db.update(TABLE_PROTOCOLS, values,
                COLUMN_PROTO_ID + " = ?",
                new String[]{String.valueOf(protocol.getProto_id())});
        return rowsAffected;
    }

    public void deleteProtocol(int protocolId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROTOCOLS,
                COLUMN_PROTO_ID + " = ?",
                new String[]{String.valueOf(protocolId)});
    }

    public List<Protocol> getProtocolsByType(String type) {
        List<Protocol> protocols = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_PROTOCOLS + " WHERE " + COLUMN_PROTO_TYPE + " = ? ORDER BY " + COLUMN_PROTO_TITLE + " ASC",
                new String[]{type}
        );

        if (cursor.moveToFirst()) {
            do {
                protocols.add(new Protocol(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROTO_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROTO_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROTO_TYPE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROTO_VERSION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROTO_AUTHOR_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROTO_CONTENT_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROTO_CONTENT_DATA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROTO_CONTENT_MIMETYPE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return protocols;
    }

    // ===== INVENTORY FUNCTIONS =====
    public void addInventoryItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INV_NAME, item.getName());
        values.put(COLUMN_INV_DESCRIPTION, item.getDescription());
        values.put(COLUMN_INV_QUANTITY, item.getQuantity());
        values.put(COLUMN_INV_UNIT, item.getUnit());
        values.put(COLUMN_INV_LOCATION, item.getLocation());
        values.put(COLUMN_INV_CATEGORY, item.getCategory());
        db.insert(TABLE_INVENTORY, null, values);
    }

    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_INVENTORY + " ORDER BY " + COLUMN_INV_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                items.add(new InventoryItem(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INV_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INV_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INV_DESCRIPTION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_INV_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INV_UNIT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INV_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INV_CATEGORY))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

    public int updateInventoryItemQuantity(int itemId, double newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INV_QUANTITY, newQuantity);
        int rowsAffected = db.update(TABLE_INVENTORY, values, COLUMN_INV_ID + " = ?", new String[]{String.valueOf(itemId)});
        return rowsAffected;
    }

    public int updateInventoryItem(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_INV_NAME, item.getName());
        values.put(COLUMN_INV_DESCRIPTION, item.getDescription());
        values.put(COLUMN_INV_QUANTITY, item.getQuantity());
        values.put(COLUMN_INV_UNIT, item.getUnit());
        values.put(COLUMN_INV_LOCATION, item.getLocation());
        values.put(COLUMN_INV_CATEGORY, item.getCategory());
        int rowsAffected = db.update(TABLE_INVENTORY, values, COLUMN_INV_ID + " = ?", new String[]{String.valueOf(item.getInv_id())});
        return rowsAffected;
    }

    public void deleteInventoryItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_INVENTORY, COLUMN_INV_ID + " = ?", new String[]{String.valueOf(itemId)});
    }

    // ===== BOOKING & EQUIPMENT FUNCTIONS =====

    public List<Equipment> getAllEquipment() {
        List<Equipment> equipmentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EQUIPMENT + " ORDER BY " + COLUMN_EQUIP_NAME + " ASC", null);

        if (cursor.moveToFirst()) {
            do {
                equipmentList.add(new Equipment(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_EQUIP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIP_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIP_CATEGORY))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return equipmentList;
    }

    public long addBooking(Booking booking) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_EQUIP_ID_FK, booking.getEquipmentId());
        values.put(COLUMN_BOOK_USER_ID_FK, booking.getUserId());
        values.put(COLUMN_BOOK_START_TIME, booking.getStartTime());
        values.put(COLUMN_BOOK_END_TIME, booking.getEndTime());
        values.put(COLUMN_BOOK_NOTES, booking.getNotes());
        long newRowId = db.insert(TABLE_BOOKINGS, null, values);
        return newRowId;
    }

    /**
     * Lấy các lịch đặt sắp tới của một người dùng (cho Dashboard)
     */
    public List<Booking> getUpcomingBookingsForUser(String uid) {
        List<Booking> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

        // Truy vấn: JOIN Bookings, Equipment, và Users
        String sql = "SELECT b.*, e." + COLUMN_EQUIP_NAME + ", u." + COLUMN_USER_NAME + " " +
                "FROM " + TABLE_BOOKINGS + " b " +
                "INNER JOIN " + TABLE_EQUIPMENT + " e ON b." + COLUMN_BOOK_EQUIP_ID_FK + " = e." + COLUMN_EQUIP_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON b." + COLUMN_BOOK_USER_ID_FK + " = u." + COLUMN_USER_ID + " " +
                "WHERE b." + COLUMN_BOOK_USER_ID_FK + " = ? AND b." + COLUMN_BOOK_START_TIME + " > ? " +
                "ORDER BY b." + COLUMN_BOOK_START_TIME + " ASC LIMIT 5";

        Cursor cursor = db.rawQuery(sql, new String[]{uid, now});

        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_EQUIP_ID_FK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_USER_ID_FK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_START_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_END_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_NOTES))
                );

                booking.setEquipmentName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIP_NAME)));

                String userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
                booking.setUserName(userName != null ? userName : "N/A");

                bookingList.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookingList;
    }

    public List<Booking> getAllUpcomingBookings() {
        List<Booking> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Lấy thời gian hiện tại
        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

        // Truy vấn: Giống như hàm trước, nhưng XÓA điều kiện "b.user_id = ?"
        String sql = "SELECT b.*, e." + COLUMN_EQUIP_NAME + ", u." + COLUMN_USER_NAME + " " +
                "FROM " + TABLE_BOOKINGS + " b " +
                "INNER JOIN " + TABLE_EQUIPMENT + " e ON b." + COLUMN_BOOK_EQUIP_ID_FK + " = e." + COLUMN_EQUIP_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON b." + COLUMN_BOOK_USER_ID_FK + " = u." + COLUMN_USER_ID + " " +
                "WHERE b." + COLUMN_BOOK_START_TIME + " > ? " + // Chỉ lấy lịch sắp tới
                "ORDER BY b." + COLUMN_BOOK_START_TIME + " ASC LIMIT 5"; // Sắp xếp (gần nhất trước)

        Cursor cursor = db.rawQuery(sql, new String[]{now}); // Chỉ cần 1 tham số

        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_EQUIP_ID_FK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_USER_ID_FK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_START_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_END_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_NOTES))
                );

                booking.setEquipmentName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIP_NAME)));

                String userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
                booking.setUserName(userName != null ? userName : "N/A");

                bookingList.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookingList;
    }

    /**
     * Lấy TẤT CẢ các lịch đặt (của mọi người) cho một ngày cụ thể
     * @param date Ngày được chọn
     * @return Danh sách các lịch đặt đã JOIN
     */
    public List<Booking> getBookingsByDate(Date date) {
        List<Booking> bookingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Định dạng ngày thành "YYYY-MM-DD" để dùng với LIKE
        String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);

        // Truy vấn: JOIN Bookings, Equipment, Users. Lọc theo ngày (LIKE "YYYY-MM-DD%")
        String sql = "SELECT b.*, e." + COLUMN_EQUIP_NAME + ", u." + COLUMN_USER_NAME + " " +
                "FROM " + TABLE_BOOKINGS + " b " +
                "INNER JOIN " + TABLE_EQUIPMENT + " e ON b." + COLUMN_BOOK_EQUIP_ID_FK + " = e." + COLUMN_EQUIP_ID + " " +
                "LEFT JOIN " + TABLE_USERS + " u ON b." + COLUMN_BOOK_USER_ID_FK + " = u." + COLUMN_USER_ID + " " +
                "WHERE b." + COLUMN_BOOK_START_TIME + " LIKE ? " +
                "ORDER BY b." + COLUMN_BOOK_START_TIME + " ASC";

        Cursor cursor = db.rawQuery(sql, new String[]{dateString + "%"});

        if (cursor.moveToFirst()) {
            do {
                Booking booking = new Booking(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BOOK_EQUIP_ID_FK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_USER_ID_FK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_START_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_END_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_NOTES))
                );

                booking.setEquipmentName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EQUIP_NAME)));

                String userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
                booking.setUserName(userName != null ? userName : "N/A"); // (Xử lý nếu user bị null)

                bookingList.add(booking);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookingList;
    }

    public void deleteBooking(int bookingId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKINGS,
                COLUMN_BOOK_ID + " = ?",
                new String[]{String.valueOf(bookingId)});
    }

    // ===== HÀM CHÈN DỮ LIỆU MẪU =====
    private void seedDatabase(SQLiteDatabase db) {
        // Chèn dữ liệu mẫu cho Inventory
        ContentValues invValues = new ContentValues();
        invValues.put(COLUMN_INV_NAME, "Ethanol 90%");
        invValues.put(COLUMN_INV_DESCRIPTION, "Cồn 90 độ, dùng để khử trùng");
        invValues.put(COLUMN_INV_QUANTITY, 500);
        invValues.put(COLUMN_INV_UNIT, "mL");
        invValues.put(COLUMN_INV_LOCATION, "Tủ A1, Kệ 2");
        invValues.put(COLUMN_INV_CATEGORY, "Hóa chất");
        db.insert(TABLE_INVENTORY, null, invValues);

        invValues.clear();
        invValues.put(COLUMN_INV_NAME, "Nước cất (ddH2O)");
        invValues.put(COLUMN_INV_DESCRIPTION, "Nước cất 2 lần, khử ion");
        invValues.put(COLUMN_INV_QUANTITY, 1000);
        invValues.put(COLUMN_INV_UNIT, "mL");
        invValues.put(COLUMN_INV_LOCATION, "Tủ A1, Kệ 1");
        invValues.put(COLUMN_INV_CATEGORY, "Hóa chất");
        db.insert(TABLE_INVENTORY, null, invValues);

        invValues.clear();
        invValues.put(COLUMN_INV_NAME, "Pipet 1000uL");
        invValues.put(COLUMN_INV_DESCRIPTION, "Pipetman, P1000");
        invValues.put(COLUMN_INV_QUANTITY, 5); // <-- Số lượng thấp để test Cảnh báo
        invValues.put(COLUMN_INV_UNIT, "cái");
        invValues.put(COLUMN_INV_LOCATION, "Hộc tủ B1");
        invValues.put(COLUMN_INV_CATEGORY, "Dụng cụ");
        db.insert(TABLE_INVENTORY, null, invValues);

        invValues.clear();
        invValues.put(COLUMN_INV_NAME, "Ống nghiệm 1.5mL");
        invValues.put(COLUMN_INV_DESCRIPTION, "Eppendorf");
        invValues.put(COLUMN_INV_QUANTITY, 1); // <-- Số lượng thấp để test Cảnh báo
        invValues.put(COLUMN_INV_UNIT, "hộp (500 cái)");
        invValues.put(COLUMN_INV_LOCATION, "Kệ C2");
        invValues.put(COLUMN_INV_CATEGORY, "Dụng cụ");
        db.insert(TABLE_INVENTORY, null, invValues);

        // Chèn dữ liệu mẫu cho Protocols & SOPs
        ContentValues protoValues = new ContentValues();
        protoValues.put(COLUMN_PROTO_TITLE, "SOP: Vận hành máy ly tâm");
        protoValues.put(COLUMN_PROTO_TYPE, "SOP");
        protoValues.put(COLUMN_PROTO_CONTENT_TYPE, "HTML");
        protoValues.put(COLUMN_PROTO_CONTENT_DATA, "<h1>Vận hành Máy ly tâm</h1><p>1. Cân bằng mẫu...</p><p>2. Chọn tốc độ...</p>");
        protoValues.put(COLUMN_PROTO_VERSION, 1);
        protoValues.put(COLUMN_PROTO_AUTHOR_ID, "lab_admin");
        db.insert(TABLE_PROTOCOLS, null, protoValues);

        protoValues.clear();
        protoValues.put(COLUMN_PROTO_TITLE, "Protocol: Chiết tách DNA (File PDF)");
        protoValues.put(COLUMN_PROTO_TYPE, "PROTOCOL");
        protoValues.put(COLUMN_PROTO_CONTENT_TYPE, "FILE");
        protoValues.put(COLUMN_PROTO_CONTENT_DATA, "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf");
        protoValues.put(COLUMN_PROTO_CONTENT_MIMETYPE, "application/pdf");
        protoValues.put(COLUMN_PROTO_VERSION, 1);
        protoValues.put(COLUMN_PROTO_AUTHOR_ID, "lab_admin");
        db.insert(TABLE_PROTOCOLS, null, protoValues);

        protoValues.clear();
        protoValues.put(COLUMN_PROTO_TITLE, "Protocol: Quy trình An toàn (File Word)");
        protoValues.put(COLUMN_PROTO_TYPE, "SOP");
        protoValues.put(COLUMN_PROTO_CONTENT_TYPE, "FILE");
        protoValues.put(COLUMN_PROTO_CONTENT_DATA, "https://file-examples.com/storage/fe23578d65666b61b998214/2017/02/file-sample_100kB.doc");
        protoValues.put(COLUMN_PROTO_CONTENT_MIMETYPE, "application/msword");
        protoValues.put(COLUMN_PROTO_VERSION, 1);
        protoValues.put(COLUMN_PROTO_AUTHOR_ID, "lab_admin");
        db.insert(TABLE_PROTOCOLS, null, protoValues);

        // BỔ SUNG: Seed Equipment
        ContentValues equipValues = new ContentValues();

        equipValues.put(COLUMN_EQUIP_NAME, "Máy ly tâm Eppendorf 5810R");
        equipValues.put(COLUMN_EQUIP_LOCATION, "Phòng 101, Tủ 1");
        equipValues.put(COLUMN_EQUIP_CATEGORY, "Máy ly tâm");
        db.insert(TABLE_EQUIPMENT, null, equipValues);

        equipValues.clear();
        equipValues.put(COLUMN_EQUIP_NAME, "Máy PCR (Gradient)");
        equipValues.put(COLUMN_EQUIP_LOCATION, "Phòng 102 (PCR)");
        equipValues.put(COLUMN_EQUIP_CATEGORY, "Máy PCR");
        db.insert(TABLE_EQUIPMENT, null, equipValues);

        equipValues.clear();
        equipValues.put(COLUMN_EQUIP_NAME, "Máy Quang phổ NanoDrop");
        equipValues.put(COLUMN_EQUIP_LOCATION, "Phòng 101, Bàn 3");
        equipValues.put(COLUMN_EQUIP_CATEGORY, "Máy quang phổ");
        db.insert(TABLE_EQUIPMENT, null, equipValues);

        equipValues.clear();
        equipValues.put(COLUMN_EQUIP_NAME, "Tủ an toàn sinh học Cấp 2");
        equipValues.put(COLUMN_EQUIP_LOCATION, "Phòng 103 (Nuôi cấy)");
        equipValues.put(COLUMN_EQUIP_CATEGORY, "Tủ cấy");
        db.insert(TABLE_EQUIPMENT, null, equipValues);

        ContentValues bookValues = new ContentValues();

        // Lịch 1 (Cho ngày mai)
        bookValues.put(COLUMN_BOOK_EQUIP_ID_FK, 1); // ID 1 = Máy ly tâm
        bookValues.put(COLUMN_BOOK_USER_ID_FK, "user_id_A"); // Một UID giả
        bookValues.put(COLUMN_BOOK_START_TIME, "2025-11-10T10:00:00");
        bookValues.put(COLUMN_BOOK_END_TIME, "2025-11-10T11:00:00");
        bookValues.put(COLUMN_BOOK_NOTES, "Chạy mẫu DNA");
        db.insert(TABLE_BOOKINGS, null, bookValues);

        bookValues.clear();

        // Lịch 2 (Cho ngày mai)
        bookValues.put(COLUMN_BOOK_EQUIP_ID_FK, 2); // ID 2 = Máy PCR
        bookValues.put(COLUMN_BOOK_USER_ID_FK, "user_id_B"); // Một UID giả khác
        bookValues.put(COLUMN_BOOK_START_TIME, "2025-11-10T14:00:00");
        bookValues.put(COLUMN_BOOK_END_TIME, "2025-11-10T17:00:00");
        bookValues.put(COLUMN_BOOK_NOTES, "Chạy 96 giếng");
        db.insert(TABLE_BOOKINGS, null, bookValues);

        bookValues.clear();

        // Lịch 3 (Cho ngày kia)
        bookValues.put(COLUMN_BOOK_EQUIP_ID_FK, 1); // ID 1 = Máy ly tâm
        bookValues.put(COLUMN_BOOK_USER_ID_FK, "user_id_A");
        bookValues.put(COLUMN_BOOK_START_TIME, "2025-11-11T09:00:00");
        bookValues.put(COLUMN_BOOK_END_TIME, "2025-11-11T10:30:00");
        bookValues.put(COLUMN_BOOK_NOTES, "");
        db.insert(TABLE_BOOKINGS, null, bookValues);

        Log.i("DatabaseHelper", "Finished seeding database with initial data.");
    }
}