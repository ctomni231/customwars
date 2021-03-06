package com.customwars.client.io.converter;

import com.customwars.client.model.gameobject.Terrain;
import com.customwars.client.model.gameobject.TerrainConnection;
import com.customwars.client.model.map.Direction;
import com.thoughtworks.xstream.core.util.Fields;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class contains code that is shared between converters
 */
public class ConvertUtil {

  /**
   * Convert the xml '<connect type='baseTerrain'>North, East</connect>' to a TerrainConnection
   */
  public static TerrainConnection readConnectionNode(HierarchicalStreamReader reader, Terrain baseTerrain) {
    List<Direction> connections = null;
    String connectsTo = null;

    while (reader.hasMoreChildren()) {
      reader.moveDown();
      final String nodeName = reader.getNodeName();

      if (nodeName.equals("connect")) {
        connectsTo = reader.getAttribute("type");
        connections = readDirections(reader.getValue());
        reader.moveUp();
      } else {
        throw new IllegalArgumentException(
          "Could not find 'connect' node. Instead I found " + nodeName + " but that's not what i'm looking for."
        );
      }
    }

    if (connectsTo == null) {
      connectsTo = baseTerrain.getType();
    }
    return new TerrainConnection(connectsTo, connections);
  }

  private static List<Direction> readDirections(String directions) {
    List<Direction> connects = new ArrayList<Direction>();
    StringTokenizer tokenizer = new StringTokenizer(directions, ",");
    while (tokenizer.hasMoreTokens()) {
      Direction direction = Direction.getDirection(tokenizer.nextToken());
      connects.add(direction);
    }
    return connects;
  }

  /**
   * Use Reflection to write a value to a field within Object obj of the given class aClass
   * The field can be private final
   *
   * @param fieldName The name of the field to write val to
   * @param obj       The object where we want to write to
   * @param aClass    The type of obj where the field is stored in
   * @param val       The value to write to the field
   */
  public static void writeField(String fieldName, Object obj, Class aClass, Object val) {
    Field field = Fields.find(aClass, fieldName);
    Fields.write(field, obj, val);
  }
}
