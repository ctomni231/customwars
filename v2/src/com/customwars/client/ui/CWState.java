package com.customwars.client.ui;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.command.Command;
import org.newdawn.slick.command.InputProviderListener;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 * A state that CW can be in, examples MainMenu, EndTurn, InGame
 * each state can listen for input commands(Select, Cancel, menuUp,menuDown etc)
 * by implementing controlPressed(Command command)
 *
 * We extend BasicGameState to add cw specific functionality.
 *
 * @author stefan
 */
public abstract class CWState extends BasicGameState implements InputProviderListener {

  private StateLogic statelogic = new StateLogic();
    
  public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
  }

  public void controlReleased(Command command) {
  }
  
  //initializes the statelogic class (no overhead)
  public void init(StateBasedGame game, AppGameContainer container){
      statelogic.initialize(game, container);
  }
  
  //changes a game state (no overhead)
  public void changeGameState(int index){
      statelogic.changeGameState(index);
  }
  
  //Overwrite function below to change states
  //numbers < 0 == remain on current state...
  public int setState(){
      return -1;
  }
}
