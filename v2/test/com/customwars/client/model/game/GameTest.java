package com.customwars.client.model.game;

import com.customwars.client.model.TestData;
import com.customwars.client.model.gameobject.City;
import com.customwars.client.model.gameobject.GameObjectState;
import com.customwars.client.model.gameobject.Terrain;
import com.customwars.client.model.gameobject.TerrainFactory;
import com.customwars.client.model.gameobject.Unit;
import com.customwars.client.model.gameobject.UnitFactory;
import com.customwars.client.model.map.Map;
import com.customwars.client.tools.MapUtil;
import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.Color;
import java.util.Arrays;

public class GameTest {
  private Game game;
  private Map map;    // Hardcoded map, see beforeEachTest and buildHardCodedMap
  private static final Color[] colors = new Color[]{Color.WHITE, Color.GREEN, Color.BLUE, Color.BLACK, Color.RED, Color.YELLOW};

  @BeforeClass
  public static void beforeAllTests() {
    TestData.storeTestData();
  }

  @Before
  public void beforeEachTest() {
    Terrain plain = TerrainFactory.getTerrain(TestData.PLAIN);
    map = new Map(10, 10, 32, plain);
  }

  @After
  public void afterEachTest() {
    game = null;
    map = null;
  }

  @AfterClass
  public static void afterAllTests() {
    TestData.clearTestData();
  }

  /**
   * p1 Start budget=800 cities=1
   * p2 Start budget=5000 cities=1
   */
  @Test
  public void testCityBudget() {
    int P1_START_BUDGET = 800;
    int P2_START_BUDGET = 5000;
    int CITY_FUNDS = 1200;

    buildHardCodedMap(2);
    Player p1 = new Player(0, Color.RED, "Stef", P1_START_BUDGET, 0, false);
    Player p2 = new Player(1, Color.BLACK, "JSR", P2_START_BUDGET, 5, false);
    GameRules gameRules = new GameRules();
    gameRules.setCityFunds(CITY_FUNDS);

    // p1 is starting the game
    startGame(p1, gameRules, p1, p2);

    // Game is now started
    Assert.assertTrue(game.isStarted());
    Assert.assertEquals(0, game.getTurn());
    Assert.assertEquals(p1, game.getActivePlayer());

    // p1 has more cash because the one city it had
    // added 1200 funds to p1's budget.
    Assert.assertEquals(p1.getBudget(), P1_START_BUDGET + CITY_FUNDS);

    game.endTurn();

    // 1 turn further
    // p2 is now the activeplayer
    Assert.assertEquals(1, game.getTurn());
    Assert.assertEquals(p2, game.getActivePlayer());

    // p2 has more cash because the one city it had
    // added 1200 funds to p2's budget.
    Assert.assertEquals(p2.getBudget(), P2_START_BUDGET + CITY_FUNDS);
  }

  /**
   * p2 cannot end his turn because the active Player is p1
   */
  @Test(expected = NotYourTurnException.class)
  public void testEndTurnWithNonActivePlayer() {
    buildHardCodedMap(2);
    Player p1 = new Player(0, Color.RED, "Stef", 0, 0, false);
    Player p2 = new Player(1, Color.BLACK, "JSR", 0, 1, false);
    startGame(new GameRules(), p1, p2);

    game.endTurn(p2);
  }

  /**
   * Daylimit is 2, so each player can end his turn, but then the game has ended.
   */
  @Test(expected = IllegalStateException.class)
  public void testEndTurnOnLastTurn() {
    buildHardCodedMap(4);
    Player p1 = new Player(0, Color.RED, "Stef", 0, 0, false);
    Player p2 = new Player(1, Color.RED, "JSR", 0, 0, false);
    Player p3 = new Player(2, Color.RED, "Ben", 0, 0, false);
    Player p4 = new Player(3, Color.BLACK, "Joop", 0, 5, false);
    GameRules gameRules = new GameRules();
    gameRules.setDayLimit(2);
    startGame(gameRules, p1, p1, p2, p3, p4);

    game.endTurn();  // p1
    game.endTurn();  // p2
    game.endTurn();  // p3
    game.endTurn();  // p4

    // Game has ended:
    Assert.assertTrue(game.isGameOver());

    // Attempts to end the turn after the game has ended will result in an IllegalStateException
    game.endTurn();
  }

