package com.moulberry.lattice.widget;

import com.moulberry.lattice.keybind.KeybindInterface;
import com.moulberry.lattice.keybind.LatticeInputType;
import com.moulberry.lattice.multiversion.LatticeMultiversion;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.function.Supplier;

@ApiStatus.Internal
public class KeybindButton extends Button implements WidgetExtraFunctionality {

    private final Component title;
    private final KeybindInterface keybindInterface;
    private final boolean allowModifiers;
    private boolean editing = false;
    private int lastPressedModKey = -1;

    private Collection<Component> lastConflicts = null;

    public KeybindButton(int x, int y, int width, int height, Component title, boolean allowModifiers, KeybindInterface keybindInterface) {
        super(x, y, width, height, title, button -> ((KeybindButton)button).handlePress(), Supplier::get);
        this.allowModifiers = allowModifiers;
        this.title = title;
        this.keybindInterface = keybindInterface;
        this.updateMessage();
    }

    @Override
    public boolean listeningForRawKeyInput() {
        return this.editing;
    }

    @Override
    public boolean handleRawInput(LatticeInputType inputType, int value, boolean release) {
        if (release) {
            if (this.allowModifiers && inputType == LatticeInputType.KEYSYM && value == this.lastPressedModKey) {
                this.editing = false;
                return true;
            }
            return false;
        }

        if (inputType == LatticeInputType.KEYSYM && value == GLFW.GLFW_KEY_ESCAPE) {
            this.keybindInterface.setUnbound();
            this.editing = false;
            return true;
        }

        long window = LatticeMultiversion.getWindowHandle();
        boolean shiftMod = false;
        boolean ctrlMod = false;
        boolean altMod = false;
        boolean superMod = false;

        if (this.allowModifiers) {
            shiftMod = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) != GLFW.GLFW_RELEASE ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) != GLFW.GLFW_RELEASE;
            ctrlMod = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) != GLFW.GLFW_RELEASE ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) != GLFW.GLFW_RELEASE;
            altMod = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) != GLFW.GLFW_RELEASE ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) != GLFW.GLFW_RELEASE;
            superMod = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SUPER) != GLFW.GLFW_RELEASE ||
                    GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SUPER) != GLFW.GLFW_RELEASE;
        }

        boolean continueEditing = false;

        if (this.allowModifiers && inputType == LatticeInputType.KEYSYM) {
            if (value == GLFW.GLFW_KEY_LEFT_SHIFT || value == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                shiftMod = false;
                continueEditing = true;
                this.lastPressedModKey = value;
            }
            if (value == GLFW.GLFW_KEY_LEFT_CONTROL || value == GLFW.GLFW_KEY_RIGHT_CONTROL) {
                ctrlMod = false;
                continueEditing = true;
                this.lastPressedModKey = value;
            }
            if (value == GLFW.GLFW_KEY_LEFT_ALT || value == GLFW.GLFW_KEY_RIGHT_ALT) {
                altMod = false;
                continueEditing = true;
                this.lastPressedModKey = value;
            }
            if (value == GLFW.GLFW_KEY_LEFT_SUPER || value == GLFW.GLFW_KEY_RIGHT_SUPER) {
                superMod = false;
                continueEditing = true;
                this.lastPressedModKey = value;
            }
        }

        this.keybindInterface.setKey(inputType, value, shiftMod, ctrlMod, altMod, superMod);

        if (continueEditing) {
            this.updateMessage();
            return false;
        }

        this.editing = false;
        return true;
    }

    @Override
    public void afterRawInputHandledByAny() {
        this.updateMessage();
    }

    public void updateMessage() {
        this.setTooltip(null);

        Component message = this.keybindInterface.getKeyMessage();
        if (this.editing) {
            message = Component.literal("> ")
                    .append(message.copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                    .append(" <")
                    .withStyle(ChatFormatting.YELLOW);
            this.setTooltip(null);
        } else {
            Collection<Component> conflicts = this.keybindInterface.getConflicts();

            if (conflicts == null || conflicts.isEmpty()) {
                this.setTooltip(null);
            } else if (this.lastConflicts != conflicts) {
                this.lastConflicts = conflicts;

                MutableComponent conflictsMessage = Component.empty();

                boolean first = true;
                for (Component conflict : conflicts) {
                    if (first) {
                        first = false;
                    } else {
                        conflictsMessage.append(", ");
                    }
                    conflictsMessage.append(conflict);
                }

                this.setTooltip(Tooltip.create(Component.translatable("controls.keybinds.duplicateKeybinds", conflictsMessage)));

                message = Component.literal("[ ")
                        .append(message.copy().withStyle(ChatFormatting.WHITE))
                        .append(" ]").withStyle(ChatFormatting.RED);
            }
        }
        this.setMessage(Component.translatable("options.generic_value", this.title, message));
    }

    public void handlePress() {
        this.editing = true;
        this.updateMessage();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused && this.editing) {
            this.editing = false;
            this.updateMessage();
        }
    }

}
