package net.sabafly.emeraldbank.configuration.serializer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.serialize.ScalarSerializer;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class ComponentSerializer extends ScalarSerializer<Component> {

    public ComponentSerializer() {
        super(Component.class);
    }

    @Override
    public Component deserialize(@NotNull Type type, Object obj) {
        return MiniMessage.miniMessage().deserialize(obj.toString());
    }

    @Override
    protected @NotNull Object serialize(Component component, @NotNull Predicate<Class<?>> typeSupported) {
        return MiniMessage.miniMessage().serialize(component);
    }
}
