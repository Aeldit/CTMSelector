package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CTMPacks
{
    public final ArrayList<CTMPack> availableCTMPacks = new ArrayList<>();

    public static @NotNull ArrayList<String> getEnabledPacks()
    {
        return new ArrayList<>(MinecraftClient.getInstance().getResourcePackManager()
                                              //? if <1.20.6 {
                                              /*.getEnabledNames()
                                               *///?} else {
                                              .getEnabledIds()
                               //?}
        );
    }

    public static boolean isPackEnabled(String packName)
    {
        return getEnabledPacks().contains("file/%s".formatted(packName));
    }

    public void add(@NotNull CTMPack ctmPack)
    {
        if (!availableCTMPacks.contains(ctmPack))
        {
            availableCTMPacks.add(ctmPack);
        }
    }
}
