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

package pixelitor.tools;

import pixelitor.Composition;
import pixelitor.FgBgColors;
import pixelitor.ImageDisplay;
import pixelitor.layers.ImageLayer;
import pixelitor.utils.ColorUtils;

import javax.swing.*;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;

/**
 * The brush tool
 */
public class BrushTool extends TmpLayerBrushTool {
    private Color drawingColor;

    public BrushTool() {
        super('b', "Brush", "brush_tool_icon.png",
                "click and drag to draw with the current brush, Shift-click to draw lines, right-click to draw with the background color",
                Cursor.getDefaultCursor()
        );
    }

    @Override
    public void initSettingsPanel() {
        addTypeSelector();
        addBrushSettingsButton();
        settingsPanel.addSeparator();
        addSizeSelector();
        addSymmetryCombo();

        settingsPanel.addSeparator();

        addBlendingModePanel();
    }

    @Override
    public void mousePressed(MouseEvent e, ImageDisplay ic) {
        setupDrawingColor(e);
        super.mousePressed(e, ic);
    }

    @Override
    protected void createGraphicsForNewBrushStroke(Composition comp, ImageLayer layer) {
        super.createGraphicsForNewBrushStroke(comp, layer);

        // reinitialize the color for each stroke
        graphics.setColor(drawingColor);
    }

    @Override
    protected void prepareProgrammaticBrushStroke(Composition comp, Point start) {
        super.prepareProgrammaticBrushStroke(comp, start);
        graphics.setColor(FgBgColors.getFG());
    }

    private void setupDrawingColor(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            drawingColor = FgBgColors.getBG();
        } else if (SwingUtilities.isMiddleMouseButton(e)) {
            // TODO we never get here because isAltDown is always true for middle-button events, even if Alt is not pressed?
            // See source comment in java.awt.Event for ALT_MASK
            Color fg = FgBgColors.getFG();
            Color bg = FgBgColors.getBG();
            if(e.isControlDown()) {
                drawingColor = ColorUtils.getHSBAverageColor(fg, bg);
            } else {
                drawingColor = ColorUtils.getRGBAverageColor(fg, bg);
            }
        } else {
            drawingColor = FgBgColors.getFG();
        }
    }

    @Override
    public void trace(Composition comp, Shape shape) {
        if (drawingColor == null) {
            drawingColor = FgBgColors.getFG();
        }
        super.trace(comp, shape);
    }
}