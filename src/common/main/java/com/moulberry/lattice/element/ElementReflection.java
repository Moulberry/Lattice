package com.moulberry.lattice.element;

import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.moulberry.lattice.annotation.LatticeFormatValues;
import com.moulberry.lattice.annotation.constraint.LatticeEnableIf;
import com.moulberry.lattice.annotation.constraint.LatticeShowIf;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

@ApiStatus.Internal
public class ElementReflection {

    private static final Set<Class<? extends Annotation>> widgetAnnotations = Set.of(
        LatticeWidgetButton.class,
        LatticeWidgetCustom.class,
        LatticeWidgetDropdown.class,
        LatticeWidgetKeybind.class,
        LatticeWidgetMessage.class,
        LatticeWidgetSlider.class,
        LatticeWidgetTextArea.class,
        LatticeWidgetTextField.class
    );

    private static final Set<Class<? extends Annotation>> otherAnnotations = Set.of(
        LatticeIntRange.class,
        LatticeFloatRange.class
    );

    private record CachedConditionKey(Class<?> clazz, @Nullable Object object, String method) {}

    private final Map<CachedConditionKey, BooleanSupplier> conditions = new HashMap<>();
    private final KeyMappingListSupplier keyMappingListSupplier = new KeyMappingListSupplier();

    private static class KeyMappingListSupplier {
        private List<KeyMapping> keyMappingsDelayedRegister = new ArrayList<>();
        private List<KeyMapping> keyMappings = null;

        private void ensureRegistered(KeyMapping keyMapping) {
            List<KeyMapping> list = this.get();
            if (!list.contains(keyMapping)) {
                list.add(keyMapping);
            }
        }

        private List<KeyMapping> get() {
            if (this.keyMappings == null) {
                Options minecraftOptions = Minecraft.getInstance().options;
                if (minecraftOptions == null) {
                    return this.keyMappingsDelayedRegister;
                } else {
                    this.keyMappings = new ArrayList<>(Arrays.asList(minecraftOptions.keyMappings));

                    List<KeyMapping> delayedRegister = this.keyMappingsDelayedRegister;
                    this.keyMappingsDelayedRegister = null;

                    for (KeyMapping keyMapping : delayedRegister) {
                        this.ensureRegistered(keyMapping);
                    }
                }
            }

            return this.keyMappings;
        }
    }

    public void addElementsFromClass(Object config, LatticeElements elements) throws LatticeFieldToOptionException {
        for (Field field : config.getClass().getDeclaredFields()) {
            try {
                addElementsFromField(config, field, elements);
            } catch (IllegalAccessException e) {
                continue;
            } catch (LatticeFieldToOptionException e) {
                throw e;
            } catch (Exception e) {
                throw new LatticeFieldToOptionException("Unable to create lattice option for field " + field.getName() + " in " + field.getDeclaringClass().getName(), e);
            }
        }
    }

