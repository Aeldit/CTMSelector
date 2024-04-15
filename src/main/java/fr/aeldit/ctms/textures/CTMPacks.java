package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.gui.entryTypes.CTMPack;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class CTMPacks
{
    private final ArrayList<CTMPack> availableCTMPacks = new ArrayList<>();

    @Contract(" -> new")
    public static @NotNull ArrayList<String> getEnabledPacks()
    {
        return new ArrayList<>(MinecraftClient.getInstance().getResourcePackManager().getEnabledNames());
    }

    public static boolean isPackEnabled(String packName)
    {
        return getEnabledPacks().contains("file/" + packName);
    }

    public void add(@NotNull CTMPack ctmPack)
    {
        if (!availableCTMPacks.contains(ctmPack))
        {
            availableCTMPacks.add(ctmPack);
        }
    }

    public ArrayList<CTMPack> getAvailableCTMPacks()
    {
        return availableCTMPacks;
    }
}
