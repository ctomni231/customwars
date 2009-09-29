package com.customwars.client.io;

import com.customwars.client.io.img.AnimLib;
import com.customwars.client.io.img.ImageLib;
import com.customwars.client.io.img.slick.ImageStrip;
import com.customwars.client.io.img.slick.SlickImageFactory;
import com.customwars.client.io.img.slick.SpriteSheet;
import com.customwars.client.io.loading.AnimationParser;
import com.customwars.client.io.loading.ImageFilterParser;
import com.customwars.client.io.loading.ImageParser;
import com.customwars.client.io.loading.ModelLoader;
import com.customwars.client.io.loading.SoundParser;
import com.customwars.client.io.loading.map.BinaryCW2MapParser;
import com.customwars.client.io.loading.map.MapParser;
import com.customwars.client.model.gameobject.Unit;
import com.customwars.client.model.map.Direction;
import com.customwars.client.model.map.Map;
import com.customwars.client.model.map.Tile;
import org.apache.log4j.Logger;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Font;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.loading.LoadingList;
import org.newdawn.slick.util.ResourceLoader;
import tools.Args;
import tools.ColorUtil;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Handles and stores all kind of resources: Images, Sounds, Music, Animations
 * Each of these resources is mapped to a string ie "SELECT_SOUND" -> Sound object
 * Other classes can now ask this class to play the sound
 * ie resourceManager.playSound("SELECT_SOUND")
 *
 * Before loading, the paths where the resources are located should be set
 *
 * @author stefan
 */
public class ResourceManager {
  private static final Logger logger = Logger.getLogger(ResourceManager.class);
  private static final String IMAGE_LOADER_FILE = "imageLoader.txt";
  private static final String ANIM_LOADER_FILE = "animLoader.txt";
  private static final String SOUND_LOADER_FILE = "soundLoader.txt";
  private static final String COLORS_FILE = "colors.xml";
  private static final ModelLoader modelLoader = new ModelLoader();
  private MapParser mapParser;
  private int darkPercentage;

  private ImageLib imageLib;
  private AnimLib animLib;

  private HashMap<String, Sound> sounds = new HashMap<String, Sound>();
  private HashMap<String, Music> music = new HashMap<String, Music>();
  private HashMap<String, Map<Tile>> maps = new HashMap<String, Map<Tile>>();
  private HashMap<String, Font> fonts = new HashMap<String, Font>();
  private String imgPath, soundPath, dataPath, fontPath;
  private List<String> mapPaths;

  public ResourceManager() {
    this(new ImageLib(), new AnimLib());
    this.mapPaths = new ArrayList<String>();
  }

  /**
   * @param imageLib The cache to load the image to
   * @param animLib  The cache to load the animations to
   */
  public ResourceManager(ImageLib imageLib, AnimLib animLib) {
    this.imageLib = imageLib;
    this.animLib = animLib;
    SlickImageFactory.setDeferredLoading(LoadingList.isDeferredLoading());
    this.mapParser = new BinaryCW2MapParser();
  }

  public void loadAll() {
    logger.info("Loading resources");
    loadModel();
    try {
      loadResources();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load resource " + e);
    }
  }

  public void loadModel() {
    modelLoader.setModelResPath(dataPath);
    modelLoader.loadModel();
  }

  public void loadResources() throws IOException {
    loadColorsFromFile();
    loadImagesFromFile();
    loadAnimationsFromFile();
    recolor();
    loadSoundFromFile();
    loadAllMaps();
    loadFonts();
    releaseUnneededResources();
  }

  private void loadColorsFromFile() throws IOException {
    ImageFilterParser imgFilterParser = new ImageFilterParser();
    InputStream in = ResourceLoader.getResourceAsStream(dataPath + COLORS_FILE);
    imgFilterParser.loadConfigFile(in);
    imageLib.buildColorsFromImgFilters();
  }

  private void loadImagesFromFile() throws IOException {
    logger.info("Reading file " + imgPath + IMAGE_LOADER_FILE);
    ImageParser imgParser = new ImageParser(imageLib);
    InputStream in = ResourceLoader.getResourceAsStream(imgPath + IMAGE_LOADER_FILE);
    imgParser.setImgPath(imgPath);
    imgParser.loadConfigFile(in);
  }

  private void loadAnimationsFromFile() throws IOException {
    AnimationParser animParser = new AnimationParser(imageLib, animLib);
    InputStream in = ResourceLoader.getResourceAsStream(imgPath + ANIM_LOADER_FILE);
    animParser.loadConfigFile(in);
  }

