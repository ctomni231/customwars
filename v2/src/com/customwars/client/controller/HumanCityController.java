package com.customwars.client.controller;

import com.customwars.client.App;
import com.customwars.client.action.ActionFactory;
import com.customwars.client.action.CWAction;
import com.customwars.client.action.ShowPopupMenuAction;
import com.customwars.client.io.ResourceManager;
import com.customwars.client.model.gameobject.City;
import com.customwars.client.model.gameobject.Unit;
import com.customwars.client.model.gameobject.UnitFactory;
import com.customwars.client.model.map.Direction;
import com.customwars.client.model.map.Tile;
import com.customwars.client.ui.MenuItem;
import com.customwars.client.ui.renderer.MapRenderer;
import com.customwars.client.ui.state.InGameContext;
import org.newdawn.slick.Image;

import java.awt.Color;

/**
 * Allows a human to control a city
 * by using a gui
 *
 * @author stefan
 */
public class HumanCityController extends CityController {
  private final InGameContext context;
  private final MapRenderer mapRenderer;
  private final ResourceManager resources;

  public HumanCityController(City city, InGameContext inGameContext) {
    super(city, inGameContext.getGame());
    this.context = inGameContext;
    this.mapRenderer = inGameContext.getMapRenderer();
    this.resources = context.getResourceManager();

  }

  public void handleAPress() {
    Tile selected = mapRenderer.getCursorLocation();

    if (context.isDefaultMode() && canShowMenu()) {
      CWAction showMenu = buildMenu(selected);
      context.doAction(showMenu);
    }
  }

  private boolean canShowMenu() {
    Tile selected = mapRenderer.getCursorLocation();
    return !selected.isFogged() && city.getLocation() == selected && city.canBuild();
  }

  /**
   * Get Unit copies, display units that this city can build
   * Units that cannot be bought show a darker unit image and have a null action when clicked on.
   */
  private ShowPopupMenuAction buildMenu(Tile selected) {
    ShowPopupMenuAction showCityPopupMenuAction = ShowPopupMenuAction.createCenteredPopup("Buy city menu");

    for (Unit unit : UnitFactory.getAllUnits()) {
      if (city.canBuild(unit)) {
        boolean canAffordUnit = city.getOwner().isWithinBudget(unit.getStats().getPrice());
        MenuItem menuItem = buildMenuItem(unit, canAffordUnit);

        if (canAffordUnit) {
          unit.setOwner(city.getOwner());
          CWAction action = ActionFactory.buildAddUnitToTileAction(unit, selected, false);
          showCityPopupMenuAction.addAction(action, menuItem);
        } else {
          showCityPopupMenuAction.addAction(null, menuItem);
        }
      }
    }
    return showCityPopupMenuAction;
  }

  private MenuItem buildMenuItem(Unit unit, boolean active) {
    String unitInfo = App.translate(unit.getStats().getName()) + ' ' + unit.getStats().getPrice();
    Color cityColor = city.getOwner().getColor();
    Image unitImage = getUnitImg(unit, cityColor, active);
    MenuItem item = new MenuItem(unitImage, unitInfo, context.getContainer());

    if (!active) {
      item.setTextColor(new org.newdawn.slick.Color(255, 255, 255, 100));
    }
    return item;
  }

  private Image getUnitImg(Unit unit, Color color, boolean active) {
    Image unitImg;

    if (active) {
      unitImg = resources.getUnitImg(unit, color, Direction.EAST);
    } else {
      unitImg = resources.getShadedUnitImg(unit, color, Direction.EAST);
    }
    return unitImg;
  }
}

