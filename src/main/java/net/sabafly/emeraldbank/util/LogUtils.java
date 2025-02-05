package net.sabafly.emeraldbank.util;

import net.sabafly.emeraldbank.EmeraldBank;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {

    private LogUtils() {
    }

    public static @NotNull Logger getLogger() {
        try {
            return EmeraldBank.getInstance().getSLF4JLogger();
        } catch (Exception e) {
            return LoggerFactory.getLogger("EmeraldBankBootstrap");
        }
    }

}