  private void recolor() {
    Set<Color> supportedColors = getSupportedColors();
    List<Color> colors = new ArrayList<Color>(supportedColors);
    recolor(colors.toArray(new Color[colors.size()]));
  }

  private void loadSoundFromFile() throws IOException {
    SoundParser soundParser = new SoundParser(sounds, music);
    InputStream in = ResourceLoader.getResourceAsStream(soundPath + SOUND_LOADER_FILE);
    soundParser.setSoundPath(soundPath);
    soundParser.loadConfigFile(in);
  }

  private void loadFonts() throws IOException {
    Font defaultFont;
    try {
      defaultFont = new AngelCodeFont(fontPath + "default.fnt", fontPath + "default_00.tga");
    } catch (SlickException ex) {
      throw new IOException(ex);
    }
    addFont("DEFAULT", defaultFont);
  }

  public void addFont(String fontID, Font font) {
    Args.checkForNull(fontID);
    Args.checkForNull(font);
    fonts.put(fontID.toUpperCase(), font);
  }

  private void releaseUnneededResources() {
    imageLib.clearImageSources();
  }

  /**
   * Load all the maps from the 'map paths locations'
   * using the CW2 binary map parser
   *
   * Maps are searched for in the current map path and all subdirs
   */
  private void loadAllMaps() throws IOException {
    for (String mapPath : mapPaths) {
      FileSystemManager fsm = new FileSystemManager(mapPath);

      // Current dir
      File f = new File(mapPath);
      readMapsFromDir(fsm, f);

      // Subdirs
      for (File category : fsm.getDirs()) {
        readMapsFromDir(fsm, category);
      }
    }
  }

  private void readMapsFromDir(FileSystemManager fsm, File dir) throws IOException {
    for (File mapFile : fsm.getFiles(dir)) {
      if (mapFile.getName().endsWith(".map")) {
        InputStream in = ResourceLoader.getResourceAsStream(mapFile.getPath());
        Map<Tile> map = mapParser.readMap(in);
        String mapName = map.getProperty("NAME");
        maps.put(mapName, map);
      } else {
        logger.warn("Skipping " + mapFile + " wrong extension expected .map");
      }
    }
  }

  public void saveMap(Map<Tile> map, OutputStream out) throws IOException {
    mapParser.writeMap(map, out);
    String mapName = map.getProperty("NAME");
    maps.put(mapName, map);
  }

  public void recolor(Color... colors) {
    recolor(Arrays.asList(colors));
  }

  public void recolor(Collection<Color> colors) {
    recolorImages(colors);
    createRecoloredAnimations(colors);
  }

  private void recolorImages(Collection<Color> colors) {
    for (Color color : colors) {
      checkIsColorSupported(color);
      imageLib.recolorImg(color, "unit");
      imageLib.recolorImg(color, "city");
    }

    for (Color color : colors) {
      imageLib.recolorImg(color, "unit", "darker", "unit", darkPercentage);
      imageLib.recolorImg(color, "city", "darker", "city", darkPercentage);
    }
  }

  private void createRecoloredAnimations(Collection<Color> colors) {
    Color unitBaseColor = getBaseColor("unit");
    Color cityBaseColor = getBaseColor("city");

    for (Color color : colors) {
      checkIsColorSupported(color);
      animLib.createUnitAnimations(unitBaseColor, this, color);
      animLib.createCityAnimations(cityBaseColor, this, color);
    }
  }

  private void checkIsColorSupported(Color color) {
    if (!imageLib.getSupportedColors().contains(color)) {
      throw new IllegalArgumentException(
        "Color " + color + " is not supported, add the color info to " + COLORS_FILE);
    }
  }

  public void setDarkPercentage(int darkPercentage) {
    this.darkPercentage = darkPercentage;
  }

  public void setImgPath(String path) {
    this.imgPath = path;
  }

  public void setDataPath(String path) {
    this.dataPath = path;
  }

  public void setSoundPath(String soundPath) {
    this.soundPath = soundPath;
  }

  public void addMapPath(String mapPath) {
    this.mapPaths.add(mapPath);
  }

  public void setFontPath(String fontPath) {
    this.fontPath = fontPath;
  }

  public boolean isSlickImgLoaded(String slickImgName) {
    return imageLib.isSlickImgLoaded(slickImgName);
  }

  public Image getSlickImg(String imgName) {
    return imageLib.getSlickImg(imgName);
  }

  public ImageStrip getSlickImgStrip(String imgName) {
    return (ImageStrip) imageLib.getSlickImg(imgName);
  }

