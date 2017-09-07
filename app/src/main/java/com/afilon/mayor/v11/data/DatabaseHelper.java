package com.afilon.mayor.v11.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.afilon.mayor.v11.utils.ContextHandler;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int VERSION = 25;
	private static final String DATABASE_NAME = "parlacen_dbv10.db";
//	private static final String DATABASE_NAME = "parlacen_dbv32.db";
	private static File DATABASE_FILE;
	private boolean mInvalidDatabaseFile = false;
	private boolean mIsUpgraded = false;
	private Context mContext;

	/**
	 * number of users of the database connection.
	 * */
	private int mOpenConnections = 0;

	private static DatabaseHelper mInstance;

	synchronized static public DatabaseHelper getInstance(Context context) {
		if (mInstance == null) {
//			mInstance = new DatabaseHelper(ContextHandler.getElectionContext());
			mInstance = new DatabaseHelper(context.getApplicationContext());
		}
		return mInstance;
	}

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
		this.mContext = context;

		SQLiteDatabase db = null;
		try {
			db = getReadableDatabase();
			if (db != null) {
				db.close();
			}

			DATABASE_FILE = context.getDatabasePath(DATABASE_NAME);

			if (mInvalidDatabaseFile) {
				copyDatabase();
			}
			if (mIsUpgraded) {
				doUpgrade();
			}
		} catch (SQLiteException e) {
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		mInvalidDatabaseFile = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int old_version,
						  int new_version) {
		mInvalidDatabaseFile = true;
		mIsUpgraded = true;
	}

	/**
	 * called if a database upgrade is needed
	 */
	private void doUpgrade() {
		// implement the database upgrade here.
	}

	@Override
	public synchronized void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		// increment the number of users of the database connection.
		mOpenConnections++;
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	/**
	 * implementation to avoid closing the database connection while it is in
	 * use by others.
	 */
	@Override
	public synchronized void close() {
		mOpenConnections--;
		if (mOpenConnections == 0) {
			super.close();
		}
	}

	private void copyDatabase() {
		AssetManager assetManager = mContext.getResources().getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(DATABASE_NAME);
			out = new FileOutputStream(DATABASE_FILE);
			byte[] buffer = new byte[1024];
			int read = 0;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
		} catch (IOException e) {
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		setDatabaseVersion();
		mInvalidDatabaseFile = false;
	}

	private void setDatabaseVersion() {
		SQLiteDatabase db = null;
		try {
			db = SQLiteDatabase.openDatabase(DATABASE_FILE.getAbsolutePath(),
					null, SQLiteDatabase.OPEN_READWRITE);
			db.execSQL("PRAGMA user_version = " + VERSION);
		} catch (SQLiteException e) {
		} finally {
			if (db != null && db.isOpen()) {
				db.close();
			}
		}
	}

    public void backUpDataBase() {
        /** for debuggin purposes only */
        String outFileName = Environment.getExternalStorageDirectory() + File.separator + "SV_MYR_BD_copy.db";
//		DATABASE_FILE = mContext.getDatabasePath(DATABASE_NAME);
        FileInputStream fis = null;
        OutputStream output = null;
        try {
            fis = new FileInputStream(DATABASE_FILE);
            output = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            output.flush();
            output.close();
            fis.close();
        } catch (IOException e) {
            Log.e("COPY DB", "file not found: " + e.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
        }

    }

}