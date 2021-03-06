package com.customwars.client.action.unit;

import com.customwars.client.App;
import com.customwars.client.SFX;
import com.customwars.client.action.DirectAction;
import com.customwars.client.model.GameController;
import com.customwars.client.model.gameobject.Unit;
import com.customwars.client.network.MessageSender;
import com.customwars.client.network.NetworkException;
import com.customwars.client.ui.GUI;
import com.customwars.client.ui.state.InGameContext;
import com.customwars.client.ui.thingle.DialogListener;
import com.customwars.client.ui.thingle.DialogResult;
import org.apache.log4j.Logger;

public class DiveAction extends DirectAction {
  private static final Logger logger = Logger.getLogger(DiveAction.class);
  private InGameContext inGameContext;
  private GameController gameController;
  private MessageSender messageSender;
  private final Unit unit;

  public DiveAction(Unit unit) {
    super("Dive", false);
    this.unit = unit;
  }

  @Override
  protected void init(InGameContext inGameContext) {
    this.inGameContext = inGameContext;
    gameController = inGameContext.getObj(GameController.class);
    messageSender = inGameContext.getObj(MessageSender.class);
  }

  @Override
  protected void invokeAction() {
    if (!inGameContext.isTrapped()) {
      dive();
    }
  }

  private void dive() {
    gameController.dive(unit);
    SFX.playSound("dive");
    if (App.isMultiplayer()) sendDive();
  }

  private void sendDive() {
    try {
      messageSender.dive(unit);
    } catch (NetworkException ex) {
      logger.warn("Could not send dive", ex);
      GUI.askToResend(ex, new DialogListener() {
        public void buttonClicked(DialogResult button) {
          if (button == DialogResult.YES) {
            sendDive();
          }
        }
      });
    }
  }
}
