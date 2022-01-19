package io.github.cleanroommc.modularui.builder;

import io.github.cleanroommc.modularui.api.IContainerCreator;
import io.github.cleanroommc.modularui.api.IGuiCreator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

public class UIBuilder<CC extends Container, GC extends GuiContainer> {

	private static final IContainerCreator<?> DUMMY_CONTAINER_CREATOR = (player, world, x, y, z) -> null;
	private static final IGuiCreator<?> DUMMY_GUI_CREATOR = (player, world, x, y, z) -> null;

	public static <CC extends Container, GC extends GuiScreen> UIBuilder<CC, GC> of() {
		return new UIBuilder<>();
	}

	private IContainerCreator<CC> containerCreator = (IContainerCreator<CC>) DUMMY_CONTAINER_CREATOR;
	private IGuiCreator<GC> guiCreator = (IGuiCreator<GC>) DUMMY_GUI_CREATOR;

	private UIBuilder() {

	}

	public UIBuilder<CC, GC> container(IContainerCreator<CC> containerCreator) {
		this.containerCreator = containerCreator;
		return this;
	}

	public UIBuilder<CC, GC> gui(IGuiCreator<GC> guiCreator) {
		this.guiCreator = guiCreator;
		return this;
	}

	public UIInfo<IContainerCreator<CC>, IGuiCreator<GC>> build() {
		return new UIInfo<>(this.containerCreator, this.guiCreator);
	}

}