    public void addElementsFromField(Object config, Field field, LatticeElements elements) throws IllegalAccessException, LatticeFieldToOptionException {
        // Ignore static fields
        if ((field.getModifiers() & Modifier.STATIC) != 0) {
            return;
        }
        field.trySetAccessible();

        LatticeDynamicCondition hideDynamic = null;

        LatticeHideIf latticeHideIf = field.getDeclaredAnnotation(LatticeHideIf.class);
        LatticeShowIf latticeShowIf = field.getDeclaredAnnotation(LatticeShowIf.class);
        if (latticeHideIf != null && latticeShowIf != null) {
            throw new RuntimeException("Can't have both @LatticeHideIf and @LatticeShowIf");
        }
        if (latticeHideIf != null) {
            BooleanSupplier supplier = getOrCreateCondition(config, latticeHideIf, latticeHideIf.function());
            hideDynamic = new LatticeDynamicCondition(supplier, latticeHideIf.frequency());
        }
        if (latticeShowIf != null) {
            BooleanSupplier supplier = getOrCreateCondition(config, latticeShowIf, latticeShowIf.function());
            hideDynamic = new LatticeDynamicCondition(() -> !supplier.getAsBoolean(), latticeShowIf.frequency());
        }

        LatticeDynamicCondition disableDynamic = null;
        LatticeDisableIf latticeDisableIf = field.getDeclaredAnnotation(LatticeDisableIf.class);
        LatticeEnableIf latticeEnableIf = field.getDeclaredAnnotation(LatticeEnableIf.class);
        if (latticeDisableIf != null && latticeEnableIf != null) {
            throw new RuntimeException("Can't have both @LatticeDisableIf and @LatticeEnableIf");
        }
        if (latticeDisableIf != null) {
            BooleanSupplier supplier = getOrCreateCondition(config, latticeDisableIf, latticeDisableIf.function());
            disableDynamic = new LatticeDynamicCondition(supplier, latticeDisableIf.frequency());
        }
        if (latticeEnableIf != null) {
            BooleanSupplier supplier = getOrCreateCondition(config, latticeEnableIf, latticeEnableIf.function());
            disableDynamic = new LatticeDynamicCondition(() -> !supplier.getAsBoolean(), latticeEnableIf.frequency());
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
            this.addElementsFromClass(childConfig, childElements);
            elements.subcategories.add(childElements);
            childElements.hiddenDynamic(hideDynamic);
            childElements.disabledDynamic(disableDynamic);
            return;
        }

        // Special handling for LatticeWidgetMessage since it doesn't need @LatticeOption
        LatticeWidgetMessage latticeWidgetMessage = field.getDeclaredAnnotation(LatticeWidgetMessage.class);
        if (latticeWidgetMessage != null) {
            Component message;
            if (field.getType() == String.class) {
                message = Component.literal((String) field.get(config));
            } else if (Component.class.isAssignableFrom(field.getType())) {
                message = (Component) field.get(config);
            } else {
                throw new RuntimeException(field.getType() + " isn't compatible with @LatticeWidgetMessage");
            }

            LatticeElement latticeElement = new LatticeElement(WidgetFunction.string(latticeWidgetMessage.maxRows(), latticeWidgetMessage.centered()), message, null);
            latticeElement.disabledDynamic(disableDynamic);
            latticeElement.hiddenDynamic(hideDynamic);
            latticeElement.canBeSearched(false);
            elements.options.add(latticeElement);
            return;
        }

        LatticeOption latticeOption = field.getDeclaredAnnotation(LatticeOption.class);
        if (latticeOption == null) {
            return;
        }

        boolean translate = latticeOption.translate();

        String titleString = latticeOption.title();
        Component titleComponent = translate ? Component.translatable(titleString) : Component.literal(titleString);

        String descriptionString = latticeOption.description();
        Component descriptionComponent = null;
        if (descriptionString != null && !descriptionString.isEmpty()) {
            descriptionString = descriptionString.replace("!!", titleString);
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

        LatticeElement latticeElement = createLatticeElement(config, field, widgetAnnotation, titleComponent, descriptionComponent);
        latticeElement.disabledDynamic(disableDynamic);
        latticeElement.hiddenDynamic(hideDynamic);
        elements.options.add(latticeElement);
    }

    private static final Map<Class<?>, Class<?>> WRAPPER_TYPES = Map.of(
        int.class, Integer.class,
        long.class, Long.class,
        boolean.class, Boolean.class,
        short.class, Short.class,
        byte.class, Byte.class,
        char.class, Character.class,
        float.class, Float.class,
        double.class, Double.class
    );

    private <T> WidgetFunction createWidgetFunction(Object config, Annotation annotation, String functionName, Field field) {
        Method methodWithInvalidArguments = null;

        // todo: take Field instead of supplier

        Class<?> type = field.getType();
        Class<?> wrapperType = WRAPPER_TYPES.getOrDefault(type, type);

        Type supplierType = TypeToken.getParameterized(Supplier.class, wrapperType).getType();
        Type consumerType = TypeToken.getParameterized(Consumer.class, wrapperType).getType();
        Type primitiveSupplierType = null;
        Type primitiveConsumerType = null;

        if (field.getType() == int.class) {
            primitiveSupplierType = IntSupplier.class;
            primitiveConsumerType = IntConsumer.class;
        } else if (field.getType() == long.class) {
            primitiveSupplierType = LongSupplier.class;
            primitiveConsumerType = LongConsumer.class;
        } else if (field.getType() == boolean.class) {
            primitiveSupplierType = BooleanSupplier.class;
        } else if (field.getType() == double.class) {
            primitiveSupplierType = DoubleSupplier.class;
            primitiveConsumerType = DoubleConsumer.class;
        }

        Class<?> clazz = config.getClass();
        while (clazz != null) {
            Method method = null;

            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                if (declaredMethod.getName().equals(functionName)) {
                    if (isMethodValidForWidgetFunction(declaredMethod, consumerType, primitiveConsumerType, supplierType, primitiveSupplierType)) {
                        method = declaredMethod;
                        break;
                    } else if (methodWithInvalidArguments == null) {
                        methodWithInvalidArguments = declaredMethod;
                    }
                }
            }

            if (method != null && (Modifier.isStatic(method.getModifiers()) || clazz.isAssignableFrom(config.getClass()))) {
                method.trySetAccessible();

                Parameter[] parameters = method.getParameters();

                Object firstArgument = createArgumentForWidgetFunction(config, field, parameters[0], supplierType, primitiveSupplierType, consumerType, primitiveConsumerType);
                Object secondArgument = createArgumentForWidgetFunction(config, field, parameters[1], supplierType, primitiveSupplierType, consumerType, primitiveConsumerType);

                try {
                    return (WidgetFunction) method.invoke(config, firstArgument, secondArgument);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            clazz = config.getClass().getEnclosingClass();
        }

        if (methodWithInvalidArguments != null) {
            String suppliers;
            String consumers;
            if (primitiveSupplierType != null) {
                suppliers = primitiveSupplierType.getTypeName() + "/" + supplierType.getTypeName();
            } else {
                suppliers = supplierType.toString();
            }
            if (primitiveConsumerType != null) {
                consumers = primitiveConsumerType.getTypeName() + "/" + consumerType.getTypeName();
            } else {
                consumers = consumerType.toString();
            }
            throw new RuntimeException("Error trying to check " + annotation + ": " + methodWithInvalidArguments.getName() + " must return WidgetFunction and take parameters " + suppliers + " and " + consumers);
        }

        throw new RuntimeException("Can't find function for " + annotation);
    }

    private static Object createArgumentForWidgetFunction(Object config, Field field, Parameter parameter, Type supplier, @Nullable Type primitiveSupplier, Type consumer, @Nullable Type primitiveConsumer) {
        var parameterized = parameter.getParameterizedType();
        if (parameterized.equals(supplier)) {
            return (Supplier<Object>) () -> {
                try {
                    return field.get(config);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } else if (parameterized.equals(consumer)) {
            return (Consumer<Object>) value -> {
                try {
                    field.set(config, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            };
        } else if (parameterized.equals(primitiveSupplier)) {
            if (primitiveSupplier == IntSupplier.class) {
                return (IntSupplier) () -> {
                    try {
                        return field.getInt(config);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                };
            } else if (primitiveSupplier == LongSupplier.class) {
                return (LongSupplier) () -> {
                    try {
                        return field.getLong(config);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                };
            } else if (primitiveSupplier == BooleanSupplier.class) {
                return (BooleanSupplier) () -> {
                    try {
                        return field.getBoolean(config);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                };
            } else if (primitiveSupplier == DoubleSupplier.class) {
                return (DoubleSupplier) () -> {
                    try {
                        return field.getDouble(config);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                };
            } else {
                throw new RuntimeException("Unknown primitive supplier type: " + primitiveSupplier);
            }
        } else if (parameterized.equals(primitiveConsumer)) {
            if (primitiveConsumer == IntConsumer.class) {
                return (IntConsumer) value -> {
                    try {
                        field.setInt(config, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                };
            } else if (primitiveConsumer == LongConsumer.class) {
                return (LongConsumer) value -> {
                    try {
                        field.setLong(config, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                };
            } else if (primitiveConsumer == DoubleConsumer.class) {
                return (DoubleConsumer) value -> {
                    try {
                        field.setDouble(config, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                };
            } else {
                throw new RuntimeException("Unknown primitive consumer type: " + primitiveConsumer);
            }
        } else {
            throw new RuntimeException("Unknown supplier/consumer type: " + parameterized);
        }
    }

    private static boolean isMethodValidForWidgetFunction(Method method, Type supplier, @Nullable Type primitiveSupplier, Type consumer, @Nullable Type primitiveConsumer) {
        if (method.getReturnType() != WidgetFunction.class) {
            return false;
        }
        if (method.getParameterCount() != 2) {
            return false;
        }
        Parameter[] parameters = method.getParameters();
        if (parameters.length != 2) {
            return false;
        }
        if (checkWidgetFunctionParameterType(parameters[0], supplier, primitiveSupplier)) {
            return checkWidgetFunctionParameterType(parameters[1], consumer, primitiveConsumer);
        } else if (checkWidgetFunctionParameterType(parameters[0], consumer, primitiveConsumer)) {
            return checkWidgetFunctionParameterType(parameters[1], supplier, primitiveSupplier);
        } else {
            return false;
        }
    }

    private static boolean checkWidgetFunctionParameterType(Parameter parameter, Type main, @Nullable Type primitive) {
        var parameterized = parameter.getParameterizedType();
        return parameterized.equals(main) || parameterized.equals(primitive);
    }

    private BooleanSupplier getOrCreateCondition(Object config, Annotation annotation, String functionName) {
        Method methodWithInvalidArguments = null;

        Class<?> clazz = config.getClass();
        while (clazz != null) {
            CachedConditionKey nonStaticKey = null;
            if (clazz.isAssignableFrom(config.getClass())) {
                nonStaticKey = new CachedConditionKey(clazz, config, functionName);

                BooleanSupplier cached = this.conditions.get(nonStaticKey);
                if (cached != null) {
                    return cached;
                }
            }

            CachedConditionKey staticKey = new CachedConditionKey(clazz, null, functionName);
            BooleanSupplier cachedStatic = this.conditions.get(staticKey);
            if (cachedStatic != null) {
                return cachedStatic;
            }

            Method method = null;

            for (Method declaredMethod : clazz.getDeclaredMethods()) {
                if (declaredMethod.getName().equals(functionName)) {
                    if (isMethodValidForCondition(declaredMethod)) {
                        method = declaredMethod;
                        break;
                    } else if (methodWithInvalidArguments == null) {
                        methodWithInvalidArguments = declaredMethod;
                    }
                }
            }

            if (method != null) {
                method.trySetAccessible();

                Method methodF = method;
                if (Modifier.isStatic(methodF.getModifiers())) {
                    BooleanSupplier supplier = () -> {
                        try {
                            return (Boolean) methodF.invoke(null);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    };

                    this.conditions.put(staticKey, supplier);
                    return supplier;
                } else if (nonStaticKey != null) {
                    BooleanSupplier supplier = () -> {
                        try {
                            return (Boolean) methodF.invoke(config);
                        } catch (InvocationTargetException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    };

                    this.conditions.put(nonStaticKey, supplier);
                    return supplier;
                }
            }

            clazz = clazz.getEnclosingClass();
        }

        if (methodWithInvalidArguments != null) {
            throw new RuntimeException("Error trying to check " + annotation + ": " + methodWithInvalidArguments + " must return boolean and take zero arguments");
        }

        throw new RuntimeException("Can't find function for " + annotation);
    }

    private static boolean isMethodValidForCondition(Method method) {
        if (method.getReturnType() != boolean.class && method.getReturnType() != Boolean.class) {
            return false;
        }
        if (method.getParameterCount() > 0) {
            return false;
        }
        return true;
    }

    private LatticeElement createLatticeElement(Object config, Field field, Annotation widgetAnnotation, Component titleComponent, Component descriptionComponent) throws IllegalAccessException {
        Class<?> fieldType = field.getType();

        String formatting;
        boolean translateFormatting;

        LatticeFormatValues latticeFormatValues = field.getDeclaredAnnotation(LatticeFormatValues.class);
        if (latticeFormatValues != null) {
            formatting = latticeFormatValues.formattingString();
            translateFormatting = latticeFormatValues.translate();
        } else {
            formatting = null;
            translateFormatting = false;
        }

        if (widgetAnnotation instanceof LatticeWidgetCustom custom) {
            WidgetFunction widgetFunction = createWidgetFunction(config, custom, custom.function(), field);
            return new LatticeElement(widgetFunction, titleComponent, descriptionComponent);
        } else if (widgetAnnotation instanceof LatticeWidgetButton) {
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

                        EditableSlider<?> editableSlider = new EditableSlider<>(0, 0, width, 20, title, font, allowAlternativeInput, initialValue) {
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
                        editableSlider.setFormattingString(formatting, translateFormatting);
                        return editableSlider;
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

                        EditableSlider<?> editableSlider = new EditableSlider<>(0, 0, width, 20, title, font, allowAlternativeInput, initialValue) {
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
                        editableSlider.setFormattingString(formatting, translateFormatting);
                        return editableSlider;
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
                        DiscreteSlider<?> discreteSlider = new DiscreteSlider<>(0, 0, width, 20, title, initialValue, values) {
                            @Override
                            public void setValue(Object value) {
                                try {
                                    field.set(config, value);
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        };
                        discreteSlider.setFormattingString(formatting);
                        return discreteSlider;
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

                KeyMappingListSupplier keyMappingListSupplier = this.keyMappingListSupplier;
                keyMappingListSupplier.ensureRegistered(keyMapping);

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

                            for (KeyMapping other : keyMappingListSupplier.get()) {
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
                var element = new LatticeElement(WidgetFunction.editBox(() -> {
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
                }, maxLength), titleComponent, descriptionComponent);
                element.showTitleSeparately(true);
                return element;
            } else {
                throw new RuntimeException(fieldType + " isn't compatible with @LatticeWidgetTextField");
            }
        } else if (widgetAnnotation instanceof LatticeWidgetTextArea latticeWidgetTextArea) {
            int height = latticeWidgetTextArea.height();
            int characterLimit = latticeWidgetTextArea.characterLimit();
            if (fieldType == String.class) {
                var element = new LatticeElement(WidgetFunction.multilineEditBox(() -> {
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
                }, height, characterLimit), titleComponent, descriptionComponent);
                element.showTitleSeparately(true);
                return element;
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
