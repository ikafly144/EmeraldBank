package net.sabafly.emeraldbank.configuration.type;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.serialize.ScalarSerializer;

import java.lang.reflect.AnnotatedType;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public interface IntOr {

    Logger LOGGER = LogUtils.getClassLogger();

    default int or(final int fallback) {
        return this.value().orElse(fallback);
    }

    OptionalInt value();

    default boolean isDefined() {
        return this.value().isPresent();
    }

    default int intValue() {
        return this.value().orElseThrow();
    }

    record Default(OptionalInt value) implements IntOr {
        private static final String DEFAULT_VALUE = "default";
        public static final Default USE_DEFAULT = new Default(OptionalInt.empty());
        public static final ScalarSerializer<Default> SERIALIZER = new Serializer<>(Default.class, Default::new, DEFAULT_VALUE, USE_DEFAULT);
    }

    record Disabled(OptionalInt value) implements IntOr {
        private static final String DISABLED_VALUE = "disabled";
        public static final Disabled DISABLED = new Disabled(OptionalInt.empty());
        public static final ScalarSerializer<Disabled> SERIALIZER = new Serializer<>(Disabled.class, Disabled::new, DISABLED_VALUE, DISABLED);

        public boolean test(IntPredicate predicate) {
            return this.value.isPresent() && predicate.test(this.value.getAsInt());
        }

        public boolean enabled() {
            return this.value.isPresent();
        }
    }

    final class Serializer<T extends IntOr> extends OptionalNumSerializer<T, OptionalInt> {

        private Serializer(Class<T> classOfT, Function<OptionalInt, T> factory, String emptySerializedValue, T emptyValue) {
            super(classOfT, emptySerializedValue, emptyValue, OptionalInt::empty, OptionalInt::isEmpty, factory, int.class);
        }

        @Override
        protected OptionalInt full(final String value) {
            return OptionalInt.of(Integer.parseInt(value));
        }

        @Override
        protected OptionalInt full(final Number num) {
            if (num.intValue() != num.doubleValue() || num.intValue() != num.longValue()) {
                LOGGER.error("{} cannot be converted to an integer without losing information", num);
            }
            return OptionalInt.of(num.intValue());
        }

        @Override
        protected boolean belowZero(final OptionalInt value) {
            Preconditions.checkArgument(value.isPresent());
            return value.getAsInt() < 0;
        }

        @Override
        protected @NotNull Object serialize(@NotNull AnnotatedType type, T item, @NotNull Predicate<Class<?>> typeSupported) {
            return serialize(item, typeSupported);
        }

        @Override
        protected @NotNull Object serialize(final T item, final @NotNull Predicate<Class<?>> typeSupported) {
            final OptionalInt value = item.value();
            if (value.isPresent()) {
                return value.getAsInt();
            } else {
                return this.emptySerializedValue;
            }
        }
    }
}