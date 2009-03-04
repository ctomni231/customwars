package com.customwars.client.model.gameobject;

import com.customwars.client.model.TurnHandler;
import com.customwars.client.model.game.Player;
import com.customwars.client.model.map.Direction;
import com.customwars.client.model.map.Location;
import com.customwars.client.model.map.UnitFight;
import com.customwars.client.model.map.path.MoveStrategy;
import com.customwars.client.model.map.path.Mover;
import tools.Args;

import java.util.LinkedList;
import java.util.List;

/**
 * Is a Mover meaning it has the ability to be put on and removed from a Location.
 * Is a Location, meaning it can transport Locatables
 * It has a Facing direction, this is one of the compass Direction(N,S,E,W)
 * Units have 2 weapons(both are optional): 1 Primary and 1 Secundary
 *
 * @author Stefan
 */
public class Unit extends GameObject implements Mover, Location, TurnHandler {
  public static final Direction DEFAULT_ORIENTATION = Direction.EAST;
  private static final int MAX_EXP = 10;
  private int id;               // The unit Type ie(1->INF, 2->APC,...)
  private String name;          // Full name ie Infantry, Tank, ...
  private String description;   // Information about this Unit
  private int price;            // The price for buying this Unit
  private int movement;         // The amount of tiles this unit may move per turn.
  private int vision;           // The amount of tiles this unit can see in all directions aka line of sight

  private int maxHp;              // The value when this unit is 100% Healthy
  private int minHealRange, maxHealRange; // Range in which we can heal
  private int maxSupplies;        // The value when this unit has 100% supplies
  private int maxTransportCount;  // Amount of units that can be transported
  private int dailyUse;           // Amount of supplies that are subtracted each turn

  private boolean canCapture;   // Abilities
  private boolean canDive;
  private boolean canSupply;
  private boolean canHeal;
  private boolean canTransport;

  private int armyBranch;       // Naval, Ground, Air
  private int movementType;     // Inf, Mech, Tires, Tread, Air, Naval ...
  private MoveStrategy moveStrategy;

  private int hp;               // Health Points, if 0 the unit is dead
  private int supplies;         // Each unit has supplies, this can be in the form of rations(troops) or fuel(motorized vehicles)
  private int experience;       // Each time a unit wins a fight his Experience rises(starting from 0 to 10)
  private int unitState;        // Current Unitstate (under water, capturing a property,...)
  private Player owner;         // Player owning this unit
  private Location location;    // current Location
  private List<Location> moveZone;  // A zone where this unit can move in
  private List<Location> attZone;   // A zone where this unit can attack in
  private Direction orientation;    // The direction this unit is looking at
  private boolean hidden;           // In fog of war, is this unit visible within enemy moveZone

  private Weapon primaryWeapon, secondaryWeapon;
  private List<Locatable> transport;      // Units that are within this Transport(null means can't transport)
  private List<Integer> transportTypes;   // Movement Types that can be transported

  public Unit(int id, String name, String description,
              int cost, int movement, int vision,
              int maxHp, int maxSupplies, int maxTransportCount, int suppliesPerTurn,
              boolean canCapture, boolean canDive, boolean canSupply, boolean canHeal, boolean canTransport, List<Integer> transportTypes,
              int armyBranch, int movementType, int minHealRange, int maxHealRange) {
    super(GameObjectState.ACTIVE);
    this.id = id;
    this.name = name;
    this.description = description;
    this.price = cost;
    this.movement = movement;
    this.vision = vision;

    this.maxHp = maxHp;
    this.maxSupplies = maxSupplies;
    this.maxTransportCount = maxTransportCount;
    this.dailyUse = suppliesPerTurn;

    this.canCapture = canCapture;
    this.canDive = canDive;
    this.canSupply = canSupply;
    this.canHeal = canHeal;
    this.canTransport = canTransport;
    this.transportTypes = transportTypes;

    this.armyBranch = armyBranch;
    this.movementType = movementType;
    this.minHealRange = minHealRange;
    this.maxHealRange = maxHealRange;
    init();
    reset();
  }

  void init() {
    if (canTransport) this.transport = new LinkedList<Locatable>();
  }

