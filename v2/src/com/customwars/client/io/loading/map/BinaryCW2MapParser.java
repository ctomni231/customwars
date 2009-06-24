package com.customwars.client.io.loading.map;

import com.customwars.client.model.game.Player;
import com.customwars.client.model.gameobject.City;
import com.customwars.client.model.gameobject.CityFactory;
import com.customwars.client.model.gameobject.Locatable;
import com.customwars.client.model.gameobject.Terrain;
import com.customwars.client.model.gameobject.TerrainFactory;
import com.customwars.client.model.gameobject.Unit;
import com.customwars.client.model.gameobject.UnitFactory;
import com.customwars.client.model.map.Location;
import com.customwars.client.model.map.Map;
import com.customwars.client.model.map.Tile;
import tools.Args;
import tools.IOUtil;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Converts a CW2 map objects to and from a binary file.
 * Each Terrain, City and Unit object:
 * has an unique ID: eg. Tank=1 Inf=4 Bomber=403
 * has 1 BASE_ID in the map file: eg. Terrain=0 Unit=1 City=2
 *
 * The bin format has 2 chunks the Header and the map data.
 * The header has 3 chunks:
 * Static, Player and Dynamic
 *
 * STATIC HEADER:
 * <CW2_HEADER_START> txt
 * <COLS> int
 * <ROWS> int
 * <TILE_SIZE> byte
 * <FOG_ON> boolean
 * <MAX_PLAYERS> byte
 *
 * PLAYER HEADER:
 * for each player
 * <PlAYER_ID> byte
 * <PLAYER_RGB> int
 * <PLAYER_HQ_COL> int
 * <PLAYER_HQ_ROW> int
 *
 * DYNAMIC HEADER:
 * <DYNAMIC_HEADER_SIZE> int
 * for each property:
 * <MAP_PROPERTY_NAME> txt
 * <MAP_PROPERTY_VALUE> txt
 *
 * MAP DATA:
 * <TILE COL> int
 * <TILE ROW> int
 *
 * <BASE_TERRAIN_ID> byte (TERRAIN_START_BYTE)
 * <TERRAIN_ID> byte
 * or
 * <BASE_CITY_ID> byte (CITY_START_BYTE)
 * <CITY_ID> byte
 * <OWNER_ID> byte
 *
 * <BASE_UNIT_ID> byte (UNIT_START_BYTE or NO_UNIT)
 * <UNIT_ID> byte
 * <OWNER_ID> byte
 * <TRANSPORT_COUNT> byte
 * for each unit in transport
 * <UNIT_ID>
 * <OWNER_ID>
 *
 * @author stefan
 */
public class BinaryCW2MapParser {
  private static final String CW2_HEADER_START = "CW2.map";
  private static final byte TERRAIN_START = 0;
  private static final byte CITY_START = 1;
  private static final byte UNIT_START = 2;
  private static final byte NO_UNIT = 3;

  public Map<Tile> readMap(File file) throws IOException {
    Args.checkForNull(file);
    if (file.exists()) {
      BinaryMapReader reader = new BinaryMapReader();
      return reader.read(file);
    } else {
      throw new IllegalArgumentException("the file " + file + " does not exist");
    }
  }

  public void writeMap(Map<Tile> map, File file) throws IOException {
    Args.checkForNull(map);
    Args.checkForNull(file);

    if (file.createNewFile()) {
      BinaryMapWriter binMapParser = new BinaryMapWriter(map);
      binMapParser.write(file);
    } else {
      throw new IOException("Could not create file " + file);
    }
  }

  private class BinaryMapReader {
    private DataInputStream in;
    private java.util.Map<Integer, Player> players = new HashMap<Integer, Player>();
    private java.util.Map<Player, Location2D> hqLocations = new HashMap<Player, Location2D>();

    public Map<Tile> read(File file) throws IOException {
      Map<Tile> map = null;

      try {
        this.in = new DataInputStream(new FileInputStream(file));
        try {
          validateFile(in, file);
          map = readMap();
        } catch (IOException ex) {
          throw new MapFormatException(ex);
        }
      } finally {
        IOUtil.closeStream(in);
      }
      return map;
    }

    private void validateFile(DataInputStream in, File file) throws IOException {
      String headerStart = in.readUTF();

      if (!headerStart.equals(CW2_HEADER_START)) {
        throw new MapFormatException("Header doesn't match " + CW2_HEADER_START +
          " File " + file.getName() + " is not a CW2 bin file");
      }
    }

    private Map<Tile> readMap() throws IOException {
      Map<Tile> map = readHeader();
      Map<Tile> loadedMap = readMapData(map);
      return loadedMap;
    }

    private Map<Tile> readHeader() throws IOException {
      Map<Tile> map = readStaticHeader();
      readPlayers(map);
      readDynamicHeader(map);
      return map;
    }

