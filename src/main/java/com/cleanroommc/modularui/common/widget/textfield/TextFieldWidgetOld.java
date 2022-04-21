package com.cleanroommc.modularui.common.widget.textfield;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.TextFieldRenderer;
import com.cleanroommc.modularui.api.drawable.TextFieldRendererOld;
import com.cleanroommc.modularui.api.math.Alignment;
import com.cleanroommc.modularui.api.math.Color;
import com.cleanroommc.modularui.api.math.Pos2d;
import com.cleanroommc.modularui.api.math.Size;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.common.internal.JsonHelper;
import com.cleanroommc.modularui.common.internal.network.NetworkUtils;
import com.cleanroommc.modularui.common.widget.SyncedWidget;
import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Deprecated
public class TextFieldWidgetOld extends SyncedWidget implements Interactable {

    // all positive whole numbers
    public static final Pattern NATURAL_NUMS = Pattern.compile("[0-9]*");
    // all positive and negative numbers
    public static final Pattern WHOLE_NUMS = Pattern.compile("-?[0-9]*");
    public static final Pattern DECIMALS = Pattern.compile("[0-9]*(\\.[0-9]*)?");
    public static final Pattern LETTERS = Pattern.compile("[a-zA-Z]*");
    public static final Pattern ANY = Pattern.compile(".*");
    private static final Pattern BASE_PATTERN = Pattern.compile("[A-Za-z0-9\\s_+\\-.,!@#$%^&*();\\\\/|<>\"'\\[\\]?=]");

    private String text = "";
    private int cursor = 0, cursorEnd = 0;
    private final Point cursorWrapper = new Point(), cursorEndWrapper = new Point();
    private Pattern pattern = ANY;
    //protected TextFieldRendererOld renderer = new TextFieldRendererOld(Pos2d.ZERO, 0, 0);
    //protected TextRendererOld helper = new TextRendererOld(Pos2d.ZERO, 0, 0);
    protected TextFieldHandler handler = new TextFieldHandler();
    protected TextFieldRenderer helper = new TextFieldRenderer(handler);
    private Supplier<String> getter;
    private Consumer<String> setter;
    private int cursorTimer = 0;
    private int textColor = TextFieldRendererOld.DEFAULT_COLOR;
    private Function<String, String> validator = val -> val;
    private int maxWidth = 80, maxLines = -1;
    private Alignment textAlignment = Alignment.TopLeft;
    private boolean newLineOnEnter = false;

    @Override
    public void readJson(JsonObject json, String type) {
        super.readJson(json, type);
        setMaxWidth(JsonHelper.getInt(json, 80, "maxWidth"));
        setMaxLines(JsonHelper.getInt(json, 1, "maxLines"));
        Integer color = JsonHelper.getElement(json, null, Color::ofJson, "textColor", "color");
        if (color != null) {
            setTextColor(color);
        }
        color = JsonHelper.getElement(json, null, Color::ofJson, "markedColor");
        if (color != null) {
            setMarkedColor(color);
        }
        setPattern(Pattern.compile(JsonHelper.getString(json, ".*", "pattern")));
        setScale(JsonHelper.getFloat(json, 1f, "scale"));
    }

    @Override
    public void onRebuild() {
        if (maxLines < 0) {
            maxLines = (int) (size.height / helper.getFontHeight());
        }
    }

    public void setCursor(int pos) {
        setCursor(pos, true);
    }

    public void setCursor(int pos, boolean setEnd) {
        this.cursor = Math.max(0, Math.min(text.length(), pos));
        this.cursorWrapper.x = this.cursor;
        if (setEnd) {
            this.cursorEnd = cursor;
            this.cursorEndWrapper.x = this.cursorEnd;
        }
    }

    public void setCursorEnd(int pos) {
        this.cursorEnd = Math.max(0, Math.min(text.length(), pos));
        this.cursorEndWrapper.x = this.cursorEnd;
    }

    public void incrementCursor(int amount) {
        setCursor(cursor + amount);
    }

    public boolean hasTextSelected() {
        return cursor != cursorEnd;
    }

    public String getSelectedText() {
        return text.substring(Math.min(cursor, cursorEnd), Math.max(cursor, cursorEnd));
    }

    public void insert(String string) {
        String part1 = text.substring(0, Math.min(cursor, cursorEnd));
        String part2 = text.substring(Math.max(cursor, cursorEnd));
        String newText = part1 + string + part2;
        if (!pattern.matcher(newText).matches() || !canFit(newText)) {
            return;
        }
        text = newText;
        setCursor(part1.length() + string.length());
    }

