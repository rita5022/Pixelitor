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

package pixelitor.filters;

import pixelitor.filters.gui.GroupedRangeParam;
import pixelitor.filters.gui.IntChoiceParam;
import pixelitor.filters.gui.RangeParam;

import java.awt.geom.Path2D;

import static net.jafama.FastMath.cos;
import static net.jafama.FastMath.sin;
import static pixelitor.filters.gui.RandomizePolicy.IGNORE_RANDOMIZE;

/**
 * Spirograph curve
 */
public class Spirograph extends ShapeFilter {
    private static final int TYPE_HYPOTROCHOID = 1;
    private static final int TYPE_EPITROCHOID = 2;

    private static final int PROPORTIONS_CIRCLE = 3;
    private static final int PROPORTIONS_IMAGE = 4;

    private final RangeParam time = new RangeParam("Time", 0, 185, 1000);
    private final GroupedRangeParam radii = new GroupedRangeParam("Radii",
            new RangeParam[]{
                    new RangeParam("r", 1, 224, 500),
                    new RangeParam("R", 1, 114, 500),
                    new RangeParam("d", 0, 189, 500)
            }, false);
    private final IntChoiceParam type = new IntChoiceParam("Type",
            new IntChoiceParam.Value[]{
                    new IntChoiceParam.Value("Hypotrochoid", TYPE_HYPOTROCHOID),
                    new IntChoiceParam.Value("Epitrochoid", TYPE_EPITROCHOID),
            }, IGNORE_RANDOMIZE);

    private final IntChoiceParam proportions = new IntChoiceParam("Proportions",
            new IntChoiceParam.Value[]{
                    new IntChoiceParam.Value("Circle", PROPORTIONS_CIRCLE),
                    new IntChoiceParam.Value("Image", PROPORTIONS_IMAGE),
            }, IGNORE_RANDOMIZE);

    private final RangeParam zoom = new RangeParam("Zoom (%)", 1, 100, 1000);

    public Spirograph() {
        addParamsToFront(
                time,
                radii.setShowLinkedCB(false),
                type,
                proportions,
                zoom
        );
    }

    @Override
    protected Path2D createShape(int width, int height) {
        Path2D shape = new Path2D.Double();

        double r = radii.getValueAsDouble(0);
        double R = radii.getValueAsDouble(1);
        double d = radii.getValueAsDouble(2);

        double z = zoom.getValueAsDouble() / 100.0;
        r *= z;
        R *= z;
        d *= z;

        double w = width / 2.0;
        double h = height / 2.0;

        double maxValue = time.getValue();
        if (maxValue == 0.0) {
            return null;
        }

        double combinedR;
        if (type.getValue() == TYPE_HYPOTROCHOID) {
            combinedR = R - r;
        } else if (type.getValue() == TYPE_EPITROCHOID) {
            combinedR = R + r;
        } else {
            throw new IllegalStateException();
        }

        boolean followImage = proportions.getValue() == PROPORTIONS_IMAGE;
        double xMultiplier = 1.0;
        double yMultiplier = 1.0;
        if (followImage) {
            if (width > height) {
                xMultiplier = ((double) width) / height;
            } else {
                yMultiplier = ((double) height) / height;
            }
        }

        double dt = 0.05;
        double startX = combinedR + d;
        double startY = 0;
        shape.moveTo(w + startX * xMultiplier, h + startY * yMultiplier);
        for (double t = dt; t < maxValue; t += dt) {
            double x = combinedR * cos(t) + d * cos(combinedR * t / r);
            double y = combinedR * sin(t) - d * sin(combinedR * t / r);
            if (followImage) {
                x *= xMultiplier;
                y *= yMultiplier;
            }

            shape.lineTo(w + x, h + y);
        }

        return shape;
    }
}