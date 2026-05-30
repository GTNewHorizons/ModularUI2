package com.cleanroommc.modularui.utils;

import com.cleanroommc.modularui.utils.math.CustomDataAccessor;
import com.cleanroommc.modularui.utils.math.PostfixPercentOperator;

import com.cleanroommc.modularui.widgets.textfield.INumberParser;

import com.google.common.math.LongMath;

import net.minecraft.util.MathHelper;

import com.ezylang.evalex.BaseException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.function.LongSupplier;

public class MathUtils {

    public static final float PI = (float) Math.PI;
    public static final float PI2 = 2f * PI;
    public static final float PI_HALF = PI / 2f;
    public static final float PI_QUART = PI / 4f;

    public static final ExpressionConfiguration MATH_CFG = ExpressionConfiguration.builder()
            .arraysAllowed(false)
            .structuresAllowed(false)
            .stripTrailingZeros(true)
            .allowOverwriteConstants(true)
            .dataAccessorSupplier(() -> new CustomDataAccessor(false))
            .build()
            .withAdditionalOperators(Pair.of("%", new PostfixPercentOperator()));

    public static final ExpressionConfiguration MATH_CFG_CASE_SENSITIVE = MATH_CFG.toBuilder()
            .dataAccessorSupplier(() -> new CustomDataAccessor(true))
            .build();

    public static final INumberParser PARSER_WITH_SI = MathUtils::parseExpression;
    public static final INumberParser PARSER_WHOLE_NUMBER = MathUtils::parseExpressionWholeNumber;

    public static ParseResult parseExpression(String expression, double defaultValue) {
        return parseExpression(expression, defaultValue, true, false);
    }

    public static ParseResult parseExpression(String expression, double defaultValue, boolean useSiPrefixes, boolean biggerThanOne) {
        if (expression == null || expression.isEmpty()) {
            return ParseResult.success(EvaluationValue.numberValue(new BigDecimal(defaultValue)));
        }

        Expression e = new Expression(expression, MATH_CFG_CASE_SENSITIVE);
        if (useSiPrefixes) {
            SIPrefix.addAllToExpression(e, biggerThanOne);
        }
        try {
            return ParseResult.success(e.evaluate());
        } catch (BaseException exception) {
            return ParseResult.failure(exception);
        }
    }

    public static ParseResult parseExpressionWholeNumber(String expression, double defaultValue) {
        if (expression == null || expression.isEmpty()) {
            return ParseResult.success(EvaluationValue.numberValue(new BigDecimal(defaultValue)));
        }

        Expression e = new Expression(expression, MATH_CFG);
        SIPrefix.Kilo.addToExpression(e);
        SIPrefix.Mega.addToExpression(e);
        SIPrefix.Giga.addToExpression(e, "b");
        SIPrefix.Tera.addToExpression(e);
        e.with("i", 144); // ingot
        e.with("s", 64); // stack
        try {
            return ParseResult.success(e.evaluate());
        } catch (BaseException exception) {
            return ParseResult.failure(exception);
        }
    }