  /**
   * Copy Constructor
   *
   * @param otherUnit unit to copy
   */
  public Unit(Unit otherUnit) {
    super(otherUnit);
    hp = otherUnit.hp;
    supplies = otherUnit.supplies;
    experience = otherUnit.experience;
    unitState = otherUnit.unitState;

    name = otherUnit.name;
    id = otherUnit.id;
    price = otherUnit.price;
    movement = otherUnit.movement;
    vision = otherUnit.vision;

    maxHp = otherUnit.maxHp;
    minHealRange = otherUnit.minHealRange;
    maxHealRange = otherUnit.maxHealRange;
    maxSupplies = otherUnit.maxSupplies;
    maxTransportCount = otherUnit.maxTransportCount;
    dailyUse = otherUnit.dailyUse;

    canCapture = otherUnit.canCapture;
    canDive = otherUnit.canDive;
    canSupply = otherUnit.canSupply;
    canHeal = otherUnit.canHeal;
    transportTypes = otherUnit.transportTypes;

    armyBranch = otherUnit.armyBranch;
    movementType = otherUnit.movementType;
    moveStrategy = otherUnit.moveStrategy;

    // Don't copy the reference,
    // instead create a new copy of the weapon
    primaryWeapon = otherUnit.primaryWeapon != null ? new Weapon(otherUnit.primaryWeapon) : null;
    secondaryWeapon = otherUnit.secondaryWeapon != null ? new Weapon(otherUnit.secondaryWeapon) : null;
    experience = otherUnit.experience;

    location = otherUnit.location;
    owner = otherUnit.owner;
    description = otherUnit.description;
    transport = otherUnit.transport;
  }

  /**
   * Resets the unit state to default:
   * 100% Supplies, health
   * 100% ammo in both weapons(or 1 or none)
   * Facing east, Idle state, no exp
   */
  void reset() {
    restock();
    resupply();
    experience = 0;
    setOrientation(DEFAULT_ORIENTATION);
    setState(GameObjectState.ACTIVE);
  }

  /**
   * Removes all references to this unit
   * so that it can be garbage collected
   */
  public void destroy() {
    owner.removeUnit(this);
    clearTransport();
    location.remove(this);
    setState(GameObjectState.DESTROYED);
  }

  public void startTurn(Player player) {
  }

  public void endTurn(Player invoker) {
    addSupplies(-dailyUse);
  }

  /**
   * Clear and destroys every unit in the transport
   */
  public void clearTransport() {
    if (canTransport()) {
      for (Locatable locatable : transport) {
        if (locatable instanceof Unit) {
          Unit unit = (Unit) locatable;
          unit.destroy();
        }
      }
      transport.clear();
    }
  }

  /**
   * Restore
   * supplies, hp to the maximum
   */
  private void resupply() {
    hp = maxHp;
    supplies = maxSupplies;
  }

  // ----------------------------------------------------------------------------
  // Actions :: weapons
  // ----------------------------------------------------------------------------
  public void restock() {
    if (hasPrimaryWeapon()) {
      primaryWeapon.restock();
    }
    if (hasSecondaryWeapon()) {
      secondaryWeapon.restock();
    }
  }

  public void fireWeapon() {
    fireWeapon(1);
  }

  /**
   * When a unit fires the shots are subtracted from the ammoCount in weapon.
   * Some units cannot fire, they should have set primaryWeapon and secondaryWeapon to null.
   *
   * @param shots amount of bullets or misiles or shells the available weapon will fire
   * @see Weapon
   */
  public void fireWeapon(int shots) {
    Weapon weapon = getAvailableWeapon();
    if (weapon != null) {
      weapon.fire(shots);
    }
  }

  // ----------------------------------------------------------------------------
  // Actions :: Attack/Defend
  // ----------------------------------------------------------------------------
  /**
   * This unit is the attacker
   * and attacking the defender
   */
  public void attack(Unit defender, UnitFight fight) {
    if (canAttack(defender)) {
      defender.defend(this, fight);
      if (defender.isDestroyed()) {
        if (++experience > MAX_EXP) {
          experience = MAX_EXP;
        }
      }
    }
  }

  /**
   * A unit can attack if
   * 1 of the two weapons are set and have >0 ammo.
   */
  public boolean canAttack(Unit unit) {
    return unit != null && canFire();
  }

  public void defend(Unit attacker, UnitFight fight) {
    receiveDamage(attacker, fight);

    if (fight.canCounterAttack(attacker, this)) {
      fight.counterAttack(attacker);
    }

    if (hp <= 0) {
      destroy();
    }
  }

  /**
   * This unit is the defender
   * and receives damage from a Fight.
   */
  public void receiveDamage(Unit attacker, UnitFight fight) {
    int attackValue = fight.calcAttackDamage();
    addHp(-attackValue);
  }

  /**
   * We can counter Attack when we didn't die from the attack.
   */
  public boolean canCounterAttack() {
    return !isDestroyed();
  }

  // ---------------------------------------------------------------------------
  // Actions :: Transport
  // ---------------------------------------------------------------------------
  public int getCol() {
    return location.getCol();
  }

  public int getRow() {
    return location.getRow();
  }

