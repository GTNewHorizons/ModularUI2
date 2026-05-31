package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.widget.scroll.ScrollArea;

import com.google.common.base.Joiner;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Handles the text itself like inserting and deleting text. Also handles the cursor and marking text.
 */
public class TextFieldHandler {

    private static final Joiner JOINER = Joiner.on('\n');

    private final List<String> text = new ArrayList<>();
    private final Point cursor = new Point(), cursorEnd = new Point();
    private final BaseTextFieldWidget<?> textFieldWidget;
    private TextFieldRenderer renderer;
    @Nullable
    private ScrollArea scrollArea;
    private boolean mainCursorStart = true;
    private int maxLines = 1;
    @Nullable
    private Pattern pattern;
    private int maxCharacters = -1;
    private GuiContext guiContext;

    public TextFieldHandler(BaseTextFieldWidget<?> textFieldWidget) {
        this.textFieldWidget = textFieldWidget;
    }

    public void setPattern(@Nullable Pattern pattern) {
        this.pattern = pattern;
    }

    public void setMaxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
    }

    public void setScrollArea(@Nullable ScrollArea scrollArea) {
        this.scrollArea = scrollArea;
    }

    public void setRenderer(TextFieldRenderer renderer) {
        this.renderer = renderer;
    }

    public void switchCursors() {
        this.mainCursorStart = !this.mainCursorStart;
    }

    public Point getMainCursor() {
        return this.mainCursorStart ? this.cursor : this.cursorEnd;
    }

    public Point getOffsetCursor() {
        return this.mainCursorStart ? this.cursorEnd : this.cursor;
    }

    public Point getStartCursor() {
        if (!hasTextMarked()) {
            return this.cursor;
        }
        return this.cursor.y > this.cursorEnd.y || (this.cursor.y == this.cursorEnd.y && this.cursor.x > this.cursorEnd.x) ? this.cursorEnd : this.cursor;
    }

    public Point getEndCursor() {
        if (!hasTextMarked()) {
            return this.cursor;
        }
        return this.cursor.y > this.cursorEnd.y || (this.cursor.y == this.cursorEnd.y && this.cursor.x > this.cursorEnd.x) ? this.cursor : this.cursorEnd;
    }

    public boolean hasTextMarked() {
        return this.cursor.y != this.cursorEnd.y || this.cursor.x != this.cursorEnd.x;
    }

    public void setOffsetCursor(int linePos, int charPos) {
        getOffsetCursor().setLocation(charPos, linePos);
    }

    public void setMainCursor(int linePos, int charPos, boolean animate) {
        Point main = getMainCursor();
        if (main.x != charPos || main.y != linePos) {
            main.setLocation(charPos, linePos);
            if (!this.text.isEmpty() && this.renderer != null && this.scrollArea != null) {
                // update actual width
                this.renderer.setSimulate(true);
                this.renderer.draw(this.text);
                this.renderer.setSimulate(false);
                this.scrollArea.getScrollX().setScrollSize((int) this.renderer.getLastActualWidth());
                if (this.scrollArea.getScrollX().isScrollBarActive(this.scrollArea)) {
                    String line = this.text.get(main.y);
                    int scrollTo = (int) this.renderer.getPosOf(this.renderer.measureLines(Collections.singletonList(line)), main).x;
                    scrollTo -= this.scrollArea.getScrollX().getFullVisibleSize(this.scrollArea) / 2;
                    if (animate) {
                        this.scrollArea.getScrollX().animateTo(this.scrollArea, scrollTo);
                    } else {
                        this.scrollArea.getScrollX().scrollTo(this.scrollArea, scrollTo);
                    }
                }
            }
        }
    }

    public void setCursor(int linePos, int charPos, boolean animate) {
        setCursor(linePos, charPos, true, animate);
    }

    public void setCursor(int linePos, int charPos, boolean applyToOffset, boolean animate) {
        setMainCursor(linePos, charPos, animate);
        if (applyToOffset) {
            setOffsetCursor(linePos, charPos);
        }
    }

    public void setOffsetCursor(Point cursor) {
        setOffsetCursor(cursor.y, cursor.x);
    }

    public void setMainCursor(Point cursor, boolean animate) {
        setMainCursor(cursor.y, cursor.x, animate);
    }

    public void setCursor(Point cursor, boolean animate) {
        setMainCursor(cursor, animate);
        setOffsetCursor(cursor);
    }

    private void clampCursor(Point p) {
        p.y = MathUtils.clamp(p.y, 0, this.text.size() - 1);
        String line = this.text.get(p.y);
        p.x = MathUtils.clamp(p.x, 0, line.length());
    }

    public void clampCursors() {
        clampCursor(getMainCursor());
        setOffsetCursor(getMainCursor());
    }

    public void putMainCursorAtStart() {
        if (hasTextMarked() && getMainCursor() != getStartCursor()) {
            switchCursors();
        }
    }

    public void putMainCursorAtEnd() {
        if (hasTextMarked() && getMainCursor() != getEndCursor()) {
            switchCursors();
        }
    }

    public void moveCursorLeft(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.x == 0) {
            if (main.y == 0) return;
            setCursor(main.y - 1, this.text.get(main.y - 1).length(), !shift, true);
        } else {
            int newPos = main.x - 1;
            if (ctrl) {
                newPos = searchWord(this.text.get(main.y), newPos, true);
            }
            setCursor(main.y, newPos, !shift, true);
        }
    }

    public void moveCursorRight(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        String line = this.text.get(main.y);
        if (main.x == line.length()) {
            if (main.y == this.text.size() - 1) return;
            setCursor(main.y + 1, 0, !shift, true);
        } else {
            int newPos = main.x + 1;
            if (ctrl) {
                newPos = searchWord(this.text.get(main.y), newPos - 1, false);
            }
            setCursor(main.y, newPos, !shift, true);
        }
    }

    public int searchWord(String line, int start, boolean reverse) {
        if (reverse) {
            if (start <= 1) return 0;
            for (int i = start; i >= 0; i--) {
                if (!canMoveCursorPastChar(line.charAt(i))) {
                    return i == start ? i : i + 1;
                }
            }
            return 0;
        }
        if (start >= line.length() - 1) return line.length();
        for (int i = start; i < line.length(); i++) {
            if (!canMoveCursorPastChar(line.charAt(i))) {
                return i == start ? i + 1 : i;
            }
        }
        return line.length();
    }

    private boolean canMoveCursorPastChar(char c) {
        return Character.isLetter(c) || Character.isDigit(c) || c == ',' || c == '_';
    }

    public void moveCursorUp(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.y > 0) {
            setCursor(main.y - 1, main.x, !shift, true);
        } else {
            setCursor(main.y, 0, !shift, true);
        }
    }

    public void moveCursorDown(boolean ctrl, boolean shift) {
        if (this.text.isEmpty()) return;
        Point main = getMainCursor();
        if (main.y < this.text.size() - 1) {
            setCursor(main.y + 1, main.x, !shift, true);
        } else {
            setCursor(main.y, this.text.get(main.y).length(), !shift, true);
        }
    }

    public void moveCursorStart(boolean ctrl, boolean shift) {
        int y = ctrl ? 0 : getMainCursor().y;
        setCursor(y, 0, !shift, true);
    }

    public void moveCursorEnd(boolean ctrl, boolean shift) {
        int y = ctrl ? this.text.size() - 1 : getMainCursor().y;
        String line = this.text.get(y);
        setCursor(y, line.length(), !shift, true);
    }

    public void markAll() {
        setOffsetCursor(0, 0);
        setMainCursor(this.text.size() - 1, this.text.get(this.text.size() - 1).length(), true);
    }

    public void markCurrentLine() {
        setOffsetCursor(getMainCursor().y, 0);
        setMainCursor(getMainCursor().y, this.text.get(getMainCursor().y).length(), true);
    }

    public String getTextAsString() {
        return JOINER.join(this.text);
    }

    public List<String> getText() {
        return this.text;
    }

    public boolean isTextEmpty() {
        if (this.text.isEmpty()) return true;
        for (String line : this.text) {
            if (!line.isEmpty()) return false;
        }
        return true;
    }

    public void onChanged() {
        this.textFieldWidget.onTextChanged();
        this.textFieldWidget.markTooltipDirty();
    }

    public String getSelectedText() {
        if (!hasTextMarked()) return "";
        Point min = getStartCursor();
        Point max = getEndCursor();
        if (min.y == max.y) {
            return this.text.get(min.y).substring(min.x, max.x);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(this.text.get(min.y).substring(min.x));
        if (max.y > min.y + 2) {
            for (int i = min.y + 1; i < max.y - 1; i++) {
                builder.append(this.text.get(i));
            }
        }
        builder.append(this.text.get(max.y), 0, max.x);
        return builder.toString();
    }

    public boolean test(String text) {
        return this.maxLines > 1 || ((this.pattern == null || this.pattern.matcher(text).matches()) && (this.maxCharacters < 0 || this.maxCharacters >= text.length()));
    }

    public void insert(String text, boolean hasHorizontalScrolling) {
        insert(Arrays.asList(text.split("\n")), hasHorizontalScrolling);
    }

    public void insert(List<String> text, boolean hasHorizontalScrolling) {
        List<String> copy = new ArrayList<>(this.text);
        Point point = insert(copy, text);
        // if we can scroll horizontally, we have virtually an infinite amount of space and don't need to check width
        if (point == null || copy.size() > this.maxLines || !this.renderer.wouldFit(copy, !hasHorizontalScrolling)) return;
        this.text.clear();
        this.text.addAll(copy);
        setCursor(point, true);
        onChanged();
    }

    private Point insert(List<String> text, List<String> insertion) {
        if (insertion.isEmpty() || (insertion.size() > 1 && text.size() + insertion.size() - 1 > this.maxLines)) {
            return null;
        }
        int x, y = this.cursor.y;
        if (hasTextMarked()) {
            delete(false, false, false);
        }
        if (text.isEmpty()) {
            if (insertion.size() == 1 && !test(insertion.get(0))) {
                return null;
            }
            text.addAll(insertion);
            return new Point(text.get(text.size() - 1).length(), text.size() - 1);
        }
        String lineStart = text.get(this.cursor.y).substring(0, this.cursor.x);
        String lineEnd = text.get(this.cursor.y).substring(this.cursor.x);
        if (insertion.size() == 1 && text.size() == 1 && !test(lineStart + insertion.get(0) + lineEnd)) {
            return null;
        }
        text.set(this.cursor.y, lineStart + insertion.get(0));
        if (insertion.size() == 1) {
            if (!test(insertion.get(0))) {
                return null;
            }
            text.set(this.cursor.y, text.get(this.cursor.y) + lineEnd);
            return new Point(this.cursor.x + insertion.get(0).length(), this.cursor.y);
        } else {
            text.add(this.cursor.y + 1, insertion.get(insertion.size() - 1) + lineEnd);
            x = insertion.get(insertion.size() - 1).length();
            y += 1;
            if (insertion.size() > 2) {
                text.addAll(this.cursor.y + 1, text.subList(1, insertion.size() - 1));
                x = insertion.get(insertion.size() - 1).length();
                y += insertion.size() - 1;
            }
            return new Point(x, y);
        }
    }

    public void newLine() {
        deleteMarked();
        String line = this.text.get(this.cursor.y);
        this.text.set(this.cursor.y, line.substring(0, this.cursor.x));
        this.text.add(this.cursor.y + 1, line.substring(this.cursor.x));
        setCursor(this.cursor.y + 1, 0, false);
    }

    public void clear() {
        markAll();
        deleteMarked();
    }

    public void deleteMarked() {
        if (hasTextMarked()) {
            delete(false, false, false);
        }
    }

    public void delete(boolean ctrl, boolean shift) {
        delete(false, ctrl, shift);
    }

    public void delete(boolean inFront, boolean ctrl, boolean shift) {
        if (hasTextMarked()) {
            Point min = getStartCursor();
            Point max = getEndCursor();
            String minLine = this.text.get(min.y);
            if (min.y == max.y) {
                this.text.set(min.y, minLine.substring(0, min.x) + minLine.substring(max.x));
            } else {
                String maxLine = this.text.get(Math.min(this.text.size() - 1, max.y));
                this.text.set(min.y, minLine.substring(0, min.x) + maxLine.substring(max.x));
                if (max.y > min.y + 1) {
                    this.text.subList(min.y + 1, max.y + 1).clear();
                }
            }
            setCursor(min.y, min.x, false);
        } else {
            String line = this.text.get(this.cursor.y);
            if (inFront) {
                if (this.cursor.x == line.length()) {
                    if (this.text.size() > this.cursor.y + 1) {
                        this.text.set(this.cursor.y, line + this.text.get(this.cursor.y + 1));
                        this.text.remove(this.cursor.y + 1);
                    }
                } else {
                    if (shift) {
                        this.text.remove(this.cursor.y);
                        if (this.text.isEmpty()) this.text.add("");
                        clampCursors();
                    } else {
                        int p1 = this.cursor.x;
                        int p2 = p1 + 1;
                        if (ctrl) {
                            p2 = searchWord(line, p2, false);
                        }
                        line = line.substring(0, p1) + line.substring(p2);
                        this.text.set(this.cursor.y, line);
                    }
                }
            } else {
                if (this.cursor.x == 0) {
                    if (this.cursor.y > 0) {
                        String lineAbove = this.text.get(this.cursor.y - 1);
                        this.text.set(this.cursor.y - 1, lineAbove + line);
                        this.text.remove(this.cursor.y);
                        setCursor(this.cursor.y - 1, lineAbove.length(), false);
                    }
                } else {
                    int p2 = this.cursor.x;
                    int p1 = p2 - 1;
                    if (ctrl) {
                        p1 = searchWord(line, p1, true);
                    }
                    line = line.substring(0, p1) + line.substring(p2);
                    this.text.set(this.cursor.y, line);
                    setCursor(this.cursor.y, p1, false);
                }
            }
        }
        if (this.scrollArea != null) {
            this.scrollArea.getScrollX().clamp(this.scrollArea);
        }
        onChanged();
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = Math.max(1, maxLines);
    }

    public int getMaxLines() {
        return this.maxLines;
    }

    public GuiContext getGuiContext() {
        return this.guiContext;
    }

    public void setGuiContext(GuiContext guiContext) {
        this.guiContext = guiContext;
    }
}
