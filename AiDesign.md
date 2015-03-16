# Introduction #

Building the AI for CW2 introduced some problems. I want to write them down here so I'll never forget.

# Cities #
It's all about the cities. Control as much cities as possible and your chances to win increase. The AI must expand rapidly.

# Build AI cheating #
The build AI can see all units in the map. It uses this information to build the unit that can do the highest damage to these units.

# AI VS Ai #
Needs special attention. First the end turn screen is not shown.
Second how can the game be stopped by the user? Check for input at each turn start.

Show a dialog: quit now? Yes/No

# Pathfinding #

The unit advisor returns a destination for each unit.

When a city is in range capture it, if the city is a factory capture that first. Etc...

The problem is that the destination is not always in the move zone.

Or the destination is not reachable.

For example move to enemy hq, capture city behind mountains...

To circumvent these problems the pathfinder needs to build a path accross the whole map.

To do this a path is generated with infinitive movepoints. Yet some destinations are still unreachable.

To allow a unit to find a path through a gateway like a pipe seam and wall seam the movepoints for these cities are reduced.

Less then static blocking terrains like a wall but higher then the movepoints of any unit.
This will make the Ai find the best route towards the destination through the gateway using the Astar algorithm.

The unit will attack the gateway city when it is in range. Units that can't attack will be trapped before the gateway.

Better trapped near the destination then on your own factory.

If the unit can really not move towards the destination find a tile around it that can be reached.

If all this fails move to the tile in the movezone closest to the closest enemy.

If there are no enemies move to the Hq

Else if we are on a factory get off!

# Map copy #
The Ai performs his moves in a copy of the map. The coordinates are stored in orders. These orders are then executed in the gui.

An example of an order could be: join 0,0 2,0

1 big advantage is that the Ai can be **tested without ui**.

A new copy is created on each start of a turn.

The Ai must use the pathfinder in the map to make sure he is trapped in fog of war and to make sure he cannot move outside his movezone.

Note of the dev make sure to return the order destination and not the location just before the trap.

So the pathfinder is used 2 times. First to find the destination. Then to move the unit in the map towards the destination within his movezone.

The Ai map and the "real" map must always be equal or strange things will happen. An action in the map copy should not have any effect in the real map.

# Seperation of concerns #
The decision making progress should be decoupled from the execution of that decision. In CW2 this is done by having advisors.

For example the build AI advisor generates a build priority, it's up to the build AI to do something smart with this information.

Advisors can be created for each AI difficulty level.

# Destroying units #
When an AI unit is destroyed we need to make sure not to evaluate this unit anymore in the unit loop.