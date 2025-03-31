package fr.aeldit.ctms;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class VersionUtils
{
    public static @NotNull Identifier getIdentifier(String path)
    {
        //? if <1.21-1.21.1 {
        /*return new Identifier(path);
         *///?} else {
        return Identifier.of(path);
        //?}
    }

    public static @NotNull Identifier getIdentifier(String namespace, String path)
    {
        //? if <1.21-1.21.1 {
        /*return new Identifier(namespace, path);
         *///?} else {
        return Identifier.of(namespace, path);
        //?}
    }
}
