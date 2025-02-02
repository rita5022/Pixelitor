package pixelitor.utils.test;

import pixelitor.Canvas;
import pixelitor.Composition;
import pixelitor.ImageComponents;
import pixelitor.ImageDisplay;
import pixelitor.layers.ContentLayer;
import pixelitor.layers.ImageLayer;
import pixelitor.layers.Layer;
import pixelitor.layers.LayerMask;
import pixelitor.menus.view.ZoomLevel;
import pixelitor.tools.Tool;
import pixelitor.tools.Tools;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

public class Assertions {
    private Assertions() {
    }

    public static boolean hasSelection() {
        Composition comp = ImageComponents.getActiveCompOrNull();
        if (comp == null) {
            throw new IllegalStateException();
        }
        return comp.hasSelection();
    }

    public static boolean noSelection() {
        return !hasSelection();
    }

    @SuppressWarnings("SameReturnValue")
    public static boolean selectionBoundsAre(int x, int y, int w, int h) {
        Composition comp = ImageComponents.getActiveCompOrNull();
        if (comp == null) {
            throw new IllegalStateException();
        }
        if (!comp.hasSelection()) {
            throw new IllegalStateException();
        }
        Shape shape = comp.getSelectionOrNull().getShape();
        Rectangle expected = new Rectangle(x, y, w, h);
        Rectangle actual = shape.getBounds();
        if (actual.equals(expected)) {
            return true;
        } else {
            throw new AssertionError("expected = " + expected + ", actual = " + actual);
        }
    }

    public static boolean hasMask(boolean enabled, boolean linked) {
        Layer layer = ImageComponents.getActiveLayerOrNull();
        if (layer == null) {
            throw new IllegalStateException();
        }
        if (!layer.hasMask()) {
            return false;
        }
        if (layer.isMaskEnabled() != enabled) {
            return false;
        }
        LayerMask mask = layer.getMask();
        return mask.isLinked() == linked;
    }

    @SuppressWarnings("SameReturnValue")
    public static boolean canvasSizeIs(int w, int h) {
        Composition comp = ImageComponents.getActiveCompOrNull();
        if (comp == null) {
            throw new IllegalStateException();
        }
        Canvas canvas = comp.getCanvas();
        int actualWidth = canvas.getWidth();
        int actualHeight = canvas.getHeight();
        if (actualWidth == w && actualHeight == h) {
            return true;
        } else {
            throw new AssertionError("Expected w = " + w + ", h = " + h +
                    ", actual w = " + actualWidth + ", h = " + actualHeight);
        }
    }

    public static boolean translationIs(int x, int y) {
        ImageLayer layer = ImageComponents.getActiveImageLayerOrMaskOrNull();
        return translationIs(layer, x, y);
    }

    public static boolean translationIs(ContentLayer layer, int x, int y) {
        if (layer == null) {
            throw new IllegalArgumentException();
        }
        boolean eq1 = layer.getTX() == x;
        boolean eq2 = layer.getTY() == y;
        assert eq1 : "expected x = " + x + ", found = " + layer.getTX() + " for " + layer.getName();
        assert eq2 : "expected y = " + y + ", found = " + layer.getTY() + " for " + layer.getName();
        return eq1 && eq2;
    }

    public static boolean cropToolRectangleBoundsAre(int x, int y, int w, int h) {
        ImageDisplay ic = ImageComponents.getActiveIC();
        if (ic == null) {
            throw new IllegalStateException();
        }
        return Tools.CROP.getCropRect(ic).equals(new Rectangle(x, y, w, h));
    }

    public static boolean selectedToolIs(Tool tool) {
        return Tools.getCurrentTool() == tool;
    }

    public static boolean pixelColorIs(int x, int y, int a, int r, int g, int b) {
        Composition comp = ImageComponents.getActiveCompOrNull();
        if (comp == null) {
            throw new IllegalStateException();
        }
        BufferedImage img = comp.getCompositeImage();
        int rgb = img.getRGB(x, y);
        Color c = new Color(rgb);
        return c.getAlpha() == a && c.getRed() == r && c.getGreen() == g && c.getBlue() == b;
    }

    public static boolean zoomIs(ZoomLevel zoom) {
        ImageDisplay ic = ImageComponents.getActiveIC();
        if (ic == null) {
            throw new IllegalStateException();
        }
        return ic.getZoomLevel() == zoom;
    }

    public static boolean numLayersIs(int number) {
        Composition comp = ImageComponents.getActiveCompOrNull();
        if (comp == null) {
            throw new IllegalStateException();
        }
        return comp.getNrLayers() == number;
    }

    public static boolean numOpenImagesIs(int number) {
        return ImageComponents.getNrOfOpenImages() == number;
    }
}
