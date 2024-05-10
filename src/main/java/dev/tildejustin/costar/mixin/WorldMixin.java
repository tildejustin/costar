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
        return getRemappedSimpleName(instance);
    }

    // the following is edited from https://github.com/openjdk/jdk8/blob/6a383433a9f4661a96a90b2a4c7b5b9a85720031/jdk/src/share/classes/java/lang/Class.java#L1292-L1323

    /**
     * Returns the simple name of the underlying class as given in the
     * source code. Returns an empty string if the underlying class is
     * anonymous.
     *
     * <p>The simple name of an array is the simple name of the
     * component type with "[]" appended.  In particular the simple
     * name of an array whose component type is anonymous is "[]".
     *
     * @return the simple name of the underlying class
     * @since 1.5
     */
    @Unique
    private String getRemappedSimpleName(Class<?> instance) {
        if (instance.isArray())
            return getRemappedSimpleName(instance.getComponentType())+"[]";

        String simpleName = getRemappedSimpleBinaryName(instance);
        if (simpleName == null) { // top level class
            simpleName = mappingResolver.unmapClassName("official", instance.getName());
            return simpleName.substring(simpleName.lastIndexOf(".")+1); // strip the package name
        }
        // According to JLS3 "Binary Compatibility" (13.1) the binary
        // name of non-package classes (not top level) is the binary
        // name of the immediately enclosing class followed by a '$' followed by:
        // (for nested and inner classes): the simple name.
        // (for local classes): 1 or more digits followed by the simple name.
        // (for anonymous classes): 1 or more digits.

        // Since getSimpleBinaryName() will strip the binary name of
        // the immediatly enclosing class, we are now looking at a
        // string that matches the regular expression "\$[0-9]*"
        // followed by a simple name (considering the simple of an
        // anonymous class to be the empty string).

        // Remove leading "\$[0-9]*" from the name
        int length = simpleName.length();
        if (length < 1 || simpleName.charAt(0) != '$')
            throw new InternalError("Malformed class name");
        int index = 1;
        while (index < length && isAsciiDigit(simpleName.charAt(index)))
            index++;
        // Eventually, this is the empty string iff this is an anonymous class
        return simpleName.substring(index);
    }

    /**
     * Returns the "simple binary name" of the underlying class, i.e.,
     * the binary name without the leading enclosing class name.
     * Returns {@code null} if the underlying class is a top level
     * class.
     */
    @Unique
    private String getRemappedSimpleBinaryName(Class<?> instance) {
        Class<?> enclosingClass = instance.getEnclosingClass();
        if (enclosingClass == null) // top level class
            return null;
        // Otherwise, strip the enclosing class' name
        try {
            // return getName().substring(enclosingClass.getName().length());
            return mappingResolver.unmapClassName("official", instance.getName()).substring(mappingResolver.unmapClassName("official", enclosingClass.getName()).length());
        } catch (IndexOutOfBoundsException ex) {
            throw new InternalError("Malformed class name", ex);
        }
    }

    /**
     * Character.isDigit answers {@code true} to some non-ascii
     * digits.  This one does not.
     */
    @Unique
    private static boolean isAsciiDigit(char c) {
        return '0' <= c && c <= '9';
    }
}
