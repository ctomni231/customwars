package com.customwars.client.model.rules;

import com.customwars.client.model.gameobject.City;
import com.customwars.client.model.gameobject.Unit;

public class CityRules {
  /**
   * Supplying has the same conditions as healing
   */
  public boolean canSupply(City city, Unit unit) {
    return city != null && unit != null &&
            canHeal(city, unit);
  }

  /**
   * Can the City heal the unit
   */
  public boolean canHeal(City city, Unit unit) {
    return city != null && unit != null &&
            city.getOwner().isAlliedWith(unit.getOwner()) &&
            city.getLocation() == unit.getLocation() &&
            city.canHeal(unit);
  }

  /**
   * Can the City be captured by the unit
   */
  public boolean canBeCapturedBy(City city, Unit unit) {
    return city != null && unit != null &&
            !city.getOwner().isAlliedWith(unit.getOwner()) &&
            city.canBeCapturedBy(unit) &&
            unit.canCapture() && unit.isActive();
  }
}