    private Map<Tile> readStaticHeader() throws IOException {
      int cols = in.readInt();
      int rows = in.readInt();
      int tileSize = in.readByte();
      boolean fogOn = in.readBoolean();
      int maxPlayers = in.readByte();

      Terrain plain = TerrainFactory.getTerrain(0);
      return new Map<Tile>(cols, rows, tileSize, maxPlayers, fogOn, plain);
    }

    private void readPlayers(Map<Tile> map) throws IOException {
      for (int i = 0; i < map.getNumPlayers(); i++) {
        int id = in.readByte();
        Color color = new Color(in.readInt());
        int hqCol = in.readInt();
        int hqRow = in.readInt();

        Location2D hqLocation = new Location2D(hqCol, hqRow);
        Player player = new Player(id, color, false, null, "Map player " + id, 0, 0, false);

        players.put(id, player);
        hqLocations.put(player, hqLocation);
      }
    }

    private void readDynamicHeader(Map<Tile> map) throws IOException {
      int dynamicHeaderSize = in.readInt();
      int bytesRead = 0;

      while (bytesRead < dynamicHeaderSize) {
        String property = in.readUTF();
        String value = in.readUTF();
        map.putProperty(property, value);
        bytesRead += (property.getBytes().length + value.getBytes().length);
      }
    }

    private Map<Tile> readMapData(Map<Tile> map) throws IOException {
      while (true) {
        try {
          Tile t = readTile();
          Unit unit;

          if (nextBytesIsUnit()) {
            unit = readUnit();
            t.add(unit);

            // Search for units in transport
            addUnitsToTransport(unit);
          }
          map.setTile(t);
        } catch (EOFException ex) {
          break;
        }
      }

      // After all tiles have been read,
      // read the hq's from the map and add them to the players
      // if the player doesn't have a hq then the hqLocation will be off the map.
      for (Player p : players.values()) {
        Location2D hqLocation = hqLocations.get(p);
        Tile hqLoc = map.getTile(hqLocation);

        if (hqLoc != null) {
          City hq = map.getCityOn(hqLoc);
          p.setHq(hq);
        }
      }
      return map;
    }

    private Tile readTile() throws IOException {
      int col = in.readInt();
      int row = in.readInt();
      Terrain terrain;
      City city = null;

      // Terrain or City
      int terrainBaseID = in.readByte();
      if (terrainBaseID == CITY_START) {
        city = readCity();
        terrain = city;
      } else if (terrainBaseID == TERRAIN_START) {
        terrain = readTerrain();
      } else {
        throw new RuntimeException("tile @ " + col + "," + row + " has an invalid terrain base id " + terrainBaseID);
      }

      Tile tile = new Tile(col, row, terrain);

      if (city != null) {
        city.setLocation(tile);
      }
      return tile;
    }

    private boolean nextBytesIsUnit() throws IOException {
      int unitBaseID = in.readByte();
      if (unitBaseID == UNIT_START) {
        return true;
      } else if (unitBaseID == NO_UNIT) {
        return false;
      } else {
        throw new RuntimeException("unit has an invalid unit base id " + unitBaseID);
      }
    }

    private Terrain readTerrain() throws IOException {
      int terrainID = in.readByte();
      return TerrainFactory.getTerrain(terrainID);
    }

    private City readCity() throws IOException {
      int cityID = in.readByte();
      int ownerID = in.readByte();
      Player owner = players.get(ownerID);
      City city = CityFactory.getCity(cityID);
      city.setOwner(owner);
      return city;
    }

    private Unit readUnit() throws IOException {
      int unitID = in.readByte();
      int unitOwnerID = in.readByte();
      Player owner = players.get(unitOwnerID);

      Unit unit = UnitFactory.getUnit(unitID);
      unit.setOwner(owner);
      return unit;
    }

    private void addUnitsToTransport(Unit transport) throws RuntimeException, IOException {
      int unitsInTransportCount = in.readByte();

      for (int i = 0; i < unitsInTransportCount; i++) {
        int unitBaseID = in.readByte();
        if (unitBaseID == UNIT_START) {
          Unit unit = readUnit();
          in.readByte();  // Skip unit in transport in transport count
          transport.add(unit);
        } else {
          throw new RuntimeException("unit in transport @ " + transport.getLocationString() + " has an invalid unit base id " + unitBaseID);
        }
      }
    }
  }

  private class BinaryMapWriter {
    private Map<Tile> map;
    private DataOutputStream out;

    public BinaryMapWriter(Map<Tile> map) {
      this.map = map;
    }

    public void write(File file) throws IOException {
      try {
        this.out = new DataOutputStream(new FileOutputStream(file));
        writeMap();
      } finally {
        IOUtil.closeStream(out);
      }
    }

    private void writeMap() throws IOException {
      writeHeader();
      writeMapData();
    }