    public void delete(boolean positive) {
        String part1;
        String part2;
        if (hasTextSelected()) {
            part1 = text.substring(0, Math.min(cursor, cursorEnd));
            part2 = text.substring(Math.max(cursor, cursorEnd));
            text = part1 + part2;
            setCursor(part1.length());
        } else {
            if ((positive && cursor == text.length()) || (!positive && cursor == 0)) {
                return;
            }
            part1 = text.substring(0, cursor - (positive ? 0 : 1));
            part2 = text.substring(cursor + (positive ? 1 : 0));
        }
        text = part1 + part2;
        setCursor(part1.length());
    }

    @Override
    public void onScreenUpdate() {
        if (isFocused() && ++cursorTimer == 10) {
            helper.toggleCursor();
            cursorTimer = 0;
        }
    }

    @Override
    public void draw(float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5f, 0.5f, 0);
        helper.setAlignment(textAlignment, size.width, size.height);
        helper.draw(text);
        //renderer.drawAligned(text, 0, 0, size.width, size.height, textColor, textAlignment.x, textAlignment.y);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean onKeyPressed(char character, int keyCode) {
        if (character == Character.MIN_VALUE) {
            switch (keyCode) {
                case Keyboard.KEY_RETURN:
                case Keyboard.KEY_ESCAPE:
                    if (newLineOnEnter) {
                        insert("\n");
                    } else {
                        removeFocus();
                    }
                    break;
                case Keyboard.KEY_LEFT: {
                    int newCursor = cursor - 1;
                    if (Interactable.hasControlDown()) {
                        newCursor = text.lastIndexOf(" ", newCursor);
                        if (newCursor < 0) {
                            newCursor = 0;
                        }
                    }
                    setCursor(newCursor, !Interactable.hasShiftDown());
                    break;
                }
                case Keyboard.KEY_RIGHT: {
                    int newCursor = cursor + 1;
                    if (Interactable.hasControlDown()) {
                        newCursor = text.indexOf(" ", newCursor);
                        if (newCursor < 0) {
                            newCursor = text.length();
                        }
                    }
                    setCursor(newCursor, !Interactable.hasShiftDown());
                    break;
                }

                case Keyboard.KEY_DELETE:
                    delete(true);
                    break;
                case Keyboard.KEY_BACK:
                    delete(false);
                    break;
                default:
                    return false;
            }
            return true;
        }
        if (keyCode == Keyboard.KEY_BACK) {
            // backspace char is not equal to Character.MIN_VALUE
            int oldLength = text.length();
            delete(false);
            return oldLength != text.length();
        }

