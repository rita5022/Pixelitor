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

package pixelitor.utils.test;

import pixelitor.Composition;
import pixelitor.ImageComponents;
import pixelitor.ImageDisplay;
import pixelitor.NewImage;
import pixelitor.tools.AbstractBrushTool;
import pixelitor.tools.GradientTool;
import pixelitor.tools.GradientType;
import pixelitor.tools.MoveTool;
import pixelitor.tools.ShapeType;
import pixelitor.tools.Tools;
import pixelitor.tools.UserDrag;
import pixelitor.tools.shapestool.ShapesTool;

import java.awt.AlphaComposite;
import java.awt.Point;

import static java.awt.MultipleGradientPaint.CycleMethod.REFLECT;
import static pixelitor.FillType.WHITE;
import static pixelitor.tools.GradientColorType.BLACK_TO_WHITE;

/**
 *
 */
public class ToolTests {

    /**
     * Utility class with static methods
     */
    private ToolTests() {
    }

    public static void testTools() {
        NewImage.addNewImage(WHITE, 400, 400, "Tool Tests");

        ImageDisplay ic = ImageComponents.getActiveIC();
        Composition comp = ic.getComp();

        addRadialBWGradientToActiveLayer(comp, true);

        int xDistanceFormEdge = 20;
        int yDistanceFormEdge = 20;

        // erase diagonally
        paintDiagonals(Tools.ERASER, comp, xDistanceFormEdge, yDistanceFormEdge);

        // paint a frame
        paintImageFrame(Tools.BRUSH, comp, xDistanceFormEdge, yDistanceFormEdge);

        paintHeartShape(comp);

        MoveTool.move(comp, 40, 40);

        ic.repaint();
    }

    private static void paintHeartShape(Composition comp) {
        ShapesTool shapesTool = Tools.SHAPES;
        int canvasWidth = comp.getCanvasWidth();
        int canvasHeight = comp.getCanvasHeight();

        UserDrag userDrag = new UserDrag((int) (canvasWidth * 0.25), (int) (canvasHeight * 0.25), (int) (canvasWidth * 0.75), (int) (canvasHeight * 0.75));

        shapesTool.setShapeType(ShapeType.HEART);
        shapesTool.paintShapeOnIC(comp, userDrag);
    }

    private static void paintDiagonals(AbstractBrushTool eraseTool, Composition comp, int xDistanceFormEdge, int yDistanceFormEdge) {
        int canvasWidth = comp.getCanvasWidth();
        int canvasHeight = comp.getCanvasHeight();
        Point topLeft = new Point(xDistanceFormEdge, yDistanceFormEdge);
        Point topRight = new Point(canvasWidth - xDistanceFormEdge, yDistanceFormEdge);
        Point bottomRight = new Point(canvasWidth - xDistanceFormEdge, canvasHeight - yDistanceFormEdge);
        Point bottomLeft = new Point(xDistanceFormEdge, canvasHeight - yDistanceFormEdge);
        eraseTool.drawBrushStrokeProgrammatically(comp, topLeft, bottomRight);
        eraseTool.drawBrushStrokeProgrammatically(comp, topRight, bottomLeft);
    }

    private static void paintImageFrame(AbstractBrushTool brushTool, Composition comp, int xDistanceFormEdge, int yDistanceFormEdge) {
        int canvasWidth = comp.getCanvasWidth();
        int canvasHeight = comp.getCanvasHeight();
        Point topLeft = new Point(xDistanceFormEdge, yDistanceFormEdge);
        Point topRight = new Point(canvasWidth - xDistanceFormEdge, yDistanceFormEdge);
        Point bottomRight = new Point(canvasWidth - xDistanceFormEdge, canvasHeight - yDistanceFormEdge);
        Point bottomLeft = new Point(xDistanceFormEdge, canvasHeight - yDistanceFormEdge);
        brushTool.drawBrushStrokeProgrammatically(comp, topLeft, topRight);
        brushTool.drawBrushStrokeProgrammatically(comp, topRight, bottomRight);
        brushTool.drawBrushStrokeProgrammatically(comp, bottomRight, bottomLeft);
        brushTool.drawBrushStrokeProgrammatically(comp, bottomLeft, topLeft);
    }

    public static void addRadialBWGradientToActiveLayer(Composition comp, boolean radial) {
        int canvasWidth = comp.getCanvasWidth();
        int canvasHeight = comp.getCanvasHeight();

        int startX = canvasWidth / 2;
        int startY = canvasHeight / 2;

        int endX = 0;
        int endY = 0;
        if (canvasWidth > canvasHeight) {
            endX = startX;
        } else {
            endY = startY;
        }

        GradientType gradientType;

        if (radial) {
            gradientType = GradientType.RADIAL;
        } else {
            gradientType = GradientType.SPIRAL_CW;
        }

        GradientTool.drawGradient(comp.getActiveMaskOrImageLayer(),
                gradientType,
                BLACK_TO_WHITE,
                REFLECT,
                AlphaComposite.SrcOver,
                new UserDrag(startX, startY, endX, endY),
                false);
    }


}
