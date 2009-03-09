package com.customwars.client.action;

import com.customwars.client.model.map.Tile;
import com.customwars.client.ui.HUD;
import com.customwars.client.ui.PopupMenu;
import com.customwars.client.ui.renderer.MapRenderer;
import com.customwars.client.ui.state.InGameSession;
import org.apache.log4j.Logger;
import org.newdawn.slick.gui.AbstractComponent;
import org.newdawn.slick.gui.ComponentListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Show a popup to te screen
 * The popup is filled with CWAction objects
 * When a menu item is pressed then the CWAction behind that item is executed.
 *
 * @author stefan
 */
public class ShowPopupMenu extends CWAction implements ComponentListener {
  private static final Logger logger = Logger.getLogger(ShowPopupMenu.class);
  private String popupName;
  private HUD hud;
  private List<CWAction> unitActions;
  private List<String> unitMenuItemNames;
  private InGameSession inGameSession;
  private MapRenderer mapRenderer;

  public ShowPopupMenu(String popupName, HUD hud, InGameSession inGameSession, MapRenderer mapRenderer) {
    super(popupName);
    this.popupName = popupName;
    this.hud = hud;
    this.inGameSession = inGameSession;
    this.mapRenderer = mapRenderer;
    this.unitActions = new ArrayList<CWAction>();
    this.unitMenuItemNames = new ArrayList<String>();
  }

  public void doActionImpl() {
    if (unitActions.size() == 0) {
      logger.warn("No menu items");
    }
    Tile clicked = inGameSession.getClick(2);
    if (clicked != null) {
      hud.showPopUp(clicked, popupName, unitMenuItemNames, this);
      mapRenderer.moveCursor(clicked);
    }

    inGameSession.setMode(InGameSession.MODE.GUI);
    mapRenderer.showArrows(true);
    mapRenderer.setCursorLocked(true);
  }

  public void undoAction() {
    hud.hidePopup();
    inGameSession.setMode(InGameSession.MODE.DEFAULT);
    mapRenderer.showArrows(false);
    mapRenderer.setCursorLocked(false);
  }

  public void addAction(CWAction action, String menuItemName) {
    unitActions.add(action);
    unitMenuItemNames.add(menuItemName);
  }

  public void clearActions() {
    unitActions.clear();
    unitMenuItemNames.clear();
  }

  public void componentActivated(AbstractComponent abstractComponent) {
    PopupMenu popupMenu = (PopupMenu) abstractComponent;
    CWAction action = unitActions.get(popupMenu.getCurrentOption());
    this.undoAction();    // Hide the popup when clicked on a item
    inGameSession.doAction(action);
  }

  public boolean atLeastHasOneItem() {
    return unitMenuItemNames.size() > 0;
  }
}
