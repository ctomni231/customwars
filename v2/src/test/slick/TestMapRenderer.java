package test.slick;

import com.customwars.client.io.ResourceManager;
import com.customwars.client.io.img.ImageLib;
import com.customwars.client.io.img.slick.ImageStrip;
import com.customwars.client.model.map.Direction;
import com.customwars.client.model.map.Tile;
import com.customwars.client.model.map.TileMap;
import com.customwars.client.ui.renderer.MapRenderer;
import com.customwars.client.ui.renderer.TerrainRenderer;
import com.customwars.client.ui.sprite.TileSprite;
import com.customwars.client.ui.state.CWInput;
import com.customwars.client.ui.state.CWState;
import org.apache.log4j.Logger;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.command.Command;
import org.newdawn.slick.state.StateBasedGame;
import test.testData.HardCodedGame;

import java.io.IOException;

/**
 * renders a hardcoded map
 *
 * @author stefan
 */
public class TestMapRenderer extends CWState {
  private static final Logger logger = Logger.getLogger(TestMapRenderer.class);
  private MapRenderer mapRenderer;
  private TerrainRenderer miniMapRenderer;
  private CWInput input;

  public TestMapRenderer(CWInput input) {
    this.input = input;
  }

  public void init(GameContainer container, StateBasedGame game) throws SlickException {
    ImageLib imageLib = new ImageLib();
    ResourceManager resources = new ResourceManager(imageLib);
    resources.setDataPath("res/data/");
    resources.setImgPath("res/image/");

    try {
      resources.loadImages();
    } catch (IOException e) {
      logger.fatal(e);
    }

    TileMap<Tile> map = HardCodedGame.getMap();
    ImageStrip terrainStrip = imageLib.getSlickImgStrip("terrains");
    ImageStrip cursor1 = imageLib.getSlickImgStrip("selectCursor");
    ImageStrip cursor2 = imageLib.getSlickImgStrip("aimCursor");

    TileSprite selectCursor = new TileSprite(cursor1, 250, map.getRandomTile(), map);
    TileSprite aimCursor = new TileSprite(cursor2, map.getRandomTile(), map);

    mapRenderer = new MapRenderer(map);
    mapRenderer.loadResources(resources);
    mapRenderer.setTerrainStrip(terrainStrip);
    mapRenderer.addCursor("SELECT", selectCursor);
    mapRenderer.addCursor("AIM", aimCursor);
    mapRenderer.activedCursor("SELECT");

    ImageStrip miniMapTerrainStrip = imageLib.getSlickImgStrip("miniMap");
    miniMapRenderer = new TerrainRenderer(map);
    miniMapRenderer.setTerrainStrip(miniMapTerrainStrip);
    miniMapRenderer.setLocation(510, 25);
  }

  public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
    mapRenderer.render(g);
    g.drawString(mapRenderer.getCursorLocation().toString(), 10, container.getHeight() - 20);

    g.drawString("MiniMap", 500, 2);
    miniMapRenderer.render(g);
  }

  public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
    mapRenderer.update(delta);
  }

  public void controlPressed(Command command) {
    moveCursor(command);
    if (input.isSelectPressed(command)) {
      System.out.println("Clicked on " + mapRenderer.getCursorLocation());
    }
  }

  private void moveCursor(Command command) {
    if (input.isUpPressed(command)) {
      mapRenderer.moveCursor(Direction.NORTH);
    }
    if (input.isDownPressed(command)) {
      mapRenderer.moveCursor(Direction.SOUTH);
    }
    if (input.isLeftPressed(command)) {
      mapRenderer.moveCursor(Direction.WEST);
    }
    if (input.isRightPressed(command)) {
      mapRenderer.moveCursor(Direction.EAST);
    }
  }

  public void keyReleased(int key, char c) {
    if (key == Input.KEY_0) {
      mapRenderer.activedCursor("DOES_NOT_EXISTS");
    }
    if (key == Input.KEY_1) {
      mapRenderer.activedCursor("AIM");
    }
    if (key == Input.KEY_2) {
      mapRenderer.activedCursor("SELECT");
    }
  }

  public void mouseMoved(int oldx, int oldy, int newx, int newy) {
    mapRenderer.moveCursor(newx, newy);
  }

  public int getID() {
    return 1;
  }
}