package com.customwars.client.model.map;

import com.customwars.client.model.TestData;
import com.customwars.client.model.gameobject.Terrain;
import com.customwars.client.model.gameobject.TerrainFactory;
import com.customwars.client.model.gameobject.UnitFactory;
import com.customwars.client.model.map.path.Mover;
import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test functions in the TileMap class
 *
 * @author Stefan
 */
public class TileMapTest {
  private TileMap<Tile> map;
  private Terrain plain;

  @BeforeClass
  public static void beforeAllTests() {
    TestData.storeTestData();
  }

  @Before
  public void beforeEachTest() {
    plain = TerrainFactory.getTerrain(TestData.PLAIN);
    map = new Map(10, 15, 32, plain);
  }

  @AfterClass
  public static void afterAllTests() {
    TestData.clearTestData();
  }

  /**
   * Check If getAllTiles really Returns AllTiles
   * by counting them and checking them for notNull
   * All Tiles should at least have a terrain
   */
  @Test
  public void loopThroughAllTiles() {
    int nTiles = 0;
    for (Tile t : map.getAllTiles()) {
      Assert.assertNotNull(t);
      Assert.assertNotNull(t.getTerrain());
      nTiles++;
    }

    // The real amount should match the loop amount
    int realCount = map.countTiles();
    Assert.assertEquals(realCount, nTiles);
  }

  @Test
  public void isWithinMapBounds() {
    Location leftCorner = map.getTile(0, 0);
    Location tile = map.getTile(0, 1);
    Assert.assertFalse(map.isValid(map.getTile(-1, -1)));
    Assert.assertTrue(map.isValid(leftCorner));
    Assert.assertTrue(TileMap.isAdjacent(leftCorner, tile));
  }

  @Test
  public void testIsTileAdjacent() {
    Location left = map.getTile(0, 1);
    Location up = map.getTile(1, 0);
    Location right = map.getTile(2, 1);
    Location down = map.getTile(1, 2);
    Location center = map.getTile(1, 1);

    // All tiles touch the center tile
    Assert.assertTrue(TileMap.isAdjacent(left, center));
    Assert.assertTrue(TileMap.isAdjacent(up, center));
    Assert.assertTrue(TileMap.isAdjacent(right, center));
    Assert.assertTrue(TileMap.isAdjacent(down, center));

    // These Tiles don't touch
    Assert.assertFalse(TileMap.isAdjacent(down, up));
    Assert.assertFalse(TileMap.isAdjacent(left, right));
    Assert.assertFalse(TileMap.isAdjacent(up, left));
    Assert.assertFalse(TileMap.isAdjacent(up, right));
    Assert.assertFalse(TileMap.isAdjacent(up, down));
  }

  /**
   * Loop through all tiles that touch the baseTile 5,0
   * North = 4,-1  == excluded
   * South = 5,1  == included
   * East = 6,0   == included
   * West = 4,0   == included
   * Since the topEdge tile is the top row, the Iterator should ignore the north side
   * and not return it.
   */
  @Test
  public void adjacentIteratorEdgeOfMap1() {
    Location topEdge = map.getTile(5, 0);
    for (Location t : map.getSurroundingTiles(topEdge, 1, 1)) {
      boolean eastTileIsIncluded = t.getCol() == 6 && t.getRow() == 0;
      boolean southTileIsIncluded = t.getCol() == 5 && t.getRow() == 1;
      boolean westTileIsIncluded = t.getCol() == 4 && t.getRow() == 0;

      Assert.assertNotNull(t);
      Assert.assertTrue(eastTileIsIncluded || westTileIsIncluded || southTileIsIncluded);
    }
  }

  /**
   * Loop through all tiles that touch the leftTop tile 0,0
   * Iterating around the left top should return 2 Tiles east and south.
   * Ignoring North and West
   */
  @Test
  public void adjacentIteratorEdgeOfMap2() {
    Location leftTop = map.getTile(0, 0);
    for (Location t : map.getSurroundingTiles(leftTop, 1, 1)) {
      boolean eastTile = t.getCol() == 1 && t.getRow() == 0;
      boolean southTile = t.getCol() == 0 && t.getRow() == 1;
      Assert.assertNotNull(t);
      Assert.assertTrue(eastTile || southTile);
    }
  }

  @Test
  public void getDirectionTo() {
    Location baseTile = map.getTile(5, 5);
    Location left = map.getTile(4, 5);
    Location right = map.getTile(6, 5);
    Location up = map.getTile(5, 4);
    Location down = map.getTile(5, 6);

    // Connected Tiles should return correct compass direction
    Direction dir = map.getDirectionTo(baseTile, left);
    Assert.assertEquals(Direction.WEST, dir);

    dir = map.getDirectionTo(baseTile, right);
    Assert.assertEquals(Direction.EAST, dir);

    dir = map.getDirectionTo(baseTile, up);
    Assert.assertEquals(Direction.NORTH, dir);

    dir = map.getDirectionTo(baseTile, down);
    Assert.assertEquals(Direction.SOUTH, dir);

    dir = map.getDirectionTo(baseTile, baseTile);
    Assert.assertEquals(Direction.STILL, dir);

    // Not connected should return STILL
    dir = map.getDirectionTo(left, right);
    Assert.assertEquals(Direction.STILL, dir);
  }