    private void writeHeader() throws IOException {
      writeStaticHeader();
      writePlayers();
      writeProperties();
    }

    /**
     * The static header includes data that is always included in each map file
     */
    private void writeStaticHeader() throws IOException {
      writeTxt(out, CW2_HEADER_START);
      out.writeInt(map.getCols());
      out.writeInt(map.getRows());
      out.writeByte(map.getTileSize());
      out.writeBoolean(map.isFogOfWarOn());
      out.writeByte(map.getNumPlayers());
    }

    /**
     * Write the players to the outputstream
     * including: playerID, color, hq
     */
    private void writePlayers() throws IOException {
      Set<Player> players = getUniquePlayers();

      for (Player p : players) {
        out.writeByte(p.getId());
        out.writeInt(p.getColor().getRGB());
        writeHQ(p);
      }
    }

    private void writeHQ(Player p) throws IOException {
      City hq = p.getHq();
      if (hq != null) {
        out.writeInt(hq.getLocation().getCol());
        out.writeInt(hq.getLocation().getRow());
      } else {
        out.writeInt(-1);
        out.writeInt(-1);
      }
    }

    private Set<Player> getUniquePlayers() {
      Set<Player> players = new HashSet<Player>();
      for (Tile t : map.getAllTiles()) {
        Unit unit = map.getUnitOn(t);
        City city = map.getCityOn(t);

        if (unit != null && !unit.getOwner().isNeutral()) {
          players.add(unit.getOwner());
        }

        if (city != null && !city.getOwner().isNeutral()) {
          players.add(city.getOwner());
        }
      }
      return players;
    }

    private void writeProperties() throws IOException {
      out.writeInt(getPropertiesByteSize());

      for (String property : map.getPropertyKeys()) {
        String value = map.getProperty(property);
        writeTxt(out, property);
        writeTxt(out, value);
      }
    }

    private int getPropertiesByteSize() {
      int headerSize = 0;
      for (String property : map.getPropertyKeys()) {
        String value = map.getProperty(property);
        headerSize += property.getBytes().length;
        headerSize += value.getBytes().length;
      }
      return headerSize;
    }

    private void writeMapData() throws IOException {
      for (Tile t : map.getAllTiles()) {
        writeTile(t);
      }
    }

    private void writeTile(Tile t) throws IOException {
      out.writeInt(t.getCol());
      out.writeInt(t.getRow());

      Terrain terrain = t.getTerrain();
      Unit unit = map.getUnitOn(t);

      if (terrain instanceof City) {
        City city = (City) terrain;
        writeCity(city);
      } else {
        writeTerrain(terrain);
      }

      writeUnit(unit);
    }

    private void writeCity(City city) throws IOException {
      out.writeByte(CITY_START);
      out.writeByte(city.getID());
      out.writeByte(city.getOwner().getId());
    }

    private void writeTerrain(Terrain terrain) throws IOException {
      out.writeByte(TERRAIN_START);
      out.writeByte(terrain.getID());
    }

    private void writeUnit(Unit unit) throws IOException {
      if (unit != null) {
        out.writeByte(UNIT_START);
        out.writeByte(unit.getID());
        out.writeByte(unit.getOwner().getId());
        writeUnitsInTransport(unit);
      } else {
        out.writeByte(NO_UNIT);
      }
    }

    /**
     * If this unit is a transport with units inside it
     * write the transport count followed by
     * a list of the units inside
     *
     * If this unit is not a transport or the transport is empty
     * write 0 as transport count
     */
    private void writeUnitsInTransport(Unit unit) throws IOException {
      int unitsInTransport = unit.getLocatableCount();

      if (unit.canTransport() && unitsInTransport > 0) {
        out.writeByte(unitsInTransport);

        for (int i = 0; i < unitsInTransport; i++) {
          Unit unitInTransport = (Unit) unit.getLocatable(i);
          writeUnit(unitInTransport);
        }
      } else {
        out.writeByte(0);
      }
    }

    private void writeTxt(DataOutputStream out, String txt) throws IOException {
      if (txt == null) {
        txt = "";
        System.out.println("Writing empty txt!");
      }
      out.writeUTF(txt);
    }
  }

  private class Location2D implements Location {

    int col, row;

    public Location2D(int col, int row) {
      this.col = col;
      this.row = row;
    }

    public boolean canAdd(Locatable locatable) {
      return false;
    }

    public void add(Locatable locatable) {
    }

    public boolean remove(Locatable locatable) {
      return false;
    }

    public boolean contains(Locatable locatable) {
      return false;
    }

    public Locatable getLastLocatable() {
      return null;
    }

    public Locatable getLocatable(int index) {
      return null;
    }

    public int getLocatableCount() {
      return 0;
    }

    public int getCol() {
      return col;
    }

    public int getRow() {
      return row;
    }

    public String getLocationString() {
      return col + "," + row;
    }
  }
}