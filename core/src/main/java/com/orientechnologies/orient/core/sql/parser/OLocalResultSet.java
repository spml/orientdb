package com.orientechnologies.orient.core.sql.parser;

import com.orientechnologies.orient.core.db.document.OQueryLifecycleListener;
import com.orientechnologies.orient.core.sql.executor.OExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OInternalExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OTodoResultSet;

import java.util.*;

/**
 * Created by luigidellaquila on 07/07/16.
 */
public class OLocalResultSet implements OTodoResultSet {
  List<OQueryLifecycleListener> lifecycleListeners = new ArrayList<>();

  private OTodoResultSet lastFetch = null;
  private final OInternalExecutionPlan executionPlan;
  private boolean finished = false;

  public OLocalResultSet(OInternalExecutionPlan executionPlan) {
    this.executionPlan = executionPlan;
    fetchNext();
  }

  private boolean fetchNext() {
    lastFetch = executionPlan.fetchNext(100);
    if (!lastFetch.hasNext()) {
      finished = true;
      return false;
    }
    return true;
  }

  @Override public boolean hasNext() {
    if (finished) {
      return false;
    }
    if (lastFetch.hasNext()) {
      return true;
    } else {
      return fetchNext();
    }
  }

  @Override public OResult next() {
    if (finished) {
      throw new IllegalStateException();
    }
    if (!lastFetch.hasNext()) {
      if (!fetchNext()) {
        throw new IllegalStateException();
      }
    }
    return lastFetch.next();
  }

  @Override public void close() {
    executionPlan.close();
    this.lifecycleListeners.forEach(x -> x.queryClosed(this));
    this.lifecycleListeners.clear();
  }

  @Override public Optional<OExecutionPlan> getExecutionPlan() {
    return Optional.of(executionPlan);
  }

  @Override public Map<String, Object> getQueryStats() {
    return new HashMap<>();//TODO
  }

  public void addLifecycleListener(OQueryLifecycleListener db) {
    this.lifecycleListeners.add(db);
  }
}