  @Test
  public void testIllegalAdjacentDirection() {
    Tile from = map.getTile(0, 0);
    Tile to = map.getTile(-1, -1);      // null tile out of map bounds!

    Direction dir = map.getDirectionTo(from, to);
    Assert.assertEquals(Direction.STILL, dir);
  }

  @Test
  public void teleportTest() {
    Tile from = map.getTile(0, 0);
    Tile to = map.getTile(2, 0);

    Mover mover = UnitFactory.getUnit(TestData.INF);
    from.add(mover);
    map.teleport(from, to, mover);

    // Make sure that the unit moved to the location
    Assert.assertNull(from.getLastLocatable());
    Assert.assertEquals(0, from.getLocatableCount());

    Assert.assertEquals(to, mover.getLocation());
    Assert.assertEquals(to.getLastLocatable(), mover);
  }

  @Test
  public void testDiagonalDirection() {
    Tile from = map.getTile(0, 0);
    Tile to = map.getTile(1, 1);      // South east relative to from

    Direction dir = map.getDirectionTo(from, to);
    Assert.assertEquals(Direction.SOUTHEAST, dir);
  }

  @Test
  public void testIllegalDiagonalDirection() {
    Tile from = map.getTile(0, 0);
    Tile to = map.getTile(-1, -1);      // Nort West relative to from but out of map bounds!

    Direction dir = map.getDirectionTo(from, to);
    Assert.assertEquals(Direction.STILL, dir);
  }

  @Test
  public void testSquareIterator() {
    Tile center = map.getTile(1, 1);
    int surroundingTilesCount = 0;

    for (Location t : map.getSquareIterator(center, 1)) {
      surroundingTilesCount++;
      Assert.assertNotNull(t);
      Assert.assertNotSame("The center is never included", t, center);
    }

    Assert.assertEquals(8, surroundingTilesCount);
  }

  /**
   * 3 tiles are skipped because they are outside the map bounds
   */
  @Test
  public void testSquareIteratorAtMapEdge() {
    Tile center = map.getTile(0, 1);

    int surroundingTilesCount = 0;
    for (Location t : map.getSquareIterator(center, 1)) {
      surroundingTilesCount++;
    }

    Assert.assertEquals(5, surroundingTilesCount);
  }

  /**
   * 3 tiles are skipped because they are outside the map bounds
   */
  @Test
  public void testSquareIteratorAtMapEdge2() {
    Tile center = map.getTile(map.getCols() - 1, 1);

    int surroundingTilesCount = 0;
    for (Location t : map.getSquareIterator(center, 1)) {
      surroundingTilesCount++;
    }

    Assert.assertEquals(5, surroundingTilesCount);
  }

  /**
   * 5 tiles are skipped because they are outside the map bounds
   */
  @Test
  public void testSquareIteratorAtMapEdge3() {
    Tile center = map.getTile(0, map.getRows() - 1);

    int surroundingTilesCount = 0;
    for (Location t : map.getSquareIterator(center, 1)) {
      surroundingTilesCount++;
    }

    Assert.assertEquals(3, surroundingTilesCount);
  }

  /**
   * 3 tiles are skipped because they are outside the map bounds
   */
  @Test
  public void testSquareIteratorAtMapEdge4() {
    Tile center = map.getTile(1, map.getRows() - 1);

    int surroundingTilesCount = 0;
    for (Location t : map.getSquareIterator(center, 1)) {
      surroundingTilesCount++;
    }

    Assert.assertEquals(5, surroundingTilesCount);
  }

  /**
   * 0 tiles are skipped, there are 24 tiles(excluding the center) around 2,2
   */
  @Test
  public void testSquareIteratorWithBiggerRange() {
    Tile center = map.getTile(2, 2);

    int surroundingTilesCount = 0;
    for (Location t : map.getSquareIterator(center, 2)) {
      surroundingTilesCount++;
    }

    Assert.assertEquals(24, surroundingTilesCount);
  }

  /**
   * 1 row of tiles(5) are skipped, there are 19 tiles(excluding the center) around 2,2
   */
  @Test
  public void testSquareIteratorWithBiggerRange2() {
    Tile center = map.getTile(2, 1);

    int surroundingTilesCount = 0;
    for (Location t : map.getSquareIterator(center, 2)) {
      surroundingTilesCount++;
    }

    Assert.assertEquals(19, surroundingTilesCount);
  }

  @Test
  public void distanceTest() {
    Location a = map.getTile(0, 0);
    Location b = map.getTile(1, 1);

    // One down one to the right makes 2
    Assert.assertEquals(2, TileMap.getDistanceBetween(a, b));
  }

