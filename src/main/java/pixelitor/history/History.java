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

import pixelitor.Build;
import pixelitor.Composition;
import pixelitor.ConsistencyChecks;
import pixelitor.ImageComponents;
import pixelitor.menus.MenuAction;
import pixelitor.utils.AppPreferences;
import pixelitor.utils.Messages;
import pixelitor.utils.VisibleForTesting;
import pixelitor.utils.test.DebugEventQueue;
import pixelitor.utils.test.HistoryEvent;

import javax.swing.*;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Static methods for managing history and undo/redo for all open images
 */
public class History {
    private static final UndoableEditSupport undoableEditSupport = new UndoableEditSupport();
    private static final PixelitorUndoManager undoManager = new PixelitorUndoManager();
    private static int numUndoneEdits = 0;
    private static boolean suspended = false;

    static {
        setUndoLevels(AppPreferences.loadUndoLevels());
    }

    public static final Action UNDO_ACTION = new MenuAction("Undo") {
        @Override
        public void onClick() {
            History.undo();
        }
    };

    public static final Action REDO_ACTION = new MenuAction("Redo") {
        @Override
        public void onClick() {
            History.redo();
        }
    };

    private History() {
    }

    /**
     * This is used to notify the menu items
     */
    public static void notifyMenus(PixelitorEdit edit) {
        undoableEditSupport.postEdit(edit);
    }

    public static void addEdit(AddToHistory addToHistory, Supplier<PixelitorEdit> supplier) {
        if (addToHistory.isYes()) {
            addEdit(supplier.get());
        }
    }

    public static void addEdit(PixelitorEdit edit) {
        assert edit != null;
        if (suspended) {
            return;
        }

        if (edit.canUndo()) {
            undoManager.addEdit(edit);
        } else {
            undoManager.discardAllEdits();
        }

        numUndoneEdits = 0; // reset BEFORE posting, so that the fade menu item can become enabled
        undoableEditSupport.postEdit(edit);

        if (Build.CURRENT != Build.FINAL) {
            DebugEventQueue.post(new HistoryEvent(edit));
            ConsistencyChecks.checkAll(false);
        }
    }

    public static String getUndoPresentationName() {
        return undoManager.getUndoPresentationName();
    }

    public static String getRedoPresentationName() {
        return undoManager.getRedoPresentationName();
    }

    public static void undo() {
        if (Build.CURRENT != Build.FINAL) {
            DebugEventQueue.post(HistoryEvent.createUndoEvent());
        }

        try {
            numUndoneEdits++; // increase it before calling undoManager.undo() so that the result of undo is not fadeable
            undoManager.undo();
        } catch (CannotUndoException e) {
            Messages.showInfo("No undo available", "No undo available, probably because the undo image was discarded in order to save memory");
        }
    }

    public static void redo() {
        if (Build.CURRENT != Build.FINAL) {
            DebugEventQueue.post(HistoryEvent.createRedoEvent());
        }

        try {
            numUndoneEdits--; // after redo we should be fadeable again
            undoManager.redo();
        } catch (CannotRedoException e) {
            // TODO is a "No redo available" scenario possible?
            Messages.showException(e);
        }
    }

    public static boolean canUndo() {
        return undoManager.canUndo();
    }

    public static boolean canRedo() {
        return undoManager.canRedo();
    }

    public static void addUndoableEditListener(UndoableEditListener listener) {
        undoableEditSupport.addUndoableEditListener(listener);
    }

    public static void setUndoLevels(int undoLevels) {
        undoManager.setLimit(undoLevels);
    }

    public static int getUndoLevels() {
        return undoManager.getLimit();
    }

    public static boolean canRepeatOperation() {
        if (numUndoneEdits > 0) {
            return false;
        }

        PixelitorEdit lastEdit = undoManager.getLastEdit();
        if (lastEdit != null) {
            return lastEdit.canRepeat();
        }
        return false;
    }

    /**
     * Used for the name of the fade/repeat menu items
     */
    public static String getLastEditName() {
        PixelitorEdit lastEdit = undoManager.getLastEdit();
        if (lastEdit != null) {
            return lastEdit.getPresentationName();
        }
        return "";
    }

    /**
     * If the last edit in the history is a FadeableEdit for the given composition,
     * return it, otherwise return empty Optional
     */
    public static Optional<FadeableEdit> getPreviousEditForFade(Composition comp) {
        if (numUndoneEdits > 0) {
            return Optional.empty();
        }
        PixelitorEdit lastEdit = undoManager.getLastEdit();
        if (lastEdit != null) {
            if (lastEdit instanceof FadeableEdit) {
                FadeableEdit fadeableEdit = (FadeableEdit) lastEdit;
                if (!fadeableEdit.isFadeable()) {
                    return Optional.empty();
                }

                Composition lastComp = lastEdit.getComp();
                if (comp != lastComp) {
                    // this happens if the active image has changed
                    // since the last edit
                    return Optional.empty();
                }
                return Optional.of(fadeableEdit);
            }
        }
        return Optional.empty();
    }

    public static boolean canFade() {
        Optional<Composition> optComp = ImageComponents.getActiveComp();
        if (!optComp.isPresent()) {
            return false;
        }
        Composition comp = optComp.get();
        if (!comp.hasActiveImageLayerOrMask()) {
            return false;
        }
        return getPreviousEditForFade(comp).isPresent();
    }

    public static void onAllImagesClosed() {
        numUndoneEdits = 0;
//        lastFadeableEdit = null;

        undoManager.discardAllEdits();
        undoableEditSupport.postEdit(null);
    }

    public static void showHistory() {
        undoManager.showHistory();
    }

    // for debugging only
    public static PixelitorEdit getLastEdit() {
        return undoManager.getLastEdit();
    }

    @VisibleForTesting
    public static void clear() {
        undoManager.discardAllEdits();
        assertNumEditsIs(0);
    }

    @VisibleForTesting
    public static void assertNumEditsIs(int expectedEdits) {
        int numEdits = undoManager.getSize();
        if (numEdits != expectedEdits) {
            throw new AssertionError(String.format(
                    "Expected %d edits, but found %d",
                    expectedEdits, numEdits));
        }
    }

    @VisibleForTesting
    public static void assertLastEditNameIs(String expectedName) {
        String lastEditName = undoManager.getLastEdit().getName();
        if (!lastEditName.equals(expectedName)) {
            throw new AssertionError(String.format(
                    "Expected '%s' as the last edit name, but found '%s'",
                    expectedName, lastEditName));
        }
    }

    public static void setSuspended(boolean suspended) {
        History.suspended = suspended;
    }
}