  /**
   * Should not throw any exceptions
   * in other words the happy path
   */
  @Test
  public void testEndTurn() {
    int END_TURN_ITERATIONS = 1500;

    buildHardCodedMap(4);
    Player p1 = new Player(0, Color.RED, "Stef", 0, 0, false);
    Player p2 = new Player(1, Color.GREEN, "Jan", 0, 0, false);
    Player p3 = new Player(2, Color.BLUE, "JSR", 0, 0, false);
    Player p4 = new Player(3, Color.BLACK, "Joop", 0, 1, false);
    startGame(new GameRules(), p1, p2, p3, p4);

    for (int counter = 0; counter < END_TURN_ITERATIONS; counter++) {
      game.endTurn();
    }
  }

  /**
   * Try to Start a game with duplicate players
   */
  @Test(expected = IllegalStateException.class)
  public void testDuplicatePlayers() {
    buildHardCodedMap(5);
    Player p1 = new Player(0, Color.RED, "Stef", 0, 0, false);
    Player p2 = new Player(1, Color.RED, "Jan", 0, 0, false);
    Player p3 = new Player(2, Color.RED, "Jsr", 0, 0, false);
    Player p4 = new Player(3, Color.BLACK, "Joop", 0, 5, false);
    Player p5 = new Player(4, Color.YELLOW, "Kembo", 0, 4, false);
    startGame(new GameRules(), p3, p3, p1, p2, p4, p5);
  }

  @Test
  public void testGameOverByLastPlayerRemaining() {
    buildHardCodedMap(4);
    Player p1 = new Player(0, Color.RED, "Stef", 0, 0, false);
    Player p2 = new Player(1, Color.BLUE, "Jan", 0, 0, false);
    Player p3 = new Player(2, Color.YELLOW, "JSR", 0, 0, false);
    Player p4 = new Player(3, Color.BLACK, "Kembo", 0, 0, false);
    startGame(new GameRules(), p1, p2, p3, p4);

    // Give p2 a unit
    Unit inf2 = UnitFactory.getUnit(TestData.INF);
    MapUtil.addUnitToMap(map, 0, 0, inf2, p2);

    // Give p3 a unit
    Unit inf3 = UnitFactory.getUnit(TestData.INF);
    MapUtil.addUnitToMap(map, 1, 0, inf3, p3);

    // Give p4 a unit
    Unit inf4 = UnitFactory.getUnit(TestData.INF);
    MapUtil.addUnitToMap(map, 2, 0, inf4, p4);

    // p1 slays them all:
    inf2.destroy(true);
    inf3.destroy(true);
    inf4.destroy(true);

    // Game is now over
    Assert.assertTrue(game.isGameOver());
  }

  /**
   * The game ends when only 1 team remains(all other team players are destroyed)
   * There are 2 teams: 1(Stef, JSR) and 2(Ben, Joop)
   */
  @Test
  public void testGameOverByAlliedVictory() {
    buildHardCodedMap(4);
    Player p1 = new Player(0, Color.RED, "Stef", Integer.MAX_VALUE, 1, false);
    Player p2 = new Player(1, Color.BLUE, "JSR", 8500, 1, false);
    Player p3 = new Player(2, Color.GREEN, "Ben", 500, 2, false);
    Player p4 = new Player(3, Color.BLACK, "Joop", 1000, 2, false);
    startGame(new GameRules(), p1, p2, p3, p4);

    // p1 builds up his army
    Unit inf1 = UnitFactory.getUnit(TestData.INF);
    MapUtil.addUnitToMap(map, 0, 0, inf1, p1);

    // p2 builds up his army
    Unit inf2 = UnitFactory.getUnit(TestData.INF);
    MapUtil.addUnitToMap(map, 1, 0, inf2, p2);

    // team 2 comes along and kills all units of team 1
    inf1.destroy(true);
    inf2.destroy(true);

    Assert.assertTrue(p1.isDestroyed());
    Assert.assertTrue(p2.isDestroyed());
    Assert.assertTrue(game.isGameOver());
  }

