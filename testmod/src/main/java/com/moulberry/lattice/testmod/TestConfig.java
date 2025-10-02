package com.moulberry.lattice.testmod;

import com.moulberry.lattice.LatticeDynamicFrequency;
import com.moulberry.lattice.WidgetFunction;
import com.moulberry.lattice.annotation.LatticeCategory;
import com.moulberry.lattice.annotation.LatticeFormatValues;
import com.moulberry.lattice.annotation.constraint.LatticeDisableIf;
import com.moulberry.lattice.annotation.constraint.LatticeFloatRange;
import com.moulberry.lattice.annotation.constraint.LatticeHideIf;
import com.moulberry.lattice.annotation.constraint.LatticeIntRange;
import com.moulberry.lattice.annotation.constraint.LatticeShowIf;
import com.moulberry.lattice.annotation.widget.*;
import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.keybind.LatticeInputType;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TestConfig {

    @LatticeWidgetMessage
    public transient Component message = Component.literal("Welcome to the Lattice test config!");

    @LatticeOption(title = "Root", description = "This is an option present at the root level. It should use the default category name 'General'", translate = false)
    @LatticeWidgetButton
    public boolean rootButton = false;

    @LatticeOption(title = "Formatting", description = "This has custom value formatting `-> 0x%X <-`", translate = false)
    @LatticeIntRange(min = 0, max = 0xFFFF)
    @LatticeFormatValues(formattingString = "-> 0x%X <-")
    @LatticeWidgetSlider
    public int withFormatting = 0xBEEF;

    @LatticeOption(title = "Custom", description = "This is custom widget", translate = false)
    @LatticeWidgetCustom(function = "customWidgetFunction")
    public int customValue = 2;

    private WidgetFunction customWidgetFunction(Supplier<Integer> supplier, Consumer<Integer> consumer) {
        return WidgetFunction.cycleButton(supplier, consumer, 1, 2, 3);
    }

    @LatticeCategory(name = "Booleans", translate = false)
    public Booleans booleans = new Booleans();

    public static class Booleans {
        @LatticeOption(title = "Boolean", description = "This is a boolean as a button", translate = false)
        @LatticeWidgetButton
        public boolean booleanButton = false;
    }

    @LatticeCategory(name = "Numbers", translate = false)
    public Numbers numbers = new Numbers();

    public static class Numbers {
        @LatticeOption(title = "Int", description = "This is an int as a slider", translate = false)
        @LatticeIntRange(min = 0, max = 32, clampMin = 0, clampMax = 256)
        @LatticeWidgetSlider
        public int intSlider = 8;

        @LatticeOption(title = "Int with step", description = "This one has a step of 2", translate = false)
        @LatticeIntRange(min = 0, max = 32, step = 2, clampMin = 0, clampMax = 256)
        @LatticeWidgetSlider
        public int intSliderWithStep = 8;

        @LatticeOption(title = "Float", description = "This is an float as a slider", translate = false)
        @LatticeFloatRange(min = 0, max = 32, clampMin = 0, clampMax = 256)
        @LatticeWidgetSlider
        public float floatSlider = 8f;

        @LatticeOption(title = "Float with step", description = "This one has a step of 0.2", translate = false)
        @LatticeFloatRange(min = 0, max = 32, step = "0.2", clampMin = 0, clampMax = 256)
        @LatticeWidgetSlider
        public float floatSliderWithStep = 8f;
    }

    @LatticeCategory(name = "Enums", translate = false)
    public Enums enums = new Enums();

    public static class Enums {
        @LatticeOption(title = "Enum", description = "This is an enum as a button", translate = false)
        @LatticeWidgetButton
        public Fruit enumButton = Fruit.APPLE;

        @LatticeOption(title = "Enum", description = "This is an enum as a slider", translate = false)
        @LatticeWidgetSlider
        public Fruit enumSlider = Fruit.APPLE;

        @LatticeOption(title = "Enum", description = "This is an enum as a dropdown", translate = false)
        @LatticeWidgetDropdown
        public Fruit enumDropdown = Fruit.APPLE;
    }

    @LatticeCategory(name = "Text", translate = false)
    public Text text = new Text();

    public static class Text {
        @LatticeOption(title = "Single line", description = "This is a single line text field", translate = false)
        @LatticeWidgetTextField
        public String singleLine1 = "Lorem ipsum dolor sit amet";

        @LatticeOption(title = "Single line with max length", description = "This is another with a max length of 32", translate = false)
        @LatticeWidgetTextField(characterLimit = 32)
        public String singleLine2 = "Consectetur adipiscing elit";

        @LatticeOption(title = "Multi-line", description = "This is a multi-line text area", translate = false)
        @LatticeWidgetTextArea
        public String multiline = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    }

    @LatticeCategory(name = "Keybinds", translate = false)
    public Keybinds keybinds = new Keybinds();

    public static class Keybinds {
//        @LatticeOption(title = "Vanilla", description = "This is a vanilla KeyMapping. It doesn't support modifiers like ctrl/alt/etc", translate = false)
//        @LatticeWidgetKeybind
//        public KeyMapping vanillaKeyMapping = new KeyMapping("Vanilla", GLFW.GLFW_KEY_H, "dummy");
//
//        @LatticeOption(title = "Vanilla 2", description = "This is also a vanilla KeyMapping. Use this to test conflicts", translate = false)
//        @LatticeWidgetKeybind
//        public KeyMapping vanillaKeyMapping2 = new KeyMapping("Vanilla 2", GLFW.GLFW_KEY_J, "dummy");

        @LatticeOption(title = "Custom", description = "This is a custom keybind type. It supports modifiers", translate = false)
        @LatticeWidgetKeybind(allowModifiers = true)
        public CustomKeybind customKeybind = new CustomKeybind(LatticeInputType.KEYSYM, GLFW.GLFW_KEY_J, false, true, false, false);
    }

    @LatticeCategory(name = "Dynamic", translate = false)
    public Dynamic dynamic = new Dynamic();

    public static class Dynamic {
        @LatticeOption(title = "Disable following options", description = "If this is set to on, the following options will become disabled", translate = false)
        @LatticeWidgetButton
        public boolean disableNext = false;

        @LatticeOption(title = "Show following options", description = "If this is set to off, the following options will disappear", translate = false)
        @LatticeWidgetButton
        public boolean showNext = true;

        @LatticeOption(title = "I don't feel so good...", translate = false)
        @LatticeWidgetButton
        @LatticeShowIf(function = "checkShowNext", frequency = LatticeDynamicFrequency.EVERY_TICK)
        @LatticeDisableIf(function = "checkDisableNext", frequency = LatticeDynamicFrequency.EVERY_TICK)
        public boolean test1 = false;

        @LatticeCategory(name = "Subcategory", translate = false)
        @LatticeShowIf(function = "checkShowNext", frequency = LatticeDynamicFrequency.EVERY_TICK)
        @LatticeDisableIf(function = "checkDisableNext", frequency = LatticeDynamicFrequency.EVERY_TICK)
        public Subcategories subcategories = new Subcategories();

        private boolean checkDisableNext() {
            return this.disableNext;
        }

        private boolean checkShowNext() {
            return this.showNext;
        }
    }

    @LatticeCategory(name = "Subcategories", translate = false)
    public Subcategories subcategories = new Subcategories();

    public static class Subcategories {
        @LatticeCategory(name = "Subcategory", translate = false)
        public Subcategory subcategory = new Subcategory();

        public static class Subcategory {
            @LatticeOption(title = "Boolean in subcategory", description = "This boolean is part of the subcategory", translate = false)
            @LatticeWidgetButton
            public boolean subcategoryBool = false;

            @LatticeCategory(name = "Subsubcategory", translate = false)
            public Subsubcategory subsubcategory = new Subsubcategory();

            public static class Subsubcategory {
                @LatticeOption(title = "Boolean in subsubcategory", description = "This boolean is part of the subsubcategory", translate = false)
                @LatticeWidgetButton
                public boolean subsubcategoryBool = false;
            }

            @LatticeCategory(name = "Subsubcategory 2", translate = false)
            public Subsubcategory subsubcategory2 = new Subsubcategory();

            @LatticeCategory(name = "Subsubcategory 3", translate = false)
            public Subsubcategory subsubcategory3 = new Subsubcategory();
        }

        @LatticeCategory(name = "Other Subcategory", translate = false)
        public OtherSubcategory otherSubcategory = new OtherSubcategory();

        @LatticeCategory(name = "Other Subcategory 2", translate = false)
        public OtherSubcategory otherSubcategory2 = new OtherSubcategory();

        @LatticeCategory(name = "Other Subcategory 3", translate = false)
        public OtherSubcategory otherSubcategory3 = new OtherSubcategory();

        public static class OtherSubcategory {
            @LatticeOption(title = "Boolean in other subcategory", description = "This boolean is part of the other subcategory", translate = false)
            @LatticeWidgetButton
            public boolean otherSubcategoryBool = false;
        }
    }

//    @LatticeOption(title = "print", description = "prints the current config to stdout", translate = false)
//    @LatticeWidgetButton
//    public transient Runnable print = () -> System.out.println(TestConfig.this);
//
//    @Override
//    public String toString() {
//        return "TestConfig{" +
//                "enumDropdown=" + enumDropdown +
//                ", enumSlider=" + enumSlider +
//                ", enumButton=" + enumButton +
//                ", intSlider=" + intSlider +
//                ", booleanButton=" + booleanButton +
//                '}';
//    }

}
