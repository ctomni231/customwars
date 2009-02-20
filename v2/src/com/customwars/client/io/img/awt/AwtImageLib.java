package com.customwars.client.io.img.awt;

import org.newdawn.slick.loading.DeferredResource;
import org.newdawn.slick.loading.LoadingList;
import org.newdawn.slick.util.ResourceLoader;
import tools.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Load, Store and Recolor BufferedImages aka awt images.
 *
 * @author stefan
 */
public class AwtImageLib {
  private Map<String, BufferedImage> bufferedImgCache;  // Contains the base images that can be recolored by a filter
  private static Map<String, ImgFilter> imgFilters;     // Can recolor a base image from base color to replacement color
  private static Set<Color> supportedColors;            // The colors supported by each filter

  public AwtImageLib() {
    bufferedImgCache = new HashMap<String, BufferedImage>();
    imgFilters = new HashMap<String, ImgFilter>();
    supportedColors = new HashSet<Color>();
  }

  public void loadImg(String awtImgPath, String imgName) {
    if (LoadingList.isDeferredLoading()) {
      LoadingList.get().add(new DeferredAwtImgLoader(awtImgPath, imgName));
    } else {
      try {
        loadImage(awtImgPath, imgName);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Load the BufferedImage from the awtImgPath
   * add it to the cache keyed by the imgName
   */
  private void loadImage(String awtImgPath, String imgName) throws IOException {
    if (!bufferedImgCache.containsKey(imgName)) {
      addAwtImg(imgName, ImageIO.read(ResourceLoader.getResourceAsStream(awtImgPath)));
    }
  }

  //----------------------------------------------------------------------------
  // Recolor
  //----------------------------------------------------------------------------
  /**
   * load, Recolor and store awt image
   *
   * if deferredLoading is on, recolor at a later time
   *
   * @param recolorTo    The color to use for the recolored unit img
   * @param darkPerc     percentage to darken the image, 0 = ignore
   * @param filterName   'unit', 'property' the RGBfilter to use
   * @param baseImgName  UNIT_RED the image with the base color aka the source image
   * @param storeImgName UNIT_BLUE the name of the recolored image, key in the BufferedImage Map
   */
  public void recolorImg(Color recolorTo, String filterName, String baseImgName, String storeImgName, int darkPerc) {
    if (LoadingList.isDeferredLoading()) {
      LoadingList.get().add(new DeferredImgRecoloring(recolorTo, filterName, baseImgName, storeImgName, darkPerc));
    } else {
      recolorImage(recolorTo, filterName, baseImgName, storeImgName, darkPerc);
    }
  }

  private void recolorImage(Color recolorTo, String filterName, String baseImgName, String storeImgName, int darkPerc) {
    // If the img is already loaded return
    if (isAwtImgLoaded(storeImgName)) {
      return;
    }

    ImgFilter filter = imgFilters.get(filterName);
    if (filter == null)
      throw new IllegalArgumentException("No ImgFilter found for " + filterName + " available filters " + imgFilters.keySet());

    BufferedImage awtImg = getAwImg(baseImgName);
    filter.setReplacementColor(recolorTo);
    filter.setDarkenPercentage(darkPerc);
    BufferedImage recoloredImage = recolorImg(awtImg, filter);
    addAwtImg(storeImgName, recoloredImage);
  }

  /**
   * Recolor awtImage with the specified filter
   */
  public BufferedImage recolorImg(BufferedImage awtImg, ImgFilter filter) {
    FilteredImageSource imgProducer = new FilteredImageSource(awtImg.getSource(), filter);
    return recolorImg(imgProducer);
  }

  private BufferedImage recolorImg(ImageProducer imgProducer) {
    return ImageUtil.convertToBufferedImg(Toolkit.getDefaultToolkit().createImage(imgProducer));
  }

  //----------------------------------------------------------------------------
  public void clear() {
    bufferedImgCache.clear();
  }

  public void addAwtImg(String imgName, BufferedImage img) {
    if (imgName != null && img != null && !bufferedImgCache.containsKey(imgName))
      bufferedImgCache.put(imgName, img);
  }

  public static void addImgFilter(String filterName, ImgFilter filter) {
    if (filterName != null && filter != null && !imgFilters.containsKey(filterName))
      imgFilters.put(filterName, filter);
  }

  public void buildColorsFromImgFilters() {
    for (ImgFilter imgFilter : imgFilters.values()) {
      Set<java.awt.Color> replacementColors = imgFilter.getReplacementColors();
      for (java.awt.Color c : replacementColors) {
        addColor(c);
      }
    }
  }

  public void addColor(Color c) {
    boolean supportedByAllImgFilters = true;
    for (ImgFilter imgFilter : imgFilters.values()) {
      if (!imgFilter.canRecolorTo(c)) {
        supportedByAllImgFilters = false;
      }
    }

    if (supportedByAllImgFilters) {
      supportedColors.add(c);
    } else {
      throw new RuntimeException("Color " + c + " is not supported by all ImgFilters.");
    }
  }

  //----------------------------------------------------------------------------
  // Getters
  //----------------------------------------------------------------------------
  public boolean isAwtImgLoaded(String awtImgName) {
    return bufferedImgCache.containsKey(awtImgName);
  }

  public BufferedImage getAwImg(String awtImgName) {
    if (!bufferedImgCache.containsKey(awtImgName))
      throw new IllegalArgumentException("No awt image found for '" + awtImgName + "' available names " + bufferedImgCache.keySet());

    return bufferedImgCache.get(awtImgName);
  }

  public boolean contains(String awtImgName) {
    return bufferedImgCache.containsKey(awtImgName);
  }

  public int size() {
    return bufferedImgCache.size();
  }

  public Color getBaseColor(String filterName) {
    return imgFilters.get(filterName).getBaseColor();
  }

  public Set<Color> getSupportedColors() {
    return supportedColors;
  }

  private class DeferredAwtImgLoader implements DeferredResource {
    private String imgPath;
    private String imgName;

    public DeferredAwtImgLoader(String imgPath, String imgName) {
      this.imgPath = imgPath;
      this.imgName = imgName;
    }

    public void load() throws IOException {
      loadImage(imgPath, imgName);
    }

    public String getDescription() {
      return imgName;
    }
  }

  private class DeferredImgRecoloring implements DeferredResource {
    private Color recolorTo;
    private String filterName, baseImgName, storeImgName;
    private int darkPerc;

    public DeferredImgRecoloring(Color recolorTo, String filterName, String baseImgName, String storeImgName, int darkPerc) {
      this.recolorTo = recolorTo;
      this.filterName = filterName;
      this.baseImgName = baseImgName;
      this.storeImgName = storeImgName;
      this.darkPerc = darkPerc;
    }

    public void load() throws IOException {
      recolorImage(recolorTo, filterName, baseImgName, storeImgName, darkPerc);
    }

    public String getDescription() {
      return storeImgName;
    }
  }
}