  /**
   * p1 conquers p2 by capturing the HQ
   * all cities that belonged to p2 are now owned by p1
   * p2 units are destroyed
   */
  @Test
  public void testGameOverByHQCapture() {
    buildHardCodedMap(2);
    Player mapPlayer2 = new Player(1, Color.YELLOW);
    City HQ = MapUtil.addCityToMap(map, 1, 0, TestData.HQ, mapPlayer2);
    mapPlayer2.setHq(HQ);

    Player p1 = new Player(0, Color.RED, "p1", 50, 1, false);
    Player p2 = new Player(1, Color.BLUE, "p2", 50, 2, false);
    startGame(new GameRules(), p1, p2);

    // p1 builds up his army
    Unit inf1 = UnitFactory.getUnit(TestData.INF);
    MapUtil.addUnitToMap(map, 0, 0, inf1, p1);
    game.endTurn();

    // p2 builds up his army
    Unit inf2 = UnitFactory.getUnit(TestData.INF);
    MapUtil.addUnitToMap(map, 1, 0, inf2, p2);
    game.endTurn();

    Assert.assertSame(p1, game.getActivePlayer());

    City player2HQ = p2.getHq();

    // p1 inf1 Captures the HQ of p2
    p2.getHq().capture(inf1);
    p2.getHq().capture(inf1);

    Assert.assertTrue(inf2.isDestroyed());
    Assert.assertTrue(player2HQ.isCapturedBy(inf1));
    Assert.assertSame(p1, player2HQ.getOwner());
    Assert.assertTrue(map.getCityOn(map.getTile(1, 0)).getOwner() == p1);
    Assert.assertTrue(p2.isDestroyed());
    Assert.assertTrue(game.isGameOver());
  }

  /**
   * When the game has started the starting player(p1) contains active units
   * all the other players contain idle units
   */
  @Test
  public void testActiveUnitsAfterGameStart() {
    buildHardCodedMap(5);
    Player mapPlayer1 = new Player(0, colors[0]);
    Player mapPlayer2 = new Player(1, colors[1]);
    Player mapPlayer3 = new Player(2, colors[2]);
    Player mapPlayer4 = new Player(3, colors[3]);
    Player mapPlayer5 = new Player(4, colors[4]);

    // Add some units to each player
    MapUtil.addUnitToMap(map, 0, 0, TestData.INF, mapPlayer1);
    MapUtil.addUnitToMap(map, 1, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 2, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 3, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 4, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 5, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 6, 0, TestData.INF, mapPlayer3);
    MapUtil.addUnitToMap(map, 7, 0, TestData.INF, mapPlayer3);
    MapUtil.addUnitToMap(map, 8, 0, TestData.INF, mapPlayer4);
    MapUtil.addUnitToMap(map, 9, 0, TestData.INF, mapPlayer4);
    MapUtil.addUnitToMap(map, 0, 1, TestData.INF, mapPlayer5);

    Player p1 = new Player(0, Color.RED, "Stef", 0, 0, false);
    Player p2 = new Player(1, Color.BLUE, "Jan", 0, 0, false);
    Player p3 = new Player(2, Color.WHITE, "Jsr", 0, 0, false);
    Player p4 = new Player(3, Color.BLACK, "Joop", 0, 5, false);
    Player p5 = new Player(4, Color.YELLOW, "Kembo", 0, 3, false);
    startGame(new GameRules(), p1, p2, p3, p4, p5);

    // Only player1 has active units
    checkUnitState(p1.getArmy(), GameObjectState.ACTIVE);
    checkUnitState(p2.getArmy(), GameObjectState.IDLE);
    checkUnitState(p3.getArmy(), GameObjectState.IDLE);
    checkUnitState(p4.getArmy(), GameObjectState.IDLE);
    checkUnitState(p5.getArmy(), GameObjectState.IDLE);
  }

