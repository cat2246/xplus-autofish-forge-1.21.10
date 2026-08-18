package com.wudji.xplusautofish.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

/** A labelled, narrated row containing exactly one native Minecraft control. */
public final class OptionRow extends ContainerObjectSelectionList.Entry<OptionRow> {
    private static final int ROW_HEIGHT = 32;
    private static final int SECTION_HEIGHT = 20;
    private final Font font;
    private final Component label;
    private final Component section;
    private final AbstractWidget control;
    private final List<GuiEventListener> children;
    private final List<NarratableEntry> narratables;

    public OptionRow(Font font, Component label, AbstractWidget control, List<Component> tooltipLines) {
        this(font, null, label, control, tooltipLines);
    }

    public OptionRow(Font font, Component section, Component label, AbstractWidget control,
                     List<Component> tooltipLines) {
        this.font = font;
        this.section = section;
        this.label = label;
        this.control = control;
        this.children = List.of(control);
        this.narratables = List.of(control);
        if (!tooltipLines.isEmpty()) {
            String tooltip = tooltipLines.stream().map(Component::getString).reduce(
                    (left, right) -> left + "\n" + right).orElse("");
            control.setTooltip(Tooltip.create(Component.literal(tooltip)));
        }
        setHeight(getHeight());
    }

    public AbstractWidget control() {
        return control;
    }

    @Override
    public int getHeight() {
        return (section == null ? 0 : SECTION_HEIGHT) + ROW_HEIGHT;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        control.setX(x + getWidth() - control.getWidth() - 4);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        control.setY(y + (section == null ? 0 : SECTION_HEIGHT) + (ROW_HEIGHT - control.getHeight()) / 2);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        control.setX(getX() + width - control.getWidth() - 4);
    }

    @Override
    public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float delta) {
        int controlTop = getY() + (section == null ? 0 : SECTION_HEIGHT);
        if (section != null) {
            graphics.drawString(font, section, getX() + 4, getY() + 3, 0xFFFFFFFF);
        }
        graphics.drawString(font, label, getX() + 4, controlTop + (ROW_HEIGHT - 9) / 2, 0xFFFFFFFF);
        control.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return narratables;
    }
}
