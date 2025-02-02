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

package pixelitor.layers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import pixelitor.Composition;
import pixelitor.TestHelper;
import pixelitor.filters.painters.TextSettings;
import pixelitor.history.History;
import pixelitor.testutils.WithMask;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class TextLayerTest {
    private TextLayer layer;
    private Composition comp;

    @Parameter
    public WithMask withMask;

    @Parameters(name = "{index}: mask = {0}")
    public static Collection<Object[]> instancesToTest() {
        return Arrays.asList(new Object[][]{
                {WithMask.NO},
                {WithMask.YES},
        });
    }

    @Before
    public void setUp() {
        comp = TestHelper.createEmptyComposition();
        layer = TestHelper.createTextLayer(comp, "Text Layer");
        layer.updateLayerName();
        comp.addLayerNoGUI(layer);

        withMask.init(layer);

        assert layer.getComp().checkInvariant();
        History.clear();
    }

    @Test
    public void testRasterize() {
        checkThereIsOnlyOneLayerOfType(TextLayer.class);

        TextLayer.replaceWithRasterized(comp);

        checkThereIsOnlyOneLayerOfType(ImageLayer.class);
        History.assertNumEditsIs(1);
        History.assertLastEditNameIs("Text Layer Rasterize");

        History.undo();
        checkThereIsOnlyOneLayerOfType(TextLayer.class);

        History.redo();
        checkThereIsOnlyOneLayerOfType(ImageLayer.class);
    }

    private void checkThereIsOnlyOneLayerOfType(Class<? extends Layer> type) {
        assertThat(comp.getNrLayers()).isEqualTo(1);
        assertThat(comp.getActiveLayer()).isInstanceOf(type);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCommitSettingsWithSameSettings() {
        TextSettings oldSettings = layer.getSettings();
        // expected to throw exception because it
        // is the same settings object
        layer.commitSettings(oldSettings);
    }

    @Test
    public void testCommitSettingsCorrectly() {
        TextSettings oldSettings = layer.getSettings();
        String oldText = oldSettings.getText();
        assertThat(layer.getName()).isEqualTo(oldText);

        String newText = "New Text";
        TextSettings newSettings = new TextSettings(oldSettings);
        newSettings.setText(newText);
        layer.setSettings(newSettings);

        layer.commitSettings(oldSettings);

        assertThat(layer.getName()).isEqualTo(newText);
        History.assertNumEditsIs(1);
        History.assertLastEditNameIs("Text Layer Change");

        History.undo();
        assertThat(layer.getName()).isEqualTo(oldText);

        History.redo();
        assertThat(layer.getName()).isEqualTo(newText);
    }
}