package dev.tildejustin.costar.mixin;

import net.fabricmc.loader.api.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(World.class)
public abstract class WorldMixin {
    @Unique
    private static final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();

    @Redirect(method = "tickEntities", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getSimpleName()Ljava/lang/String;", remap = false))
    private String remapBlockEntityClassName(Class<BlockEntity> instance) {
        // big thanks to https://github.com/openjdk/jdk8/blob/6a383433a9f4661a96a90b2a4c7b5b9a85720031/jdk/src/share/classes/java/lang/Class.java#L1292-L1323
        String className = instance.getName();
        String remappedClassName = mappingResolver.unmapClassName("official", className);
        Class<?> enclosingClass = instance.getEnclosingClass();
        // top level class case
        if (enclosingClass == null) {
            return remappedClassName.substring(remappedClassName.lastIndexOf(".") + 1);
        }
        String enclosingClassName = enclosingClass.getName();
        String remappedEnclosingClassName = mappingResolver.unmapClassName("official", enclosingClassName);
        String remappedSimpleBinaryName = remappedClassName.substring(remappedEnclosingClassName.length());
        int length = remappedSimpleBinaryName.length();
        int index = 1;
        while (index < length && isAsciiDigit(remappedSimpleBinaryName.charAt(index))) {
            ++index;
        }
        return remappedSimpleBinaryName.substring(index);
    }

    @Unique
    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }
}
