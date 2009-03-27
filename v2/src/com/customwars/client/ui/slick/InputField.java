package com.customwars.client.ui.slick;

import com.customwars.client.ui.state.CWInput;
import org.newdawn.slick.Input;
import org.newdawn.slick.command.Command;
import org.newdawn.slick.command.Control;
import org.newdawn.slick.command.KeyControl;
import org.newdawn.slick.gui.ComponentListener;
import org.newdawn.slick.gui.GUIContext;
import org.newdawn.slick.gui.TextField;

import java.util.List;

/**
 * The InputField class displays the keys mapped to a particular command and allows the user to
 * change the mapped keys. The user selects an InputField by clicking on it, then can press any key or todo (Including the mouse wheel)
 * to change the mapped value.
 */
public class InputField extends TextField {
  private static final int CLEAR_FIELD = Input.KEY_BACK;
  private CWInput cwInput;
  private Command command;
  private int bindingLimit;

  public InputField(GUIContext container, int x, int y, int width, int height, Command command, CWInput cwInput) {
    this(container, x, y, width, height, null, command, cwInput);
  }

  public InputField(GUIContext container, int x, int y, int width, int height, ComponentListener listener, Command command, CWInput cwInput) {
    super(container, container.getDefaultFont(), x, y, width, height, listener);
    this.command = command;
    this.cwInput = cwInput;
    this.bindingLimit = 250;
  }

  /**
   * Set the display text to the names of the controls mapped to command
   * as a ',' separated list
   */
  public void initDisplayText() {
    List controls = cwInput.getControlsFor(command);

    if (controls.isEmpty()) {
      setText("");
    } else {
      setText(getControlsAsText(controls));
    }
  }

  private String getControlsAsText(List controls) {
    String txt = "";
    int bindCount = 0;

    for (Object control : controls) {
      if (control instanceof KeyControl) {
        KeyControl keyControl = (KeyControl) control;
        txt += Input.getKeyName(keyControl.hashCode()) + ", ";
      }

      if (++bindCount >= bindingLimit) {
        break;
      }
    }

    // remove trailing comma
    if (txt.length() > 0) {
      txt = txt.substring(0, txt.length() - 2);
    }
    return txt;
  }

  @Override
  public void keyPressed(int key, char c) {
    if (!hasFocus()) return;

    if (key == CLEAR_FIELD && cwInput.getControlsFor(command).size() > 0) {
      for (Object control : cwInput.getControlsFor(command)) {
        cwInput.unbindCommand((Control) control);
      }
    } else {
      cwInput.bindCommand(new KeyControl(key), command);
    }
    initDisplayText();
  }

  public void setBindingLimit(int bindingLimit) {
    this.bindingLimit = bindingLimit;
  }
}