        if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            // copy marked text
            GuiScreen.setClipboardString(getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            // paste copied text in marked text
            insert(GuiScreen.getClipboardString());
            return true;
        } else if (GuiScreen.isKeyComboCtrlX(keyCode) && hasTextSelected()) {
            // copy and delete copied text
            GuiScreen.setClipboardString(getSelectedText());
            delete(false);
            return true;
        } else if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            // mark whole text
            setCursor(0);
            setCursor(text.length(), false);
            return true;
        } else if (BASE_PATTERN.matcher(String.valueOf(character)).matches()) {
            // insert typed char
            insert(String.valueOf(character));
            return true;
        }
        return false;
    }

    @Override
    public boolean onClick(int buttonId, boolean doubleClick) {
        setCursor(getTextIndexUnderMouse());
        return true;
    }

    @Override
    public void onMouseDragged(int buttonId, long deltaTime) {
        setCursor(getTextIndexUnderMouse(), false);
    }

    private boolean canFit(String string) {
        helper.setSimulate(true);
        helper.draw(string);
        helper.setSimulate(false);
        return helper.getLastHeight() <= helper.getFontHeight() * maxLines;
    }

    private int getTextIndexUnderMouse() {
        return getTextIndex(getContext().getMousePos().subtract(getAbsolutePos()));
    }

    private int getTextIndex(Pos2d pos2d) {
        if (text.isEmpty()) {
            return 0;
        }
        helper.setAlignment(textAlignment, size.width, size.height);
        return helper.getCursorPos(Collections.singletonList(text), pos2d.x, pos2d.y).x;
        /*helper.setPosToFind(pos2d);
        helper.drawAligned(text, 0, 0, size.width, size.height, 0, textAlignment.x, textAlignment.y);
        helper.setPosToFind(null);
        if (helper.getFoundIndex() < 0) {
            if (pos2d.x > helper.getWidth() || (pos2d.y >= helper.getHeight() - helper.getFontHeight() && pos2d.y <= helper.getHeight())) {
                return text.length();
            }
            return 0;
        }
        return helper.getFoundIndex();*/
    }

    @Override
    public boolean shouldGetFocus() {
        return true;
    }

    @Override
    public void onRemoveFocus() {
        super.onRemoveFocus();
        helper.setCursor(false);
        cursorTimer = 0;
        setCursorEnd(cursor);
        text = validator.apply(text);
        if (syncsToServer()) {
            syncToServer(1, buffer -> NetworkUtils.writeStringSafe(buffer, text));
        }
    }

    @Override
    protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
        if (this.maxWidth < 0) {
            this.maxWidth = maxWidth - getPos().x;
        }
        if (maxLines <= 0) {
            maxLines = 1;
        }
        return new Size(this.maxWidth - 1, (int) (helper.getFontHeight() * maxLines + 0.5));
    }

    @Override
    public void detectAndSendChanges() {
        if (syncsToClient() && getter != null) {
            String val = getter.get();
            if (!text.equals(val)) {
                text = val;
                syncToClient(1, buffer -> NetworkUtils.writeStringSafe(buffer, text));
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            if (!isFocused()) {
                this.text = buf.readString(Short.MAX_VALUE);
                if (this.setter != null && (this.getter == null || !this.text.equals(this.getter.get()))) {
                    this.setter.accept(this.text);
                }
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            this.text = buf.readString(Short.MAX_VALUE);
            if (this.setter != null) {
                this.setter.accept(this.text);
            }
        }
    }

    public TextFieldWidgetOld setSetter(Consumer<String> setter) {
        this.setter = setter;
        return this;
    }

    public TextFieldWidgetOld setSetterLong(Consumer<Long> setter) {
        this.setter = val -> {
            if (!val.isEmpty()) {
                try {
                    setter.accept(Long.parseLong(val));
                } catch (NumberFormatException e) {
                    ModularUI.LOGGER.warn("Error parsing text field value to long: {}", val);
                }
            }
        };
        return this;
    }

    public TextFieldWidgetOld setSetterInt(Consumer<Integer> setter) {
        this.setter = val -> {
            if (!val.isEmpty()) {
                try {
                    setter.accept(Integer.parseInt(val));
                } catch (NumberFormatException e) {
                    ModularUI.LOGGER.warn("Error parsing text field value to int: {}", val);
                }
            }
        };
        return this;
    }

    public TextFieldWidgetOld setGetter(Supplier<String> getter) {
        this.getter = getter;
        return this;
    }

    public TextFieldWidgetOld setGetterLong(Supplier<Long> getter) {
        this.getter = () -> String.valueOf(getter.get());
        return this;
    }

    public TextFieldWidgetOld setGetterInt(Supplier<Integer> getter) {
        this.getter = () -> String.valueOf(getter.get());
        return this;
    }

    public TextFieldWidgetOld setPattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public TextFieldWidgetOld setTextColor(int textColor) {
        this.textColor = textColor;
        this.helper.setColor(textColor);
        return this;
    }

    public TextFieldWidgetOld setMarkedColor(int color) {
        this.helper.setMarkedColor(color);
        return this;
    }

    public TextFieldWidgetOld setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        return this;
    }

    public TextFieldWidgetOld setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public TextFieldWidgetOld setBounds(int maxWidth, int maxLines) {
        setMaxWidth(maxWidth);
        return setMaxLines(maxLines);
    }

    public TextFieldWidgetOld setScale(float scale) {
        this.helper.setScale(scale);
        return this;
    }

    public TextFieldWidgetOld setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public TextFieldWidgetOld setNumbersLong(Function<Long, Long> validator) {
        setPattern(WHOLE_NUMS);
        setValidator(val -> {
            long num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = Long.parseLong(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
        });
        return this;
    }

    public TextFieldWidgetOld setNumbers(Function<Integer, Integer> validator) {
        setPattern(WHOLE_NUMS);
        return setValidator(val -> {
            int num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
        });
    }

    public TextFieldWidgetOld setNumbersDouble(Function<Double, Double> validator) {
        setPattern(DECIMALS);
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = 0;
            } else {
                try {
                    num = Double.parseDouble(val);
                } catch (NumberFormatException e) {
                    num = 0;
                }
            }
            return String.valueOf(validator.apply(num));
        });
    }

    public TextFieldWidgetOld setNumbers(Supplier<Integer> min, Supplier<Integer> max) {
        return setNumbers(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public TextFieldWidgetOld setNumbersLong(Supplier<Long> min, Supplier<Long> max) {
        return setNumbersLong(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    public TextFieldWidgetOld setNumbers(int min, int max) {
        return setNumbers(val -> Math.min(max, Math.max(min, val)));
    }

    public TextFieldWidgetOld setTextAlignment(Alignment textAlignment) {
        this.textAlignment = textAlignment;
        return this;
    }
}