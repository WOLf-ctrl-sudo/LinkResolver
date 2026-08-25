package com.linkresolver.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ResolutionHistoryDao _resolutionHistoryDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `resolution_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `originalUrl` TEXT NOT NULL, `finalUrl` TEXT, `provider` TEXT NOT NULL, `status` TEXT NOT NULL, `redirectCount` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '50ad390707faa062f9cd9a2d0d843e25')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `resolution_history`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsResolutionHistory = new HashMap<String, TableInfo.Column>(7);
        _columnsResolutionHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResolutionHistory.put("originalUrl", new TableInfo.Column("originalUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResolutionHistory.put("finalUrl", new TableInfo.Column("finalUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResolutionHistory.put("provider", new TableInfo.Column("provider", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResolutionHistory.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResolutionHistory.put("redirectCount", new TableInfo.Column("redirectCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsResolutionHistory.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysResolutionHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesResolutionHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoResolutionHistory = new TableInfo("resolution_history", _columnsResolutionHistory, _foreignKeysResolutionHistory, _indicesResolutionHistory);
        final TableInfo _existingResolutionHistory = TableInfo.read(db, "resolution_history");
        if (!_infoResolutionHistory.equals(_existingResolutionHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "resolution_history(com.linkresolver.app.data.local.ResolutionHistoryEntity).\n"
                  + " Expected:\n" + _infoResolutionHistory + "\n"
                  + " Found:\n" + _existingResolutionHistory);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "50ad390707faa062f9cd9a2d0d843e25", "42195386d95a257a78b7171ff9497abf");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "resolution_history");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `resolution_history`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ResolutionHistoryDao.class, ResolutionHistoryDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ResolutionHistoryDao resolutionHistoryDao() {
    if (_resolutionHistoryDao != null) {
      return _resolutionHistoryDao;
    } else {
      synchronized(this) {
        if(_resolutionHistoryDao == null) {
          _resolutionHistoryDao = new ResolutionHistoryDao_Impl(this);
        }
        return _resolutionHistoryDao;
      }
    }
  }
}