  public Locatable getLastLocatable() {
    return getLocatable(transport.size() - 1);
  }

  public Locatable getLocatable(int id) {
    if (id > 0 || id < transport.size()) {
      return transport.get(id);
    } else {
      return null;
    }
  }

  public boolean remove(Locatable locatable) {
    return transport.remove(locatable);
  }

  public boolean contains(Locatable locatable) {
    return transport.contains(locatable);
  }

  public void add(Locatable locatable) {
    if (canAdd(locatable)) {
      transport.add(locatable);
    }
  }

  public boolean canAdd(Locatable locatable) {
    return canTransport() && locatable != null;
  }

  /**
   * @return Amount of units in the transport
   */
  public int getLocatableCount() {
    return transport.size();
  }

  // ----------------------------------------------------------------------------
  // Actions :: supply, heal, capture
  // ----------------------------------------------------------------------------
  public void supply(Unit unit) {
    if (canSupply(unit))
      unit.addHp(hp);
  }

  public boolean canSupply(Unit unit) {
    return unit != null && canSupply;
  }

  public void heal(Unit unit) {
    if (canHeal(unit)) {
      unit.addHp(hp);
    }
  }

  private boolean canHeal(Unit unit) {
    return unit != null && canHeal;
  }

  // ---------------------------------------------------------------------------
  // Setters
  // ---------------------------------------------------------------------------
  public void setPrimaryWeapon(Weapon priWeapon) {
    Weapon oldVal = this.primaryWeapon;
    primaryWeapon = priWeapon;
    firePropertyChange("primaryWeapon", oldVal, orientation);
  }

  public void setSecondaryWeapon(Weapon secWeapon) {
    Weapon oldVal = this.secondaryWeapon;
    secondaryWeapon = secWeapon;
    firePropertyChange("secondaryWeapon", oldVal, orientation);
  }

  public void setOrientation(String orientation) {
    Direction dir = Direction.getDirection(orientation);
    setOrientation(dir);
  }

  public void setOrientation(Direction orientation) {
    Direction oldVal = this.orientation;
    this.orientation = orientation;
    firePropertyChange("orientation", oldVal, orientation);
  }

  public void setOwner(Player owner) {
    Player oldVal = this.owner;
    this.owner = owner;
    firePropertyChange("owner", oldVal, owner);
  }

  public void addHp(int additionalHp) {
    setHp(hp + additionalHp);
  }

  protected void setHp(int hp) {
    int oldVal = this.hp;
    int validHp = Args.getBetweenZeroMax(hp, maxHp);
    this.hp = validHp;
    firePropertyChange("hp", oldVal, validHp);
  }

  public void addSupplies(int additionalSupplies) {
    setSupply(supplies + additionalSupplies);
  }

  protected void setSupply(int amount) {
    int oldVal = this.supplies;
    int validSupply = Args.getBetweenZeroMax(amount, maxSupplies);
    this.supplies = validSupply;
    firePropertyChange("supplies", oldVal, validSupply);
  }

  public void setLocation(Location newLocation) {
    Location oldLocation = this.location;
    this.location = newLocation;
    firePropertyChange("location", oldLocation, newLocation);
  }

  public void setMoveZone(List<Location> moveZone) {
    List<Location> oldVal = this.moveZone;
    this.moveZone = moveZone;
    firePropertyChange("moveZone", oldVal, moveZone);
  }

  public void setAttackZone(List<Location> attackZone) {
    List<Location> oldVal = this.attZone;
    this.attZone = attackZone;
    firePropertyChange("attackZone", oldVal, attackZone);
  }

  public void setPrice(int price) {
    int oldVal = this.price;
    this.price = price;
    firePropertyChange("price", oldVal, price);
  }

  public void setUnitState(int unitState) {
    int oldVal = this.unitState;
    this.unitState = unitState;
    firePropertyChange("unitState", oldVal, unitState);
  }

  public void setHidden(boolean hidden) {
    boolean oldVal = this.hidden;
    this.hidden = hidden;
    firePropertyChange("hidden", oldVal, hidden);
  }

  public void setMoveStrategy(MoveStrategy moveStrategy) {
    MoveStrategy oldVal = this.moveStrategy;
    this.moveStrategy = moveStrategy;
    firePropertyChange("moveStrategy", oldVal, moveStrategy);
  }

  // ---------------------------------------------------------------------------
  // Getters :: Weapon
  // ---------------------------------------------------------------------------
  /**
   * @return The first available weapon
   *         this means it is not null and has some ammo left
   *         starting with the primary weapon
   */
  public Weapon getAvailableWeapon() {
    if (hasPrimaryWeapon() && primaryWeapon.getAmmo() > 0) {
      return primaryWeapon;
    } else if (hasSecondaryWeapon() && secondaryWeapon.getAmmo() > 0) {
      return secondaryWeapon;
    } else {
      return null;
    }
  }

