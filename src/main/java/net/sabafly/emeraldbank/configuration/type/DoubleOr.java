package net.sabafly.emeraldbank.configuration.type;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.ScalarSerializer;

import java.util.OptionalDouble;
import java.util.function.DoublePredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public interface DoubleOr {

    Logger LOGGER = LogUtils.getClassLogger();

    default double or(final double fallback) {
        return this.value().orElse(fallback);
    }

    OptionalDouble value();

    default boolean isDefined() {
        return this.value().isPresent();
    }

    default double doubleValue() {
        return this.value().orElseThrow();
    }

    record Default(OptionalDouble value) implements DoubleOr {
        private static final String DEFAULT_VALUE = "default";
        public static final Default USE_DEFAULT = new Default(OptionalDouble.empty());
        public static final ScalarSerializer<Default> SERIALIZER = new Serializer<>(Default.class, Default::new, DEFAULT_VALUE, USE_DEFAULT);
    }

    record Disabled(OptionalDouble value) implements DoubleOr {
        private static final String DISABLED_VALUE = "disabled";
        public static final Disabled DISABLED = new Disabled(OptionalDouble.empty());
        public static final ScalarSerializer<Disabled> SERIALIZER = new Serializer<>(Disabled.class, Disabled::new, DISABLED_VALUE, DISABLED);

        public boolean test(DoublePredicate predicate) {
            return this.value.isPresent() && predicate.test(this.value.getAsDouble());
        }

        public boolean enabled() {
            return this.value.isPresent();
        }
    }

    final class Serializer<T extends DoubleOr> extends OptionalNumSerializer<T, OptionalDouble> {

        private Serializer(Class<T> classOfT, Function<OptionalDouble, T> factory, String emptySerializedValue, T emptyValue) {
            super(classOfT, emptySerializedValue, emptyValue, OptionalDouble::empty, OptionalDouble::isEmpty, factory, double.class);
        }

        @Override
        protected OptionalDouble full(final String value) {
            return OptionalDouble.of(Double.parseDouble(value));
        }

        @Override
        protected OptionalDouble full(final Number num) {
            return OptionalDouble.of(num.doubleValue());
        }

        @Override
        protected boolean belowZero(final OptionalDouble value) {
            Preconditions.checkArgument(value.isPresent());
            return value.getAsDouble() < 0;
        }

//        @Override
//        protected @NotNull Object serialize(@NotNull AnnotatedType type, T item, @NotNull Predicate<Class<?>> typeSupported) {
//            return serialize(item, typeSupported);
//        }

        @Override
        protected @NotNull Object serialize(final T item, final @NotNull Predicate<Class<?>> typeSupported) {
            final OptionalDouble value = item.value();
            if (value.isPresent()) {
                return value.getAsDouble();
            } else {
                return this.emptySerializedValue;
            }
        }
    }
}