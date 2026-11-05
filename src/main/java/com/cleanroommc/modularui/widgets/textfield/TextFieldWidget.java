package com.cleanroommc.modularui.widgets.textfield;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.drawable.ITextLine;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.DAM;
import com.cleanroommc.modularui.utils.MathUtils;
import com.cleanroommc.modularui.utils.NumberFormat;
import com.cleanroommc.modularui.utils.ParseResult;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Text input widget with one line only. Can be synced between client and server. Can handle text validation.
 */
public class TextFieldWidget extends BaseTextFieldWidget<TextFieldWidget> {

    private IStringValue<?> stringValue;
    private Function<String, String> validator = val -> val;
    private boolean numbers = false;
    private String mathFailMessage = null;
    private double defaultNumber = 0;
    private boolean tooltipOverride = false;
    private boolean autoUpdateOnChange = false;
    private boolean acceptsExpression = true;
    private double scrollStep = 1;
    private double scrollStepCtrl = 0.1;
    private double scrollStepShift = 100;
    private double scrollStepAlt = 10000;
    private boolean usingScrollStep = false;
    private INumberParser parser;

    public double parse(String num) {
        if (!this.acceptsExpression) {
            try {
                return NumberFormat.AMOUNT_TEXT.format.parse(num).doubleValue();
            } catch (ParseException ex) {
                this.mathFailMessage = "Unable to parse number.";
                return 0.0;
            }
        }

        ParseResult result = (this.parser == null ? MathUtils.PARSER_WITH_SI : this.parser).parse(num, this.defaultNumber);
        if (result.isFailure()) {
            this.mathFailMessage = result.getErrorMessage();
            ModularUI.LOGGER.error("Math expression error in {}: {}", this, this.mathFailMessage);
            return defaultNumber;
        }
        return result.getResult().getNumberValue().doubleValue();
    }

    public IStringValue<?> createMathFailMessageValue() {
        return new StringValue.Dynamic(() -> this.mathFailMessage, val -> this.mathFailMessage = val);
    }

