package com.moulberry.lattice.element;

import com.mojang.blaze3d.platform.InputConstants;
import com.moulberry.lattice.widget.EditableSlider;
import com.moulberry.lattice.widget.KeybindButton;
import com.moulberry.lattice.WidgetFunction;
import com.moulberry.lattice.annotation.LatticeCategory;
import com.moulberry.lattice.annotation.LatticeOption;
import com.moulberry.lattice.annotation.constraint.LatticeDisableIf;
import com.moulberry.lattice.annotation.constraint.LatticeFloatRange;
import com.moulberry.lattice.annotation.constraint.LatticeHideIf;
import com.moulberry.lattice.annotation.constraint.LatticeIntRange;
import com.moulberry.lattice.annotation.widget.*;
import com.moulberry.lattice.keybind.KeybindInterface;
import com.moulberry.lattice.keybind.LatticeInputType;
import com.moulberry.lattice.widget.DiscreteSlider;
import com.moulberry.lattice.widget.DropdownWidget;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ApiStatus.Internal
class ElementReflection {

    private static final Set<Class<? extends Annotation>> widgetAnnotations = Set.of(
        LatticeWidgetButton.class,
        LatticeWidgetSlider.class,
        LatticeWidgetDropdown.class,
        LatticeWidgetKeybind.class,
        LatticeWidgetTextField.class,
        LatticeWidgetTextArea.class
    );

    private static final Set<Class<? extends Annotation>> otherAnnotations = Set.of(
        LatticeIntRange.class,
        LatticeFloatRange.class
    );

    static void addElementsFromClass(Object config, LatticeElements elements, List<KeyMapping> keyMappings) throws LatticeFieldToOptionException {
        for (Field field : config.getClass().getDeclaredFields()) {
            try {
                addElementsFromField(config, field, elements, keyMappings);
            } catch (IllegalAccessException e) {
                continue;
            } catch (LatticeFieldToOptionException e) {
                throw e;
            } catch (Exception e) {
                throw new LatticeFieldToOptionException("Unable to create lattice option for field " + field.getName() + " in " + field.getDeclaringClass().getName(), e);
            }
        }
    }

    static void addElementsFromField(Object config, Field field, LatticeElements elements, List<KeyMapping> keyMappings) throws IllegalAccessException, LatticeFieldToOptionException {
        Class<?> fieldType = field.getType();

        // Ignore static fields
        if ((field.getModifiers() & Modifier.STATIC) != 0) {
            return;
        }

        LatticeHideIf latticeHideIf = field.getDeclaredAnnotation(LatticeHideIf.class);
        if (latticeHideIf != null) {
            String functionName = latticeHideIf.function();
            if (findAndInvokeBooleanFunction(config, latticeHideIf, functionName)) return;
        }

        boolean disabled = false;
        LatticeDisableIf latticeDisableIf = field.getDeclaredAnnotation(LatticeDisableIf.class);
        if (latticeDisableIf != null) {
            String functionName = latticeDisableIf.function();
            disabled = findAndInvokeBooleanFunction(config, latticeDisableIf, functionName);
        }

        LatticeCategory latticeCategory = field.getDeclaredAnnotation(LatticeCategory.class);
        if (latticeCategory != null) {
            Object childConfig = field.get(config);

            String nameString = latticeCategory.name();
            Component nameComponent;
            if (latticeCategory.translate()) {
                nameComponent = Component.translatable(nameString);
            } else {
                nameComponent = Component.literal(nameString);
            }

            LatticeElements childElements = new LatticeElements(nameComponent);
            addElementsFromClass(childConfig, childElements, keyMappings);
            elements.subcategories.add(childElements);
            return;
        }

        LatticeOption latticeOption = field.getDeclaredAnnotation(LatticeOption.class);
        if (latticeOption == null) {
            return;
        }

        String titleString = latticeOption.title();
        String descriptionString = latticeOption.description();
        boolean translate = latticeOption.translate();

        Component titleComponent = translate ? Component.translatable(titleString) : Component.literal(titleString);

        Component descriptionComponent = null;
        if (descriptionString != null) {
            descriptionComponent = translate ? Component.translatable(descriptionString) : Component.literal(descriptionString);
        }

        Annotation widgetAnnotation = null;

        for (Annotation declaredAnnotation : field.getDeclaredAnnotations()) {
            if (widgetAnnotations.contains(declaredAnnotation.annotationType())) {
                if (widgetAnnotation != null) {
                    throw new RuntimeException("Duplicate @LatticeWidget annotation");
                }
                widgetAnnotation = declaredAnnotation;
            }
        }

        if (widgetAnnotation == null) {
            throw new RuntimeException("Missing @LatticeWidget annotation");
        }

        LatticeElement latticeElement = createLatticeElement(config, field, keyMappings, widgetAnnotation, titleComponent, descriptionComponent);
        latticeElement.withDisabled(disabled);
        elements.options.add(latticeElement);
    }