  public SpriteSheet getSlickSpriteSheet(String imgName, Color color, String suffix) {
    String colorName = ColorUtil.toString(color);
    return getSlickSpriteSheet(imgName + "_" + colorName + "_" + suffix);
  }

  public SpriteSheet getSlickSpriteSheet(String imgName, Color color) {
    String colorName = ColorUtil.toString(color);
    return getSlickSpriteSheet(imgName + "_" + colorName);
  }

  public SpriteSheet getSlickSpriteSheet(String imgName) {
    return (SpriteSheet) imageLib.getSlickImg(imgName);
  }

  public int countSlickImages() {
    return imageLib.countSlickImages();
  }

  public Color getBaseColor(String filterName) {
    return imageLib.getBaseColor(filterName);
  }

  public Set<Color> getSupportedColors() {
    return imageLib.getSupportedColors();
  }

  public Animation getAnim(String animName) {
    return animLib.getAnim(animName);
  }

  public Animation getCityAnim(int cityID, Color color) {
    return getCityAnim(cityID, color, "");
  }

  public Animation getCityAnim(int cityID, Color color, String suffix) {
    return animLib.getCityAnim(cityID, color, suffix);
  }

  public Animation getUnitAnim(Unit unit, Color color, String suffix) {
    return animLib.getUnitAnim(unit.getImgRowID(), color, suffix);
  }

  public Collection<Animation> getAllAnims() {
    return animLib.getAllAnimations();
  }

  public Font getFont(String fontName) {
    String key = fontName.toUpperCase();
    if (!fonts.containsKey(key)) {
      throw new IllegalArgumentException("no font stored for " + key);
    }
    return fonts.get(key);
  }

  public Music getMusic(String musicName) {
    return music.get(musicName.toUpperCase());
  }

  public Sound getSound(String soundName) {
    return sounds.get(soundName.toUpperCase());
  }

  /**
   * @param mapName the exact name of the map, case sensitive
   * @return a copy of the Map with mapName
   */
  public Map<Tile> getMap(String mapName) {
    if (!maps.containsKey(mapName)) {
      throw new IllegalArgumentException("no map stored for " + mapName);
    }
    return new Map<Tile>(maps.get(mapName));
  }

  public Set<String> getAllMapNames() {
    return Collections.unmodifiableSet(maps.keySet());
  }

  public SpriteSheet getUnitSpriteSheet(Color color) {
    String colorName = ColorUtil.toString(color);
    return getSlickSpriteSheet("UNIT_" + colorName);
  }

  public Image getUnitImg(Unit unit, Direction direction) {
    Color playerColor = unit.getOwner().getColor();
    return getUnitImg(unit, playerColor, direction);
  }

  public Image getUnitImg(Unit unit, Color color, Direction direction) {
    SpriteSheet unitSpriteSheet = getUnitSpriteSheet(color);
    int row = unit.getImgRowID();
    return cropUnitImg(unitSpriteSheet, direction, row);
  }

  public SpriteSheet getShadedUnitSpriteSheet(Color color) {
    String colorName = ColorUtil.toString(color);
    return getSlickSpriteSheet("UNIT_" + colorName + "_darker");
  }

  public Image getShadedUnitImg(Unit unit, Direction direction) {
    Color playerColor = unit.getOwner().getColor();
    return getShadedUnitImg(unit, playerColor, direction);
  }

  public Image getShadedUnitImg(Unit unit, Color color, Direction direction) {
    SpriteSheet unitSpriteSheet = getShadedUnitSpriteSheet(color);
    int row = unit.getImgRowID();
    return cropUnitImg(unitSpriteSheet, direction, row);
  }

  /**
   * Crop a unit from a spritesheet
   * that is looking in the given direction
   * Supported directions(N,E,S,W) all other directions will throw an IllegalArgumentException
   */
  private Image cropUnitImg(SpriteSheet unitSpriteSheet, Direction direction, int row) {
    Image unitImg;

    switch (direction) {
      case NORTH:
        unitImg = unitSpriteSheet.getSubImage(10, row);
        break;
      case EAST:
        unitImg = unitSpriteSheet.getSubImage(4, row);
        break;
      case SOUTH:
        unitImg = unitSpriteSheet.getSubImage(7, row);
        break;
      case WEST:
        unitImg = unitSpriteSheet.getSubImage(1, row);
        break;
      default:
        throw new IllegalArgumentException("Direction " + direction + " is not supported for a unit");
    }
    return unitImg;
  }

  public SpriteSheet getCitySpriteSheet(Color color) {
    String colorName = ColorUtil.toString(color);
    return getSlickSpriteSheet("CITY_" + colorName);
  }
}
