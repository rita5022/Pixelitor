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

package pixelitor.history;

import pixelitor.Composition;
import pixelitor.layers.ImageLayer;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import static pixelitor.Composition.ImageChangeActions.FULL;

/**
 * A PixelitorEdit representing an operation that can affect multiple layers,
 * such as resize, a crop, flip, or image rotation.
 * These are undoable only if the composition has a single image layer
 */
public class MultiLayerEdit extends PixelitorEdit {
    private ImageLayer layer;

    private final ImageEdit imageEdit;
    private final CanvasChangeEdit canvasChangeEdit;
    private final TranslationEdit translationEdit;
    private SelectionChangeEdit selectionChangeEdit;
    private DeselectEdit deselectEdit;
    private final boolean undoable;

    public MultiLayerEdit(Composition comp, String name, MultiLayerBackup backup) {
        super(comp, name);
        this.canvasChangeEdit = backup.getCanvasChangeEdit();
        this.translationEdit = backup.getTranslationEdit();

        int nrLayers = comp.getNrImageLayers();
        if (nrLayers == 1) {
            layer = comp.getAnyImageLayer();
            imageEdit = backup.createImageEdit(layer.getImage());
            undoable = true;
        } else {
            imageEdit = null;
            undoable = false;
        }

        if (comp.hasSelection()) {
            assert backup.hasSavedSelection();
            selectionChangeEdit = backup.createSelectionChangeEdit();
        } else {
            if (backup.hasSavedSelection()) {
                // it was a deselect:
                // either a selection crop or a crop tool crop without
                // overlap with the existing selection.
                deselectEdit = backup.createDeselectEdit();
            }
        }
    }

    @Override
    public boolean canUndo() {
        if (!undoable) {
            return false;
        }
        return super.canUndo();
    }

    @Override
    public boolean canRedo() {
        if (!undoable) {
            return false;
        }
        return super.canRedo();
    }

    @Override
    public boolean canRepeat() {
        return false;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();

        if(imageEdit != null) {
            imageEdit.undo();
        }
        if (translationEdit != null) {
            translationEdit.undo();
        }
        // it is important to undo the canvas change edit
        // after the image and translation edits because
        // of the image covers canvas checks
        if (canvasChangeEdit != null) {
            canvasChangeEdit.undo();
        }
        if (selectionChangeEdit != null) {
            selectionChangeEdit.undo();
        }
        if (deselectEdit != null) {
            deselectEdit.undo();
        }

        updateGUI();
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();

        if(imageEdit != null) {
            imageEdit.redo();
        }
        if (translationEdit != null) {
            translationEdit.redo();
        }
        // it is important to redo the canvas change edit
        // after the image and translation edits because
        // of the image covers canvas checks
        if (canvasChangeEdit != null) {
            canvasChangeEdit.redo();
        }
        if (selectionChangeEdit != null) {
            selectionChangeEdit.redo();
        }
        if (deselectEdit != null) {
            deselectEdit.redo();
        }

        updateGUI();
    }

    private void updateGUI() {
        comp.imageChanged(FULL);
        layer.updateIconImage();
        if (imageEdit instanceof ImageAndMaskEdit) {
            layer.getMask().updateIconImage();
        }
        History.notifyMenus(this);
    }

    @Override
    public void die() {
        super.die();

        if (imageEdit != null) {
            imageEdit.die();
        }
        if (translationEdit != null) {
            translationEdit.die();
        }
        if (canvasChangeEdit != null) {
            canvasChangeEdit.die();
        }
        if (selectionChangeEdit != null) {
            selectionChangeEdit.die();
        }
        if (deselectEdit != null) {
            deselectEdit.die();
        }
    }
}