package dev.tildejustin.costar.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(World.class)
public abstract class WorldMixin {
    @Redirect(method = "method_8429", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getSimpleName()Ljava/lang/String;", remap = false))
    private String remapBlockEntityClassName(Class<BlockEntity> instance) {
        return FabricLoader.getInstance().getMappingResolver().unmapClassName("official", instance.getCanonicalName());
    }
}
