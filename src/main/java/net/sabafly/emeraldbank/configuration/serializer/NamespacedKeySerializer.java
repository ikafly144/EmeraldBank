package net.sabafly.emeraldbank.configuration.serializer;

import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class NamespacedKeySerializer implements TypeSerializer<NamespacedKey> {
    @Override
    public NamespacedKey deserialize(@NotNull Type type, @NotNull ConfigurationNode node) throws SerializationException {
        if (node.isNull()) {
            return null;
        }
        return NamespacedKey.fromString(node.getString(""));
    }

    @Override
    public void serialize(@NotNull Type type, @Nullable NamespacedKey obj, @NotNull ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.set(null);
            return;
        }
        node.set(obj.asMinimalString());
    }
}