    public static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(v, max));
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }

    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(v, max));
    }

    public static long clamp(long v, long min, long max) {
        return Math.max(min, Math.min(v, max));
    }

    public static int cycler(int x, int min, int max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static float cycler(float x, float min, float max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static double cycler(double x, double min, double max) {
        return x < min ? max : (x > max ? min : x);
    }

    public static int gridIndex(int x, int y, int size, int width) {
        x = x / size;
        y = y / size;

        return x + y * width / size;
    }

    public static int gridRows(int count, int size, int width) {
        double x = count * size / (double) width;

        return count <= 0 ? 1 : (int) Math.ceil(x);
    }

    public static int min(int... values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return values[0];
        if (values.length == 2) return Math.min(values[0], values[1]);
        int min = Integer.MAX_VALUE;
        for (int i : values) {
            if (i < min) {
                min = i;
            }
        }
        return min;
    }

    public static int max(int... values) {
        if (values == null || values.length == 0) throw new IllegalArgumentException();
        if (values.length == 1) return values[0];
        if (values.length == 2) return Math.max(values[0], values[1]);
        int max = Integer.MIN_VALUE;
        for (int i : values) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }

    public static int ceil(float value) {
        int i = (int) value;
        return value > (float) i ? i + 1 : i;
    }

    public static int ceil(double value) {
        int i = (int) value;
        return value > (double) i ? i + 1 : i;
    }

    /**
     * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
     */
    public static float wrapDegrees(float value) {
        value = value % 360.0F;
        if (value >= 180.0F) value -= 360.0F;
        if (value < -180.0F) value += 360.0F;
        return value;
    }

    /**
     * the angle is reduced to an angle between -180 and +180 by mod, and a 360 check
     */
    public static double wrapDegrees(double value) {
        value = value % 360.0D;
        if (value >= 180.0D) value -= 360.0D;
        if (value < -180.0D) value += 360.0D;
        return value;
    }

    /**
     * Adjust the angle so that his value is in range [-180;180[
     */
    public static int wrapDegrees(int angle) {
        angle = angle % 360;
        if (angle >= 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }

    public static float sin(float v) {
        // MathHelper.sin doesn't account for negative numbers
        // with the point symmetry property of sin we can easily fix it
        // if v is negative, negate the input and then the output
        float s = Math.signum(v);
        return s * MathHelper.sin(s * v);
    }

    public static float cos(float v) {
        // MathHelper.cos doesn't account for negative numbers
        // with the axis symmetry property of cos we can easily fix it
        return MathHelper.cos(Math.abs(v));
    }

    public static float tan(float v) {
        return sin(v) / cos(v);
    }

    public static double sqrt(double v) {
        return Math.sqrt(v);
    }

    public static float sqrt(float v) {
        return (float) Math.sqrt(v);
    }

    public static float arithmeticGeometricMean(float a, float b) {
        return arithmeticGeometricMean(a, b, 5);
    }

    public static float arithmeticGeometricMean(float a, float b, int iterations) {
        a = (a + b) / 2;
        b = sqrt(a * b);
        if (--iterations == 0) return a;
        return arithmeticGeometricMean(a, b, iterations);
    }

    public static double rescaleLinear(double v, double fromMin, double fromMax, double toMin, double toMax) {
        v = (v - fromMin) / (fromMax - fromMin); // reverse lerp
        return toMin + (toMax - toMin) * v; // forward lerp
    }

    public static float rescaleLinear(float v, float fromMin, float fromMax, float toMin, float toMax) {
        v = (v - fromMin) / (fromMax - fromMin); // reverse lerp
        return toMin + (toMax - toMin) * v; // forward lerp
    }

    public static int intPlaces(BigDecimal x) {
        return Math.max(1, x.precision() - x.scale());
    }

    public static int intPlaces(double x) {
        if (x == 0.0) return 1;
        x = Math.abs(x);
        int d = (int) Math.floor(Math.log10(x)) + 1;
        // correct rounding errors
        if (Math.pow(10, d - 1) > x) d--;
        return Math.max(d, 1);
    }

    public static boolean areBothSmallerOrBiggerThanOne(double a, double b) {
        return a > 1 ? b > 1 : b <= 1;
    }

    public static long percentOrSelf(double value, long maxValue) {
        long rounded = Math.round(value);
        if (Math.abs(value - rounded) < 0.0000001) return rounded;
        return Math.round(value * maxValue);
    }

    public static int castToIntSaturated(long l) {
        if (l >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (l <= Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) l;
    }

    public static short castToShortSaturated(long l) {
        if (l >= Short.MAX_VALUE) return Short.MAX_VALUE;
        if (l <= Short.MIN_VALUE) return Short.MIN_VALUE;
        return (short) l;
    }

    public static byte castToByteSaturated(long l) {
        if (l >= Byte.MAX_VALUE) return Byte.MAX_VALUE;
        if (l <= Byte.MIN_VALUE) return Byte.MIN_VALUE;
        return (byte) l;
    }

    public interface UnaryLongOperator {

        UnaryLongOperator IDENTITY = v -> v;

        long apply(long l);
    }

    public interface UnaryIntOperator {

        UnaryIntOperator IDENTITY = v -> v;

        int apply(int l);
    }
}