    @Override
    public void onInit() {
        super.onInit();
        if (this.stringValue == null) {
            this.stringValue = new StringValue(this.validator.apply(""));
        }
        setText(this.stringValue.getStringValue());
        if (!hasTooltip() && !tooltipOverride) {
            tooltipBuilder(tooltip -> tooltip.addLine(IKey.str(getText())));
            // set back to false so this won't get triggered
            tooltipOverride = false;
        }
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(IStringValue.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.stringValue = syncOrValue.castNullable(IStringValue.class);
        if (syncOrValue instanceof ValueSyncHandler<?, ?> valueSyncHandler) {
            valueSyncHandler.setChangeListener(() -> {
                markTooltipDirty();
                setText(this.stringValue.getValue().toString());
            });
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isFocused()) {
            String s = this.stringValue.getStringValue();
            if (!getText().equals(s)) {
                setText(s);
            }
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        if (hasTooltip() && (tooltipOverride || getScrollData().isScrollBarActive(getScrollArea())) && isHoveringFor(getTooltip().getShowUpTimer()) && !context.hasDraggable()) {
            getTooltip().draw(getContext());
        }
    }

    @NotNull
    public String getText() {
        if (this.handler.getText().isEmpty()) {
            return "";
        }
        if (this.handler.getText().size() > 1) {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        return this.handler.getText().get(0);
    }

    public void setText(@NotNull String text) {
        if (this.handler.getText().isEmpty()) {
            this.handler.getText().add(text);
        } else {
            this.handler.getText().set(0, text);
        }
    }

    /**
     * Allows for setting the numeric values with mouse scrolling.
     * Will only allow for this behavior when number formatting is enabled, the scroll step is enabled, and the field is focused
     */
    @Override
    public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
        // default to basic behavior if scroll step isn't on, if the widget is not using numbers, and if it is focused
        if (!this.usingScrollStep || !this.numbers || !isFocused()) return super.onMouseScroll(scrollDirection, amount);

        double step = this.scrollStep;
        if (Interactable.hasControlDown()) step *= this.scrollStepCtrl;
        if (Interactable.hasShiftDown()) step *= this.scrollStepShift;
        if (Interactable.hasAltDown()) step *= this.scrollStepAlt;
        step *= scrollDirection.modifier;
        double number = this.parse(getText()) + step;
        String representation = validator.apply(Double.toString(number));
        this.stringValue.setStringValue(representation);
        this.setText(representation);
        markTooltipDirty();
        return true;
    }


    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        super.onRemoveFocus(context);
        if (this.handler.getText().isEmpty()) {
            this.handler.getText().add(this.validator.apply(""));
        } else if (this.handler.getText().size() == 1) {
            this.handler.getText().set(0, this.validator.apply(this.handler.getText().get(0)));
            markTooltipDirty();
        } else {
            throw new IllegalStateException("TextFieldWidget can only have one line!");
        }
        this.stringValue.setStringValue(this.numbers ? format.parse(getText(), new ParsePosition(0)).toString() : getText());
    }

    @Override
    protected void onTextChanged() {
        super.onTextChanged();
        if (this.autoUpdateOnChange) {
            String text = this.validator.apply(getText());
            this.stringValue.setStringValue(this.numbers ? format.parse(text, new ParsePosition(0)).toString() : getText());
        }
    }

    @Override
    public boolean canHover() {
        return true;
    }

    public boolean isAutoUpdateOnChange() {
        return autoUpdateOnChange;
    }

    public boolean acceptsExpression() {
        return acceptsExpression;
    }

    public String getMathFailMessage() {
        return mathFailMessage;
    }

    public IStringValue<?> getStringValue() {
        return stringValue;
    }

    public TextFieldWidget acceptsExpressions(boolean acceptsExpression) {
        this.acceptsExpression = acceptsExpression;
        return this;
    }

    /**
     * Sets if the string value should be updated every time the text changes and not just when the widget is unfocused.
     * This is useful for search text fields.
     *
     * @param autoUpdateOnChange if the string value should be updated when text changes
     * @return this
     */
    public TextFieldWidget autoUpdateOnChange(boolean autoUpdateOnChange) {
        this.autoUpdateOnChange = autoUpdateOnChange;
        return this;
    }

    public TextFieldWidget setMaxLength(int maxLength) {
        this.handler.setMaxCharacters(maxLength);
        return this;
    }

    public TextFieldWidget setPattern(Pattern pattern) {
        this.handler.setPattern(pattern);
        return this;
    }

    public TextFieldWidget setValidator(Function<String, String> validator) {
        this.validator = validator;
        return this;
    }

    public TextFieldWidget numberParser(INumberParser parser) {
        this.parser = parser;
        return this;
    }

    public TextFieldWidget numbersDouble(DAM.UnaryDoubleOperator validator) {
        this.numbers = true;
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = this.defaultNumber;
            } else {
                num = parse(val);
            }
            return format.format(validator.apply(num));
        });
    }

    public TextFieldWidget numbersDouble(DAM.UnaryDoubleOperator validator, @Nullable DoubleSupplier min, @Nullable DoubleSupplier max) {
        return numbersDouble(d -> {
            d = validator.apply(d);
            if (max != null) d = Math.min(d, max.getAsDouble());
            if (min != null) d = Math.max(d, min.getAsDouble());
            return d;
        });
    }

    public TextFieldWidget numbersDouble(@Nullable DoubleSupplier min, @Nullable DoubleSupplier max) {
        return numbersDouble(DAM.UnaryDoubleOperator.IDENTITY, min, max);
    }

    public TextFieldWidget numbersDouble(double min, double max) {
        return numbersDouble(() -> min, () -> max);
    }

    public TextFieldWidget numbersDouble() {
        return numbersDouble(DAM.UnaryDoubleOperator.IDENTITY);
    }

    public TextFieldWidget numbersLong(MathUtils.UnaryLongOperator validator) {
        numberParser(MathUtils.PARSER_WHOLE_NUMBER);
        defaultWholeNumberScrollValues();
        return numbersDouble(d -> validator.apply(Math.round(d)));
    }

    /**
     * Sets this text number to accept whole numbers.
     *
     * @param validator allow further validation of the number
     * @param min       optional lower limit
     * @param max       optional upper limit, if this is specified, then values that evaluate to a noninteger are multiplied by the max
     */
    public TextFieldWidget numbersLong(MathUtils.UnaryLongOperator validator, @Nullable LongSupplier min, @Nullable LongSupplier max) {
        formatAsInteger(true);
        defaultWholeNumberScrollValues();
        numberParser(MathUtils.PARSER_WHOLE_NUMBER);
        return numbersDouble(d -> {
            long l;
            if (max != null) {
                long maxValue = max.getAsLong();
                l = MathUtils.percentOrSelf(d, maxValue);
                l = Math.min(validator.apply(l), maxValue);
            } else {
                l = validator.apply(Math.round(d));
            }
            if (min != null) {
                l = Math.max(l, min.getAsLong());
            }
            return l;
        });
    }

    public TextFieldWidget numbersLong(@Nullable LongSupplier min, @Nullable LongSupplier max) {
        return numbersLong(MathUtils.UnaryLongOperator.IDENTITY, min, max);
    }

    public TextFieldWidget numbersLong(final long min, final long max) {
        return numbersLong(() -> min, () -> max);
    }

    public TextFieldWidget numbersLong() {
        return numbersLong(MathUtils.UnaryLongOperator.IDENTITY);
    }

    public TextFieldWidget numbersInt(MathUtils.UnaryIntOperator validator) {
        numberParser(MathUtils.PARSER_WHOLE_NUMBER);
        defaultWholeNumberScrollValues();
        return numbersDouble(d -> validator.apply(MathUtils.castToIntSaturated(Math.round(d))));
    }

    public TextFieldWidget numbersInt(MathUtils.UnaryIntOperator validator, @Nullable LongSupplier min, @Nullable LongSupplier max) {
        return numbersLong(l -> validator.apply(MathUtils.castToIntSaturated(l)), min, max);
    }

    public TextFieldWidget numbersInt(@Nullable LongSupplier min, @Nullable LongSupplier max) {
        return numbersLong(MathUtils.UnaryLongOperator.IDENTITY, min, max);
    }

    public TextFieldWidget numbersInt(final int min, final int max) {
        return numbersLong(() -> min, () -> max);
    }

    public TextFieldWidget numbersInt() {
        return numbersInt(MathUtils.UnaryIntOperator.IDENTITY);
    }

    @Deprecated
    public TextFieldWidget setNumbersLong(Function<Long, Long> validator) {
        return numbersDouble(d -> validator.apply((long) d));
    }

    @Deprecated
    public TextFieldWidget setNumbers(Function<Integer, Integer> validator) {
        return numbersDouble(d -> validator.apply((int) d));
    }

    @Deprecated
    public TextFieldWidget setNumbersDouble(Function<Double, Double> validator) {
        this.numbers = true;
        return setValidator(val -> {
            double num;
            if (val.isEmpty()) {
                num = this.defaultNumber;
            } else {
                num = parse(val);
            }
            return format.format(validator.apply(num));
        });
    }

    @Deprecated
    public TextFieldWidget setNumbers(Supplier<Integer> min, Supplier<Integer> max) {
        return setNumbers(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    @Deprecated
    public TextFieldWidget setNumbersLong(Supplier<Long> min, Supplier<Long> max) {
        return setNumbersLong(val -> Math.min(max.get(), Math.max(min.get(), val)));
    }

    @Deprecated
    public TextFieldWidget setNumbers(int min, int max) {
        return setNumbers(val -> Math.min(max, Math.max(min, val)));
    }

    @Deprecated
    public TextFieldWidget setNumbers() {
        return setNumbers(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Deprecated
    public TextFieldWidget setDefaultNumber(double defaultNumber) {
        return defaultNumber(defaultNumber);
    }

    public TextFieldWidget defaultNumber(double defaultNumber) {
        this.defaultNumber = defaultNumber;
        return this;
    }

    @Deprecated
    public TextFieldWidget setFormatAsInteger(boolean formatAsInteger) {
        if (formatAsInteger && !this.numbers) {
            setNumbers(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        return formatAsInteger(formatAsInteger);
    }

    public TextFieldWidget formatAsInteger(boolean formatAsInteger) {
        this.renderer.setFormatAsInteger(formatAsInteger);
        return this;
    }

    public TextFieldWidget value(IStringValue<?> stringValue) {
        setSyncOrValue(ISyncOrValue.orEmpty(stringValue));
        return this;
    }

    @Deprecated
    public TextFieldWidget setScrollValues(double baseStep, double ctrlStep, double shiftStep) {
        return scrollValues(baseStep, shiftStep, ctrlStep, 0);
    }

    private void defaultWholeNumberScrollValues() {
        if (!this.usingScrollStep) {
            scrollValues(1, 100, 10_000, 1_000_000);
            this.usingScrollStep = false;
        }
    }

    /**
     * Sets the values by which to increment the field when the player uses the scroll wheel.
     * Scrolling up increases value, and scrolling down decreases value. When multiple modifiers are held at the same time, the increments
     * will be multiplied with each other.
     * Also enables the usingScrollStep flag.
     * Default values: 1, 100, 0.1, 10000 in order. For whole numbers: 1, 100, 10_000, 1_000_000
     *
     * @param baseStep  By how much to change the value when no modifier key is held
     * @param ctrlStep  By how much to change the value when the ctrl key is held
     * @param shiftStep By how much to change the value when the shift key is held
     * @param altStep   By how much to change the value when the alt key is held
     * @return this
     */
    public TextFieldWidget scrollValues(double baseStep, double shiftStep, double ctrlStep, double altStep) {
        this.scrollStep = baseStep;
        this.scrollStepCtrl = ctrlStep;
        this.scrollStepShift = shiftStep;
        this.scrollStepAlt = altStep;
        this.usingScrollStep = true;
        return this;
    }

    public TextFieldWidget usingScrollStep() {
        return usingScrollStep(true);
    }

    /**
     * Sets the usingScrollStep flag
     *
     * @return this
     */
    public TextFieldWidget usingScrollStep(boolean usingScrollStep) {
        this.usingScrollStep = usingScrollStep;
        return this;
    }

    /**
     * Normally, Tooltips on text field widgets are used to display the contents of the widget when the scrollbar is active
     * This value is an override, that allows the methods provided by {@link com.cleanroommc.modularui.api.widget.ITooltip} to be used
     * Every method that adds a tooltip from ITooltip is overridden to enable the tooltipOverride
     *
     * @param value - sets the tooltip override on or off
     */
    public TextFieldWidget setTooltipOverride(boolean value) {
        this.tooltipOverride = value;
        return this;
    }

    @Override
    public TextFieldWidget tooltipBuilder(Consumer<RichTooltip> tooltipBuilder) {
        tooltipOverride = true;
        return super.tooltipBuilder(tooltipBuilder);
    }

    @Override
    public TextFieldWidget tooltip(RichTooltip tooltip) {
        tooltipOverride = true;
        return super.tooltip(tooltip);
    }

    @Override
    public TextFieldWidget tooltip(Consumer<RichTooltip> tooltipConsumer) {
        tooltipOverride = true;
        return super.tooltip(tooltipConsumer);
    }

    @Override
    public @NotNull RichTooltip tooltip() {
        tooltipOverride = true;
        return super.tooltip();
    }

    @Override
    public TextFieldWidget tooltipDynamic(Consumer<RichTooltip> tooltipBuilder) {
        tooltipOverride = true;
        return super.tooltipDynamic(tooltipBuilder);
    }

    @Override
    public TextFieldWidget addTooltipDrawableLines(Iterable<IDrawable> lines) {
        tooltipOverride = true;
        return super.addTooltipDrawableLines(lines);
    }

    @Override
    public TextFieldWidget addTooltipElement(String s) {
        tooltipOverride = true;
        return super.addTooltipElement(s);
    }

    @Override
    public TextFieldWidget addTooltipElement(IDrawable drawable) {
        tooltipOverride = true;
        return super.addTooltipElement(drawable);
    }

    @Override
    public TextFieldWidget addTooltipLine(String line) {
        tooltipOverride = true;
        return super.addTooltipLine(line);
    }

    @Override
    public TextFieldWidget addTooltipLine(ITextLine line) {
        tooltipOverride = true;

        return super.addTooltipLine(line);
    }

    @Override
    public TextFieldWidget addTooltipLine(IDrawable drawable) {
        tooltipOverride = true;
        return super.addTooltipLine(drawable);
    }

    @Override
    public TextFieldWidget addTooltipStringLines(Iterable<String> lines) {
        tooltipOverride = true;
        return super.addTooltipStringLines(lines);
    }

    public Function<String, String> getValidator() {
        return validator;
    }

    public boolean isNumbers() {
        return numbers;
    }

    public double getDefaultNumber() {
        return defaultNumber;
    }

    public boolean isTooltipOverride() {
        return tooltipOverride;
    }

    public boolean isAcceptsExpression() {
        return acceptsExpression;
    }
}
