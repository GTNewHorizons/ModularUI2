package com.cleanroommc.modularui.screen.viewport;

import java.util.ArrayList;
import java.util.List;

final class DeferredRenderQueue {

    private final List<Runnable> renderCalls = new ArrayList<>();

    void queue(Runnable renderCall) {
        this.renderCalls.add(renderCall);
    }

    void drawQueued() {
        try {
            this.renderCalls.forEach(Runnable::run);
        } finally {
            this.renderCalls.clear();
        }
    }
}
