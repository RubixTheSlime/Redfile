package org.rubix.redfile;

import net.fabricmc.api.ModInitializer;
import org.rubix.redfile.profiler.Sampler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;


public class Redfile implements ModInitializer {
    public static final String MOD_ID = "redfile";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final MethodHandle fHandle;
    private static final MethodHandle gHandle;

    static {
        try {
            fHandle = MethodHandles.lookup().unreflect(Redfile.class.getDeclaredMethod("f"));
            gHandle = MethodHandles.lookup().unreflect(Redfile.class.getDeclaredMethod("g", Object.class));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static void f() {}

    private static void g(Object ignored) {}

    public static void noOp() {
        try {
            fHandle.invokeExact();
        } catch (Throwable ignored) {}
    }

    public static void noOpWith(Object x) {
        try {
            gHandle.invokeExact( x);
        } catch (Throwable ignored) {}
    }



    @Override
    public void onInitialize() {
        LOGGER.info("Trans Rights");

//        ModBlock.initialize();
//        ModBlockEntityTypes.initialize();
        ModEntity.init();
        ModCommand.init();

    }
}