    private static boolean findAndInvokeBooleanFunction(Object config, Annotation annotation, String functionName) {
        Class<?> clazz = config.getClass();
        while (clazz != null) {
            try {
                Method method = config.getClass().getDeclaredMethod(functionName);

                if (method.getReturnType() != boolean.class && method.getReturnType() != Boolean.class) {
                    throw new RuntimeException("Error trying to check " + annotation + ": " + method + " does not return boolean");
                }

                try {
                    method.trySetAccessible();
                    return (Boolean) method.invoke(config);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            } catch (NoSuchMethodException ignored) {}
            clazz = config.getClass().getEnclosingClass();
        }

        throw new RuntimeException("Can't find function for " + annotation);
    }

    private static LatticeElement createLatticeElement(Object config, Field field, List<KeyMapping> keyMappings, Annotation widgetAnnotation, Component titleComponent, Component descriptionComponent) throws IllegalAccessException {
        Class<?> fieldType = field.getType();

        if (widgetAnnotation instanceof LatticeWidgetButton) {
            checkForUnexpectedAnnotations(field, LatticeWidgetButton.class);

            if (Runnable.class.isAssignableFrom(fieldType)) {
                Runnable runnable = (Runnable) field.get(config);
                return new LatticeElement(WidgetFunction.runnableButton(runnable), titleComponent, descriptionComponent);
            } else if (fieldType == boolean.class) {
                throwIfFinal(field);

                var widgetFunction = WidgetFunction.onOffButton(() -> {
                    try {
                        return field.getBoolean(config);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, value -> {
                    try {
                        field.setBoolean(config, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                });
                return new LatticeElement(widgetFunction, titleComponent, descriptionComponent);
            } else if (fieldType.isEnum()) {
                throwIfFinal(field);
                Object[] values = fieldType.getEnumConstants();

                var widgetFunction = WidgetFunction.cycleButton(() -> {
                    try {
                        return field.get(config);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, value -> {
                    try {
                        field.set(config, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, values);

                return new LatticeElement(widgetFunction, titleComponent, descriptionComponent);
            } else {
                throw new RuntimeException(fieldType + " isn't compatible with @LatticeWidgetButton");
            }
        } else if (widgetAnnotation instanceof LatticeWidgetSlider slider) {
            boolean allowAlternativeInput = slider.allowAlternateInput();
            if (fieldType == int.class) {
                throwIfFinal(field);
                checkForUnexpectedAnnotations(field, LatticeWidgetSlider.class, LatticeIntRange.class);

                LatticeIntRange intRange = field.getDeclaredAnnotation(LatticeIntRange.class);

                int min = intRange == null ? Integer.MIN_VALUE : intRange.min();
                int max = intRange == null ? Integer.MAX_VALUE : intRange.max();
                int clampMin = intRange == null ? Integer.MIN_VALUE : intRange.clampMin();
                int clampMax = intRange == null ? Integer.MAX_VALUE : intRange.clampMax();
                int step = intRange == null ? 1 : Math.max(1, intRange.step());
                int clampStep = intRange == null ? 0 : intRange.clampStep();

                if (max < min) {
                    throw new RuntimeException("max (" + max + ") is less than min (" + min + ")");
                }
                if (min + step > max) {
                    throw new RuntimeException("min (" + min + ") + step (" + step + ") is more than max (" + max + ")");
                }
                if (clampMax < clampMin) {
                    throw new RuntimeException("clampMax (" + clampMax + ") is less than clampMin (" + clampMin + ")");
                }
                if (clampMin + clampStep > clampMax) {
                    throw new RuntimeException("clampMin (" + clampMin + ") + clampStep (" + clampStep + ") is more than clampMax (" + clampMax + ")");
                }

                return new LatticeElement((font, title, description, width) -> {
                    try {
                        int initialValue = field.getInt(config);

                        return new EditableSlider<>(0, 0, width, 20, title, font, allowAlternativeInput, initialValue) {
                            @Override
                            public double toSliderRange(Integer value) {
                                return (value - min) / (double) (max - min);
                            }

                            @Override
                            public Integer fromSliderRange(double value) {
                                return min + (int) Math.round((value * (max - min)) / step) * step;
                            }

                            @Override
                            public Integer clampValue(Integer value) {
                                int clamped = Math.min(clampMax, Math.max(clampMin, value));
                                if (clampStep != 0) {
                                    clamped = (int) Math.round((double)(clamped - min) / clampStep) * clampStep + min;
                                    if (clamped < clampMin) clamped += clampStep;
                                    if (clamped > clampMax) clamped -= clampStep;
                                }
                                return clamped;
                            }

                            @Override
                            public Integer fromString(String value) {
                                return Integer.parseInt(value);
                            }

                            @Override
                            public void setValue(Integer value) {
                                try {
                                    field.setInt(config, value);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        };
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, titleComponent, descriptionComponent);
            } else if (fieldType == float.class) {
                throwIfFinal(field);
                checkForUnexpectedAnnotations(field, LatticeWidgetSlider.class, LatticeFloatRange.class);

                LatticeFloatRange floatRange = field.getDeclaredAnnotation(LatticeFloatRange.class);

                BigDecimal min = BigDecimal.valueOf(floatRange == null ? Float.MIN_VALUE : floatRange.min());
                BigDecimal max = BigDecimal.valueOf(floatRange == null ? Float.MAX_VALUE : floatRange.max());
                BigDecimal minMaxRange = max.subtract(min);
                BigDecimal clampMin = BigDecimal.valueOf(floatRange == null ? Float.MIN_VALUE : floatRange.clampMin());
                BigDecimal clampMax = BigDecimal.valueOf(floatRange == null ? Float.MAX_VALUE : floatRange.clampMax());
                BigDecimal step = floatRange == null ? BigDecimal.valueOf(1, 2) :
                        new BigDecimal(floatRange.step()).max(BigDecimal.valueOf(1, 7));
                BigDecimal clampStep = floatRange == null ? null : new BigDecimal(floatRange.clampStep());

                if (max.compareTo(min) < 0) {
                    throw new RuntimeException("max (" + max + ") is less than min (" + min + ")");
                }
                if (min.add(step).compareTo(max) > 0) {
                    throw new RuntimeException("min (" + min + ") + step (" + step + ") is more than max (" + max + ")");
                }
                if (clampMax.compareTo(clampMin) < 0) {
                    throw new RuntimeException("clampMax (" + clampMax + ") is less than clampMin (" + clampMin + ")");
                }
                if (clampMin.add(clampStep).compareTo(clampMax) > 0) {
                    throw new RuntimeException("clampMin (" + clampMin + ") + clampStep (" + clampStep + ") is more than clampMax (" + clampMax + ")");
                }

                BigDecimal clampMinWithStepScale = step.scale() > clampMin.scale() ? clampMin.setScale(step.scale(), RoundingMode.UNNECESSARY) : clampMin;
                BigDecimal clampMaxWithStepScale = step.scale() > clampMax.scale() ? clampMax.setScale(step.scale(), RoundingMode.UNNECESSARY) : clampMax;

                return new LatticeElement((font, title, description, width) -> {
                    try {
                        float floatValue = field.getFloat(config);
                        BigDecimal initialValue = BigDecimal.valueOf(floatValue);
                        if (step.scale() > initialValue.scale()) {
                            initialValue = initialValue.setScale(step.scale(), RoundingMode.UNNECESSARY);
                        } else {
                            // Try to round to the smallest scale that produces the same float value
                            for (int scale = step.scale(); scale < initialValue.scale(); scale++) {
                                BigDecimal rounded = initialValue.setScale(scale, RoundingMode.HALF_EVEN);
                                if (rounded.floatValue() == floatValue) {
                                    initialValue = rounded;
                                    break;
                                }
                            }
                        }

                        return new EditableSlider<>(0, 0, width, 20, title, font, allowAlternativeInput, initialValue) {
                            @Override
                            public double toSliderRange(BigDecimal value) {
                                return value.subtract(min).divide(minMaxRange, MathContext.DECIMAL128).doubleValue();
                            }

                            @Override
                            public BigDecimal fromSliderRange(double value) {
                                return BigDecimal.valueOf(value)
                                        .multiply(minMaxRange)
                                        .divide(step, MathContext.DECIMAL128)
                                        .setScale(0, RoundingMode.HALF_EVEN)
                                        .multiply(step)
                                        .add(min);
                            }

                            @Override
                            public BigDecimal clampValue(BigDecimal value) {
                                BigDecimal clamped = value.max(clampMinWithStepScale).min(clampMaxWithStepScale);
                                if (clampStep != null && clampStep.floatValue() != 0) {
                                    clamped = clamped.subtract(min)
                                            .divide(clampStep, MathContext.DECIMAL128)
                                            .setScale(0, RoundingMode.HALF_EVEN)
                                            .multiply(clampStep)
                                            .add(min);
                                    if (clamped.compareTo(clampMin) < 0) clamped = clamped.add(clampStep);
                                    if (clamped.compareTo(clampMax) > 0) clamped = clamped.subtract(clampStep);
                                }
                                return clamped;
                            }

                            @Override
                            public BigDecimal fromString(String value) {
                                return new BigDecimal(value);
                            }

                            @Override
                            public void setValue(BigDecimal value) {
                                try {
                                    field.setFloat(config, value.floatValue());
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        };
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, titleComponent, descriptionComponent);
            } else if (fieldType.isEnum()) {
                throwIfFinal(field);
                Object[] values = fieldType.getEnumConstants();

                if (values.length == 0) {
                    throw new RuntimeException("Can't create option for enum " + fieldType.getSimpleName() + " with zero elements");
                }

                return new LatticeElement((font, title, description, width) -> {
                    try {
                        Object initialValue = field.get(config);
                        return new DiscreteSlider<>(0, 0, width, 20, title, initialValue, values) {
                            @Override
                            public void setValue(Object value) {
                                try {
                                    field.set(config, value);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        };
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, titleComponent, descriptionComponent);
            } else {
                throw new RuntimeException(fieldType + " isn't compatible with @LatticeWidgetSlider");
            }
        } else if (widgetAnnotation instanceof LatticeWidgetDropdown) {
            checkForUnexpectedAnnotations(field, LatticeWidgetDropdown.class);

            if (fieldType.isEnum()) {
                throwIfFinal(field);
                Object[] values = fieldType.getEnumConstants();

                if (values.length == 0) {
                    throw new RuntimeException("Can't create option for enum " + fieldType.getSimpleName() + " with zero elements");
                }

                return new LatticeElement((font, title, description, width) -> {
                    try {
                        Object initialValue = field.get(config);
                        return new DropdownWidget<>(0, 0, width, 20, font, title, initialValue, values) {
                            @Override
                            public void setValue(Object value) {
                                try {
                                    field.set(config, value);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        };
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, titleComponent, descriptionComponent);
            } else {
                throw new RuntimeException(fieldType + " isn't compatible with @LatticeWidgetDropdown");
            }
        } else if (widgetAnnotation instanceof LatticeWidgetKeybind latticeWidgetKeybind) {
            checkForUnexpectedAnnotations(field, LatticeWidgetKeybind.class);

            if (KeyMapping.class.isAssignableFrom(fieldType)) {
                KeyMapping keyMapping = (KeyMapping) field.get(config);

                if (!keyMappings.contains(keyMapping)) {
                    keyMappings.add(keyMapping);
                }

                if (latticeWidgetKeybind.allowModifiers()) {
                    throw new RuntimeException("Unable to create lattice option for field " + field.getName() + " in " + field.getDeclaringClass().getName() + ": KeyMapping does not support modifiers");
                }

                return new LatticeElement((font, title, description, width) -> {
                    return new KeybindButton(0, 0, width, 20, title, false, new KeybindInterface() {
                        @Override
                        public Component getKeyMessage() {
                            return keyMapping.getTranslatedKeyMessage();
                        }

                        @Override
                        public void setKey(LatticeInputType type, int value, boolean shiftMod, boolean ctrlMod, boolean altMod, boolean superMod) {
                            var key = switch (type) {
                                case KEYSYM -> InputConstants.Type.KEYSYM.getOrCreate(value);
                                case SCANCODE -> InputConstants.Type.SCANCODE.getOrCreate(value);
                                case MOUSE -> InputConstants.Type.MOUSE.getOrCreate(value);
                            };
                            keyMapping.setKey(key);
                            KeyMapping.resetMapping();
                        }

                        @Override
                        public void setUnbound() {
                            keyMapping.setKey(InputConstants.UNKNOWN);
                            KeyMapping.resetMapping();
                        }

                        @Override
                        public Collection<Component> getConflicts() {
                            if (keyMapping.isUnbound()) {
                                return List.of();
                            }

                            List<Component> conflicts = new ArrayList<>();

                            for (KeyMapping other : keyMappings) {
                                if (other != keyMapping && keyMapping.same(other)) {
                                    conflicts.add(Component.translatable(other.getName()));
                                }
                            }

                            return conflicts;
                        }
                    });
                }, titleComponent, descriptionComponent);
            } else if (KeybindInterface.class.isAssignableFrom(fieldType)) {
                KeybindInterface keybindInterface = (KeybindInterface) field.get(config);

                return new LatticeElement((font, title, description, width) -> {
                    return new KeybindButton(0, 0, width, 20, title, latticeWidgetKeybind.allowModifiers(), keybindInterface);
                }, titleComponent, descriptionComponent);
            } else {
                throw new RuntimeException(fieldType + " isn't compatible with @LatticeWidgetKeybind");
            }
        } else if (widgetAnnotation instanceof LatticeWidgetTextField latticeWidgetTextField) {
            int maxLength = latticeWidgetTextField.characterLimit();
            if (fieldType == String.class) {
                return new LatticeElement(WidgetFunction.editBox(() -> {
                    try {
                        return (String) field.get(config);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, value -> {
                    try {
                        field.set(config, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, maxLength), titleComponent, descriptionComponent).withShowTitleSeparately(true);
            } else {
                throw new RuntimeException(fieldType + " isn't compatible with @LatticeWidgetTextField");
            }
        } else if (widgetAnnotation instanceof LatticeWidgetTextArea latticeWidgetTextArea) {
            int height = latticeWidgetTextArea.height();
            int characterLimit = latticeWidgetTextArea.characterLimit();
            if (fieldType == String.class) {
                return new LatticeElement(WidgetFunction.multilineEditBox(() -> {
                    try {
                        return (String) field.get(config);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, value -> {
                    try {
                        field.set(config, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }, height, characterLimit), titleComponent, descriptionComponent).withShowTitleSeparately(true);
            } else {
                throw new RuntimeException(fieldType + " isn't compatible with @LatticeWidgetTextArea");
            }
        } else {
            throw new RuntimeException("Missing @LatticeWidget annotation");
        }
    }

    private static void throwIfFinal(Field field) {
        if ((field.getModifiers() & Modifier.FINAL) != 0) {
            throw new RuntimeException("Field is final");
        }
    }

    private static void checkForUnexpectedAnnotations(Field field, Class<?>... expected) {
        Set<Class<?>> expectedSet = Set.of(expected);

        for (Annotation annotation : field.getDeclaredAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (otherAnnotations.contains(annotationType) && !expectedSet.contains(annotationType)) {
                throw new RuntimeException("Unexpected annotation @" + annotationType.getSimpleName());
            }
        }
    }

}
