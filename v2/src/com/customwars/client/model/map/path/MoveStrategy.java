package com.customwars.client.model.map.path;

import com.customwars.client.model.map.Location;

import java.io.Serializable;

public interface MoveStrategy extends Serializable {
  /**
   * @return the movement cost required to traverse over a particular location.
   */
  public int getMoveCost(Location location);

  /**
   * Create a new instance of this MoveStrategy for the given mover
   */
  MoveStrategy newInstance(Mover mover);
}
