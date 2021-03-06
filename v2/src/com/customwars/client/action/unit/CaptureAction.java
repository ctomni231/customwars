package com.customwars.client.action.unit;

import com.customwars.client.App;
import com.customwars.client.SFX;
import com.customwars.client.action.DirectAction;
import com.customwars.client.model.GameController;
import com.customwars.client.model.gameobject.City;
import com.customwars.client.model.gameobject.Unit;
import com.customwars.client.network.MessageSender;
import com.customwars.client.network.NetworkException;
import com.customwars.client.ui.GUI;
import com.customwars.client.ui.state.InGameContext;
import com.customwars.client.ui.thingle.DialogListener;
import com.customwars.client.ui.thingle.DialogResult;
import org.apache.log4j.Logger;

/**
 * Capture the given City with the given unit
 */
public class CaptureAction extends DirectAction {
  private static final Logger logger = Logger.getLogger(CaptureAction.class);
  private InGameContext inGameContext;
  private GameController gameController;
  private MessageSender messageSender;
  private final Unit unit;
  private final City city;

  public CaptureAction(Unit unit, City city) {
    super("Capture", false);
    this.unit = unit;
    this.city = city;
  }

  protected void init(InGameContext inGameContext) {
    this.inGameContext = inGameContext;
    gameController = inGameContext.getObj(GameController.class);
    messageSender = inGameContext.getObj(MessageSender.class);
  }

  protected void invokeAction() {
    if (!inGameContext.isTrapped()) {
      capture();
    }
  }

  private void capture() {
    boolean captured = gameController.capture(unit, city);

    if (captured) {
      SFX.playSound("captured");
    }
    if (App.isMultiplayer()) sendCapture();
  }

  private void sendCapture() {
    try {
      messageSender.capture(unit, city);
    } catch (NetworkException ex) {
      logger.warn("Could not send capture city", ex);
      GUI.askToResend(ex, new DialogListener() {
        public void buttonClicked(DialogResult button) {
          if (button == DialogResult.YES) {
            sendCapture();
          }
        }
      });
    }
  }
}
