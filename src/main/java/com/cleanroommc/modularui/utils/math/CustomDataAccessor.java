package com.cleanroommc.modularui.utils.math;

import com.ezylang.evalex.data.DataAccessorIfc;
import com.ezylang.evalex.data.EvaluationValue;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Locale;
import java.util.Map;

public class CustomDataAccessor implements DataAccessorIfc {

    private final Map<String, EvaluationValue> map = new Object2ObjectOpenHashMap<>();
    private final boolean caseSensitive;

    public CustomDataAccessor(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    @Override
    public EvaluationValue getData(String variable) {
        if (this.caseSensitive) {
            return this.map.get(variable);
        }
        return this.map.get(variable.toLowerCase(Locale.ROOT));
    }

    @Override
    public void setData(String variable, EvaluationValue value) {
        if (this.caseSensitive) {
            this.map.put(variable, value);
        } else {
            this.map.put(variable, value);
            String lower = variable.toLowerCase(Locale.ROOT);
            if (!lower.equals(variable)) {
                this.map.put(lower, value);
            }
            String upper = variable.toUpperCase(Locale.ROOT);
            if (!upper.equals(variable)) {
                this.map.put(upper, value);
            }
        }
    }
}
