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
        String className = mappingResolver.unmapClassName("official", instance.getName());
        return className.substring(className.lastIndexOf(".") + 1);
    }
}
