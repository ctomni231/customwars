package com.customwars.client.ui.state;

import com.customwars.client.App;
import com.customwars.client.controller.GameRulesController;
import com.customwars.client.io.loading.ThinglePageLoader;
import com.customwars.client.tools.ThingleUtil;
import com.customwars.client.ui.state.input.CWCommand;
import com.customwars.client.ui.state.input.CWInput;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.thingle.Page;
import org.newdawn.slick.thingle.internal.slick.FontWrapper;

import java.util.Arrays;
import java.util.List;

/**
 * Allow the user to define the game rules
 */
public class GameRulesState extends CWState {
  private Page page;
  private Image backGroundImage;
  private GameRulesController controller;
  private Font guiFont;

  public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
    controller = new GameRulesController(stateChanger, stateSession);
    guiFont = resources.getFont("gui_text");
    initPage();
    initPageContent();
    backGroundImage = resources.getSlickImg("light_menu_background");
    page.layout();
  }

  private void initPage() {
    ThinglePageLoader thingleLoader = new ThinglePageLoader(App.get("gui.path"));
    page = thingleLoader.loadPage("gameRules.xml", "greySkin.properties", controller);
    page.setFont(new FontWrapper(guiFont));
  }

  private void initPageContent() {
    List<String> translatedFogValues = Arrays.asList(App.translate("on"), App.translate("off"));
    ThingleUtil.fillCbo(page, page.getWidget("fog"), translatedFogValues, Arrays.asList("on", "off"));
    ThingleUtil.addChoice(page, page.getWidget("day_limit"), App.translate("unlimited"), "-1");
    ThingleUtil.fillCboWithNumbers(page, "day_limit", 5, 99, 1);
    ThingleUtil.fillCboWithNumbers(page, "funds", 1000, 10000, 1000);
    ThingleUtil.fillCboWithNumbers(page, "income", 1000, 10000, 1000);
  }

  @Override
  public void enter(GameContainer container, StateBasedGame game) throws SlickException {
    super.enter(container, game);
    controller.initValues();
    page.enable();
  }

  @Override
  public void leave(GameContainer container, StateBasedGame game) throws SlickException {
    super.leave(container, game);
    page.disable();
  }

  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {
    g.drawImage(backGroundImage, 0, 0);
    page.render();
  }

  @Override
  public void update(GameContainer container, int delta) throws SlickException {
  }

  @Override
  public void controlPressed(CWCommand command, CWInput cwInput) {
    if (command == CWInput.CANCEL) {
      controller.back();
    }
  }

  @Override
  public int getID() {
    return 13;
  }
}