  public Weapon getPrimaryWeapon() {
    return primaryWeapon;
  }

  public Weapon getSecondaryWeapon() {
    return secondaryWeapon;
  }

  /**
   * @return if one of the two weapons can be fired
   */
  public boolean canFire() {
    boolean canfire = false;

    if (hasPrimaryWeapon() && primaryWeapon.getAmmo() > 0) {
      canfire = true;
    } else if (hasSecondaryWeapon() && secondaryWeapon.getAmmo() > 0) {
      canfire = true;
    }
    return canfire;
  }

  public boolean hasPrimaryWeapon() {
    return primaryWeapon != null;
  }

  public boolean hasSecondaryWeapon() {
    return secondaryWeapon != null;
  }

  public int getMinAttackRange() {
    Weapon weapon = getAvailableWeapon();
    if (weapon != null) {
      return weapon.getMinRange();
    } else {
      return 0;
    }
  }

  public int getMaxAttackRange() {
    Weapon weapon = getAvailableWeapon();
    if (weapon != null) {
      return weapon.getMaxRange();
    } else {
      return 0;
    }
  }

  // ---------------------------------------------------------------------------
  // Getters :: Supplies, hp
  // ---------------------------------------------------------------------------
  protected int getSupplies() {
    return supplies;
  }

  public int getSuppliesPercentage() {
    int percentage;
    if (maxSupplies <= 0) {
      percentage = 100;
    } else {
      double divide = (double) supplies / maxSupplies;
      percentage = (int) Math.round(divide * 100);
    }
    return percentage;
  }

  public boolean hasLowSupplies() {
    return getSuppliesPercentage() <= 20;
  }

  public int getHp() {
    return hp;
  }

  public int getMaxHp() {
    return maxHp;
  }

  public int getHpPercentage() {
    int percentage;
    if (maxHp <= 0) {
      percentage = 100;
    } else {
      double divide = (double) hp / maxHp;
      percentage = (int) Math.round(divide * 100);
    }
    return percentage;
  }

  public boolean hasLowHp() {
    return getHpPercentage() < 100;
  }

  // ---------------------------------------------------------------------------
  // Getters :: Moving
  // ---------------------------------------------------------------------------
  public Location getLocation() {
    return location;
  }

  public int getMovementType() {
    return movementType;
  }

  public MoveStrategy getMoveStrategy() {
    return moveStrategy;
  }

  public int getMovement() {
    return movement;
  }

  public void addPathMoveCost(int moveCost) {
    addSupplies(-moveCost);
  }

  public List<Location> getMoveZone() {
    return moveZone;
  }

  public List<Location> getAttackZone() {
    return attZone;
  }

  // ---------------------------------------------------------------------------
  // Getters :: Other
  // ---------------------------------------------------------------------------
  public int getID() {
    return id;
  }

  public String getName() {
    return name;
  }

  public int getArmyBranch() {
    return armyBranch;
  }

  public int getVision() {
    return vision;
  }

  public int getPrice() {
    return price;
  }

  public Player getOwner() {
    return owner;
  }

  public int getUnitState() {
    return unitState;
  }

  public boolean isWithinMoveZone(Location location) {
    return moveZone.contains(location);
  }

  public boolean isWithinAttackZone(Location location) {
    return attZone.contains(location);
  }

  public boolean hasLowAmmo() {
    boolean lowAmmo = false;
    if (hasPrimaryWeapon()) {
      if (primaryWeapon.getAmmoPercentage() < 20) {
        lowAmmo = true;
      }
    } else if (hasSecondaryWeapon()) {
      if (secondaryWeapon.getAmmoPercentage() < 20) {
        lowAmmo = true;
      }
    }
    return lowAmmo;
  }

  public boolean isHidden() {
    return hidden;
  }

  public boolean canCapture() {
    return canCapture;
  }

  public boolean canDive() {
    return canDive;
  }

  public boolean canTransport(int id) {
    return canTransport() && transportTypes.contains(id);
  }

  public boolean canTransport() {
    return canTransport && transport.size() - 1 < maxTransportCount;
  }

  public int getMinHealRange() {
    return minHealRange;
  }

  public int getMaxHealRange() {
    return maxHealRange;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder("[name=" + name + " id=" + id);
    if (location != null)
      strBuilder.append(" location=(").append(location.getCol()).append(",").append(location.getRow()).append(")");
    if (owner != null) strBuilder.append(" owner=").append(owner);
    strBuilder.append("]");
    return strBuilder.toString();
  }
}