  @Test
  public void distanceTest2() {
    Location a = map.getTile(2, 2);
    Location b = map.getTile(4, 5);

    // 2 down 3 to the right makes 5
    Assert.assertEquals(5, TileMap.getDistanceBetween(a, b));
  }

  @Test
  public void quadrantTest() {
    Location topLeft = map.getTile(0, 0);
    Assert.assertEquals("Top left tile is always in the NW quadrant.",
      Direction.NORTHWEST, map.getQuadrantFor(topLeft));
  }

  @Test
  public void quadrantTest2() {
    Location topLeft = map.getTile(0, map.getRows() - 1);
    Assert.assertEquals("Down left tile is always in the SW quadrant.",
      Direction.SOUTHWEST, map.getQuadrantFor(topLeft));
  }

  @Test
  public void quadrantTest3() {
    Location topLeft = map.getTile(map.getCols() - 1, 0);
    Assert.assertEquals("Top right tile is always in the NE quadrant.",
      Direction.NORTHEAST, map.getQuadrantFor(topLeft));
  }


  /**
   * Should not throw an exception or return null.
   */
  @Test
  public void quadrantWithUnEvenMapSize() {
    Location middle = new Location2D(5, 7);
    TileMap map = new Map(10, 15, 32, plain);
    Direction quadrant = map.getQuadrantFor(middle);
    Assert.assertNotNull(quadrant);
  }

  @Test
  public void testRandomTile() {
    Tile random = map.getRandomTile();

    Assert.assertNotNull(random);
    Assert.assertTrue(map.isValid(random));
  }

  @Test(expected = IllegalArgumentException.class)
  public void illegalMapInitialisation() {
    new TileMap(-2, -1, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void illegalMapInitialisation2() {
    new TileMap(1, 1, -5);
  }

  @Test
  public void testRelativeTileOutOfUpperMapBounds() {
    Location leftCorner = map.getTile(0, 0);

    // The tile relative to the leftCorner in the direction north is out of the map bounds
    Location outOfMapLocation = map.getRelativeTile(leftCorner, Direction.NORTH);
    Assert.assertEquals(outOfMapLocation, null);
  }

  @Test
  public void testRelativeTileOutOfRightMapBounds() {
    Location rightEdgeOfTheMap = map.getTile(map.getCols() - 1, 2);

    // The tile relative to the rightEdgeOfTheMap in the direction east is out of the map bounds
    Location outOfMapLocation = map.getRelativeTile(rightEdgeOfTheMap, Direction.EAST);
    Assert.assertEquals(outOfMapLocation, outOfMapLocation);
  }

  @Test
  public void testRelativeToItself() {
    Location leftCorner = map.getTile(0, 0);

    // The tile relative to the leftCorner in the direction still is the same tile
    Location sameRelativeLocation = map.getRelativeTile(leftCorner, Direction.STILL);
    Assert.assertEquals(leftCorner, sameRelativeLocation);
  }

  @Test
  public void testRelativeTileEast() {
    Location baseTile = map.getTile(2, 2);
    Location expectedTile = map.getTile(3, 2);

    Location relativeTile = map.getRelativeTile(baseTile, Direction.EAST);
    Assert.assertEquals(expectedTile, relativeTile);
  }

  @Test
  public void testRelativeTileSouth() {
    Location baseTile = map.getTile(2, 2);
    Location expectedTile = map.getTile(2, 3);

    Location relativeTile = map.getRelativeTile(baseTile, Direction.SOUTH);
    Assert.assertEquals(expectedTile, relativeTile);
  }

  @Test
  public void testRelativeTileNorth() {
    Location baseTile = map.getTile(2, 2);
    Location expectedTile = map.getTile(2, 1);

    Location relativeTile = map.getRelativeTile(baseTile, Direction.NORTH);
    Assert.assertEquals(expectedTile, relativeTile);
  }

  @Test
  public void testRelativeTileWest() {
    Location baseTile = map.getTile(2, 2);
    Location expectedTile = map.getTile(1, 2);

    Location relativeTile = map.getRelativeTile(baseTile, Direction.WEST);
    Assert.assertEquals(expectedTile, relativeTile);
  }

  @Test
  public void testRelativeTileNorthWest() {
    Location baseTile = map.getTile(2, 2);
    Location expectedTile = map.getTile(1, 1);

    Location relativeTile = map.getRelativeTile(baseTile, Direction.NORTHWEST);
    Assert.assertEquals(expectedTile, relativeTile);
  }

  @Test
  public void testRelativeTileSouthEast() {
    Location baseTile = map.getTile(2, 2);
    Location expectedTile = map.getTile(3, 3);

    Location relativeTile = map.getRelativeTile(baseTile, Direction.SOUTHEAST);
    Assert.assertEquals(expectedTile, relativeTile);
  }
}