  @Test
  public void testPlayerBudgetProgress() {
    Player mapPlayer1 = new Player(0, colors[0]);
    Player mapPlayer2 = new Player(1, colors[1]);

    // Give p1 3 cities
    MapUtil.addCityToMap(map, 0, 1, TestData.BASE, mapPlayer1);
    MapUtil.addCityToMap(map, 0, 2, TestData.BASE, mapPlayer1);
    MapUtil.addCityToMap(map, 0, 3, TestData.BASE, mapPlayer1);

    // Give p2 3 cities
    MapUtil.addCityToMap(map, 0, 4, TestData.BASE, mapPlayer2);
    MapUtil.addCityToMap(map, 0, 5, TestData.BASE, mapPlayer2);
    MapUtil.addCityToMap(map, 0, 6, TestData.BASE, mapPlayer2);

    final int P1_START_BUDGET = 500;
    final int CITY_FUNDS = 1000;
    Player p1 = new Player(0, Color.RED, "Stef", P1_START_BUDGET, 0, false);
    Player p2 = new Player(1, Color.BLUE, "Jan", 0, 1, false);

    GameRules rules = new GameRules();
    rules.setCityFunds(CITY_FUNDS);
    startGame(rules, p1, p2);

    Assert.assertEquals(P1_START_BUDGET + CITY_FUNDS * 3, p1.getBudget());
    Assert.assertEquals(0, p2.getBudget());

    // p1 captures the city @ (0,4) owned by p2
    Unit unit = UnitFactory.getUnit(TestData.INF);
    unit.setOwner(p1);

    City city = map.getCityOn(map.getTile(0, 4));
    city.capture(unit);
    city.capture(unit);

    // p1 now owns 4 cities. p2 only 2
    game.endTurn();
    Assert.assertEquals(CITY_FUNDS * 2, p2.getBudget());

    game.endTurn();
    Assert.assertEquals(P1_START_BUDGET + CITY_FUNDS * 3 + CITY_FUNDS * 4, p1.getBudget());
  }

  @Test
  public void testCapturingNeutralCityStats() {
    buildHardCodedMap(2);
    MapUtil.addCityToMap(map, 0, 0, TestData.BASE, map.getNeutralPlayer());
    MapUtil.addCityToMap(map, 0, 1, TestData.BASE, map.getNeutralPlayer());
    MapUtil.addCityToMap(map, 0, 2, TestData.BASE, new Player(1, colors[1]));
    Unit inf = MapUtil.addUnitToMap(map, 0, 0, TestData.INF, new Player(0, Color.BLACK));

    Player p1 = new Player(0, Color.BLACK, "First player", 0, 1, false);
    Player p2 = new Player(1, Color.RED, "Second player to prevent immediate victory", 0, 2, false);
    startGame(new GameRules(), p1, p2);

    inf.setOwner(p1);
    City city = game.getMap().getCityOn(0, 0);
    city.capture(inf);
    city.capture(inf);
    Assert.assertEquals(1, game.getStats().getNumericStat(0, "cities_captured"));

    city = game.getMap().getCityOn(0, 1);
    city.capture(inf);
    city.capture(inf);
    Assert.assertEquals(2, game.getStats().getNumericStat(0, "cities_captured"));

    city = game.getMap().getCityOn(0, 2);
    city.capture(inf);
    city.capture(inf);
    Assert.assertEquals(3, game.getStats().getNumericStat(0, "cities_captured"));
  }

  // Util Functions

