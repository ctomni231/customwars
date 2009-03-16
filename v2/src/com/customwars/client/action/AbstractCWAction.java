package com.customwars.client.action;

/**
 * Default implementation of a CWAction
 *
 * @author stefan
 */
public abstract class AbstractCWAction implements CWAction {
  String name;
  boolean canUndo;
  boolean actionCompleted;

  public AbstractCWAction(String name) {
    this(name, true);
  }

  public AbstractCWAction(String name, boolean canUndo) {
    this.name = name;
    this.canUndo = canUndo;
  }

  public void update(int elapsedTime) {
  }

  public void doAction() {
    doActionImpl();
    actionCompleted = true;
  }

  protected abstract void doActionImpl();

  public void undoAction() {
  }

  public void setActionCompleted(boolean completed) {
    this.actionCompleted = completed;
  }

  public String getName() {
    return name;
  }

  public boolean canUndo() {
    return canUndo;
  }

  public boolean isActionCompleted() {
    return actionCompleted;
  }

  public String toString() {
    return name;
  }
}
