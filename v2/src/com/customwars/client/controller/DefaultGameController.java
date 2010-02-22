package com.customwars.client.controller;

import com.customwars.client.App;
import com.customwars.client.SFX;
import com.customwars.client.action.ActionFactory;
import com.customwars.client.action.CWAction;
import com.customwars.client.action.ShowPopupMenuAction;
import com.customwars.client.action.game.LoadGameAction;
import com.customwars.client.action.game.SaveGameAction;
import com.customwars.client.action.game.SaveReplayAction;
import com.customwars.client.action.unit.StartUnitCycleAction;
import com.customwars.client.model.game.Game;
import com.customwars.client.model.gameobject.City;
import com.customwars.client.model.gameobject.Unit;
import com.customwars.client.model.map.Map;
import com.customwars.client.model.map.Tile;
import com.customwars.client.ui.MenuItem;
import com.customwars.client.ui.renderer.GameRenderer;
import com.customwars.client.ui.state.InGameContext;
import org.apache.log4j.Logger;
import org.newdawn.slick.gui.GUIContext;

/**
 * Handles user input in the in game state
 */
public class DefaultGameController implements GameController {
  private static final Logger logger = Logger.getLogger(DefaultGameController.class);
  private final InGameCursorController cursorControl;
  private final InGameContext inGameContext;
  private final GUIContext guiContext;
  private final Game game;
  private final Map<Tile> map;

  public DefaultGameController(Game game, GameRenderer gameRenderer, InGameContext inGameContext) {
    this.game = game;
    this.map = game.getMap();
    this.inGameContext = inGameContext;
    this.guiContext = inGameContext.getContainer();
    this.cursorControl = new InGameCursorController(game, gameRenderer.getMapRenderer().getSpriteManager());
  }

  public void handleA(Tile cursorLocation) {
    City city = map.getCityOn(cursorLocation);
    Unit unit = getUnit(cursorLocation);

    if (canUnitAct(unit)) {
      inGameContext.handleUnitAPress(unit);
    } else if (canCityBuild(city)) {
      inGameContext.handleCityAPress(city);
    } else if (inGameContext.isDefaultMode()) {
      showContextMenu(cursorLocation);
    } else {
      logger.warn("could not handle A press");
    }
  }

  /**
   * Return true
   * when the given unit is active or
   * when a unit is selected and is going to perform an action
   */
  private boolean canUnitAct(Unit unit) {
    return unit != null && unit.isActive() || inGameContext.isInUnitMode();
  }

  /**
   * Return true
   * when the city is visible, can build a unit
   * and is owned by the active player
   */
  private boolean canCityBuild(City city) {
    if (city != null) {
      Tile cityLocation = (Tile) city.getLocation();
      boolean fogged = cityLocation.isFogged();
      boolean noUnitOnCity = cityLocation.getLocatableCount() == 0;
      boolean cityOwnedByActivePlayer = city.isOwnedBy(game.getActivePlayer());

      return !fogged && noUnitOnCity && cityOwnedByActivePlayer && city.canBuild();
    } else {
      return false;
    }
  }

  private void showContextMenu(Tile menuLocation) {
    ShowPopupMenuAction showContextMenuAction = ShowPopupMenuAction.createPopupInMap("Game menu", menuLocation);

    MenuItem endTurnMenuItem = new MenuItem(App.translate("end_turn"), guiContext);
    showContextMenuAction.addAction(ActionFactory.buildEndTurnAction(), endTurnMenuItem);

    if (App.isSinglePlayerGame()) {
      MenuItem saveGameMenuItem = new MenuItem(App.translate("save_game"), guiContext);
      showContextMenuAction.addAction(new SaveGameAction(), saveGameMenuItem);

      MenuItem loadGameMenuItem = new MenuItem(App.translate("load_game"), guiContext);
      showContextMenuAction.addAction(new LoadGameAction(), loadGameMenuItem);
    }

    MenuItem saveReplayMenuItem = new MenuItem(App.translate("save_replay"), guiContext);
    showContextMenuAction.addAction(new SaveReplayAction(), saveReplayMenuItem);

    MenuItem endGameMenuItem = new MenuItem(App.translate("end_game"), guiContext);
    showContextMenuAction.addAction(ActionFactory.buildEndGameAction(), endGameMenuItem);

    inGameContext.doAction(showContextMenuAction);
  }

  public void handleB(Tile cursorLocation) {
    Unit selectedUnit = getUnit(cursorLocation);
    if (selectedUnit != null) {
      inGameContext.handleUnitBPress(selectedUnit);
    }
  }

  /**
   * Return the active unit(if set)
   * or the selected unit(if present on the cursor location)
   * or null(both are not set)
   */
  private Unit getUnit(Tile cursorLocation) {
    Unit activeUnit = game.getActiveUnit();
    Unit selectedUnit = map.getUnitOn(cursorLocation);
    return activeUnit != null ? activeUnit : selectedUnit;
  }

  public void undo() {
    SFX.playSound("cancel");
    inGameContext.undo();
  }

  /**
   * Start unit cycle mode
   * If already in unit cycle mode, move the cursor to the next unit location
   * If there are no active units nothing happens
   */
  public void startUnitCycle() {
    if (!map.getActiveUnits().isEmpty()) {
      if (!inGameContext.isUnitCycleMode()) {
        inGameContext.doAction(new StartUnitCycleAction());
      } else {
        cursorControl.moveCursorToNextLocation();
      }
    }
  }

  public void endTurn() {
    CWAction endTurn = ActionFactory.buildEndTurnAction();
    inGameContext.doAction(endTurn);
  }

  public InGameCursorController getCursorController() {
    return cursorControl;
  }
}