  private void checkUnitState(Iterable<Unit> units, GameObjectState state) {
    for (Unit unit : units) {
      Assert.assertEquals(unit.getState(), state);
    }
  }

  /**
   */
  @Test
  public void testGameCopyAfterPlayerDestroyed() {
    Player mapPlayer1 = new Player(0, colors[0]);
    Player mapPlayer2 = new Player(1, colors[1]);
    Player mapPlayer3 = new Player(2, colors[2]);
    Player mapPlayer4 = new Player(3, colors[3]);
    Player mapPlayer5 = new Player(4, colors[4]);

    // Add some units to each player
    MapUtil.addUnitToMap(map, 0, 0, TestData.INF, mapPlayer1);
    MapUtil.addUnitToMap(map, 1, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 2, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 3, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 4, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 5, 0, TestData.INF, mapPlayer2);
    MapUtil.addUnitToMap(map, 6, 0, TestData.INF, mapPlayer3);
    MapUtil.addUnitToMap(map, 7, 0, TestData.INF, mapPlayer3);
    MapUtil.addUnitToMap(map, 8, 0, TestData.INF, mapPlayer4);
    MapUtil.addUnitToMap(map, 9, 0, TestData.INF, mapPlayer4);
    MapUtil.addUnitToMap(map, 0, 1, TestData.INF, mapPlayer4);
    MapUtil.addUnitToMap(map, 0, 2, TestData.INF, mapPlayer5);

    // Game players
    Player p1 = new Player(0, Color.RED, "Stef", 0, 0, false);
    Player p2 = new Player(1, Color.BLUE, "Jan", 0, 0, false);
    Player p3 = new Player(2, Color.WHITE, "Jsr", 0, 0, false);
    Player p4 = new Player(3, Color.BLACK, "Joop", 0, 5, false);
    Player p5 = new Player(4, Color.ORANGE, "player5", 0, 6, false);

    startGame(new GameRules(), p1, p2, p3, p4, p5);

    Assert.assertEquals("Stef", game.getPlayerByID(0).getName());
    Assert.assertEquals("Jan", game.getPlayerByID(1).getName());
    Assert.assertEquals("Jsr", game.getPlayerByID(2).getName());
    Assert.assertEquals("Joop", game.getPlayerByID(3).getName());
    Assert.assertEquals("player5", game.getPlayerByID(4).getName());

    game.getPlayerByID(2).destroy(map.getNeutralPlayer());

    Assert.assertEquals(4, map.getUniquePlayers().size());

    Game gameCopy = new Game(game);
    Assert.assertEquals("Stef", gameCopy.getPlayerByID(0).getName());
    Assert.assertEquals("Jan", gameCopy.getPlayerByID(1).getName());
    Assert.assertEquals("Jsr", gameCopy.getPlayerByID(2).getName());
    Assert.assertEquals("Joop", gameCopy.getPlayerByID(3).getName());
    Assert.assertEquals(0, gameCopy.getPlayerByID(2).getArmyCount());
  }

  /**
   * Build a map that supports the playerCount
   * Supports means that for each player in the map there should be a game player.
   * When starting a game the game players count is compared to the player count in the map
   * so add a base for each player to the first row of the map.
   */
  private void buildHardCodedMap(int playerCount) {
    int col = 0;
    for (int playerIndex = 0; playerIndex < playerCount; playerIndex++) {
      Player mapPlayer = new Player(playerIndex, colors[playerIndex]);
      MapUtil.addCityToMap(map, col++, 0, TestData.BASE, mapPlayer);
    }
  }

  /**
   * Start a game with the first player as game starter
   */
  private void startGame(GameRules gameRules, Player... players) {
    startGame(players[0], gameRules, players);
  }

  private void startGame(Player gameStarter, GameRules gameRules, Player... players) {
    game = new Game(map, Arrays.asList(players), gameRules);
    game.startGame(gameStarter);
  }

  public static junit.framework.Test suite() {
    return new JUnit4TestAdapter(GameTest.class);
  }
}
