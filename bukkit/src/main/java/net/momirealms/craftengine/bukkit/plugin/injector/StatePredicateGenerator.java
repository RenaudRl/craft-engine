package net.momirealms.craftengine.bukkit.plugin.injector;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.proxy.minecraft.core.BlockPosProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockBehaviourProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockStateProxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.method.matcher.MethodMatcher;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public final class StatePredicateGenerator {
    public static final Method method$StatePredicate$test = requireNonNull(
            SparrowClass.of(BlockBehaviourProxy.StatePredicateProxy.CLASS)
                    .getDeclaredMethod(MethodMatcher.takeArguments(BlockStateProxy.CLASS, BlockGetterProxy.CLASS, BlockPosProxy.CLASS)
                    .and(MethodMatcher.returnType(boolean.class)))
    );
    public static final Method method$StateArgumentPredicate$test = requireNonNull(
            SparrowClass.of(BlockBehaviourProxy.StateArgumentPredicateProxy.CLASS)
                    .getDeclaredMethod(MethodMatcher.takeArguments(BlockStateProxy.CLASS, BlockGetterProxy.CLASS, BlockPosProxy.CLASS, Object.class)
                    .and(MethodMatcher.returnType(boolean.class)))
    );
    private static Object alwaysTrue;
    private static Object alwaysFalse;
    private static Object alwaysTrueWithArgument;
    private static Object alwaysFalseWithArgument;

    private StatePredicateGenerator() {}

    public static void init() {
        ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V21);
        String packageWithName = StatePredicateGenerator.class.getName();
        String packageName = packageWithName.substring(0, packageWithName.lastIndexOf('.'));
        alwaysTrue = generate(byteBuddy, packageName + ".CraftEngineAlwaysTrueStatePredicate", true);
        alwaysFalse = generate(byteBuddy, packageName + ".CraftEngineAlwaysFalseStatePredicate", false);
        alwaysTrueWithArgument = generate(byteBuddy, packageName + ".CraftEngineAlwaysTrueStateArgumentPredicate", BlockBehaviourProxy.StateArgumentPredicateProxy.CLASS, method$StateArgumentPredicate$test, true);
        alwaysFalseWithArgument = generate(byteBuddy, packageName + ".CraftEngineAlwaysFalseStateArgumentPredicate", BlockBehaviourProxy.StateArgumentPredicateProxy.CLASS, method$StateArgumentPredicate$test, false);
    }

    private static Object generate(ByteBuddy byteBuddy, String generatedClassName, boolean trueOrFalse) {
        return generate(byteBuddy, generatedClassName, BlockBehaviourProxy.StatePredicateProxy.CLASS, method$StatePredicate$test, trueOrFalse);
    }

    private static Object generate(ByteBuddy byteBuddy, String generatedClassName, Class<?> predicateInterface, Method testMethod, boolean trueOrFalse) {
        try {
            return byteBuddy
                    .subclass(Object.class)
                    .name(generatedClassName)
                    .implement(predicateInterface)
                    .method(ElementMatchers.is(testMethod))
                    .intercept(FixedValue.value(trueOrFalse))
                    .make()
                    .load(StatePredicateGenerator.class.getClassLoader())
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to generate " + generatedClassName, e);
        }
    }

    public static Object alwaysTrue() {
        return alwaysTrue;
    }

    public static Object alwaysFalse() {
        return alwaysFalse;
    }

    /** The {@code StateArgumentPredicate} flavour, for fields that take an extra argument (26.3 {@code isViewBlocking}). */
    public static Object alwaysTrueWithArgument() {
        return alwaysTrueWithArgument;
    }

    public static Object alwaysFalseWithArgument() {
        return alwaysFalseWithArgument;
    }
}
