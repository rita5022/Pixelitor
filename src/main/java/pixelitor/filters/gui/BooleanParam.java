/*
 * Copyright 2015 Laszlo Balazs-Csiki
 *
 * This file is part of Pixelitor. Pixelitor is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License, version 3 as published by the Free
 * Software Foundation.
 *
 * Pixelitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pixelitor. If not, see <http://www.gnu.org/licenses/>.
 */

package pixelitor.filters.gui;

import pixelitor.utils.UpdateGUI;

import javax.swing.*;
import java.awt.Rectangle;

import static pixelitor.filters.gui.RandomizePolicy.ALLOW_RANDOMIZE;
import static pixelitor.filters.gui.RandomizePolicy.IGNORE_RANDOMIZE;

/**
 * A filter parameter for a boolean value.
 */
public class BooleanParam extends AbstractFilterParam {
    private final boolean defaultValue;
    private boolean currentValue;
    private final AddDefaultButton addDefaultButton;

    public BooleanParam(String name, boolean defaultValue) {
        this(name, defaultValue, ALLOW_RANDOMIZE);
    }

    public BooleanParam(String name, boolean defaultValue, RandomizePolicy randomizePolicy) {
        this(name, defaultValue, randomizePolicy, AddDefaultButton.NO);
    }

    public BooleanParam(String name, boolean defaultValue, RandomizePolicy randomizePolicy, AddDefaultButton addDefaultButton) {
        super(name, randomizePolicy);
        this.defaultValue = defaultValue;
        currentValue = defaultValue;
        this.addDefaultButton = addDefaultButton;
    }

    @Override
    public JComponent createGUI() {
        BooleanSelector selector = new BooleanSelector(this, addDefaultButton);
        paramGUI = selector;
        setParamGUIEnabledState();

        return selector;
    }

    public static BooleanParam createParamForHPSharpening() {
        return new BooleanParam("High-Pass Sharpening", false, IGNORE_RANDOMIZE);
    }

    @Override
    public boolean isSetToDefault() {
        return (defaultValue == currentValue);
    }

    @Override
    public void reset(boolean triggerAction) {
        setValue(defaultValue, UpdateGUI.YES, triggerAction);
    }

    @Override
    public int getNrOfGridBagCols() {
        return 2;
    }

    @Override
    public void randomize() {
        if (randomizePolicy.allowRandomize()) {
            setValue(Math.random() > 0.5, UpdateGUI.YES, false);
        }
    }

    public boolean isChecked() {
        return currentValue;
    }

    public void setValue(boolean newValue, UpdateGUI updateGUI, boolean trigger) {
        if (currentValue != newValue) {
            currentValue = newValue;
            if (trigger && adjustmentListener != null) {
                adjustmentListener.paramAdjusted();
            }
        }
        if (updateGUI.isYes() && (paramGUI != null)) {
            paramGUI.updateGUI();
        }
    }

    @Override
    public void considerImageSize(Rectangle bounds) {
    }

    @Override
    public boolean canBeAnimated() {
        return false;
    }

    @Override
    public ParamState copyState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setState(ParamState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return String.format("%s[name = '%s', currentValue = %s]",
                getClass().getSimpleName(), getName(), currentValue);
    }

}
