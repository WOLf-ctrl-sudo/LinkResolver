package com.linkresolver.app.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ResolutionHistoryDao_Impl implements ResolutionHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ResolutionHistoryEntity> __insertionAdapterOfResolutionHistoryEntity;

  private final EntityDeletionOrUpdateAdapter<ResolutionHistoryEntity> __deletionAdapterOfResolutionHistoryEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public ResolutionHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfResolutionHistoryEntity = new EntityInsertionAdapter<ResolutionHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `resolution_history` (`id`,`originalUrl`,`finalUrl`,`provider`,`status`,`redirectCount`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ResolutionHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getOriginalUrl());
        if (entity.getFinalUrl() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getFinalUrl());
        }
        statement.bindString(4, entity.getProvider());
        statement.bindString(5, entity.getStatus());
        statement.bindLong(6, entity.getRedirectCount());
        statement.bindLong(7, entity.getTimestamp());
      }
    };
    this.__deletionAdapterOfResolutionHistoryEntity = new EntityDeletionOrUpdateAdapter<ResolutionHistoryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `resolution_history` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ResolutionHistoryEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM resolution_history WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM resolution_history";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ResolutionHistoryEntity entity,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfResolutionHistoryEntity.insertAndReturnId(entity);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final ResolutionHistoryEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfResolutionHistoryEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ResolutionHistoryEntity>> observeAll() {
    final String _sql = "SELECT * FROM resolution_history ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"resolution_history"}, new Callable<List<ResolutionHistoryEntity>>() {
      @Override
      @NonNull
      public List<ResolutionHistoryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfOriginalUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "originalUrl");
          final int _cursorIndexOfFinalUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "finalUrl");
          final int _cursorIndexOfProvider = CursorUtil.getColumnIndexOrThrow(_cursor, "provider");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfRedirectCount = CursorUtil.getColumnIndexOrThrow(_cursor, "redirectCount");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<ResolutionHistoryEntity> _result = new ArrayList<ResolutionHistoryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ResolutionHistoryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpOriginalUrl;
            _tmpOriginalUrl = _cursor.getString(_cursorIndexOfOriginalUrl);
            final String _tmpFinalUrl;
            if (_cursor.isNull(_cursorIndexOfFinalUrl)) {
              _tmpFinalUrl = null;
            } else {
              _tmpFinalUrl = _cursor.getString(_cursorIndexOfFinalUrl);
            }
            final String _tmpProvider;
            _tmpProvider = _cursor.getString(_cursorIndexOfProvider);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final int _tmpRedirectCount;
            _tmpRedirectCount = _cursor.getInt(_cursorIndexOfRedirectCount);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new ResolutionHistoryEntity(_tmpId,_tmpOriginalUrl,_tmpFinalUrl,_tmpProvider,_tmpStatus,_tmpRedirectCount,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
