/*
 * Copyright (c) 2023  -  Made by Aeldit
 *
 *              GNU LESSER GENERAL PUBLIC LICENSE
 *                  Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed in the LICENSE.txt file
 * in the repo of this mod (https://github.com/Aeldit/CTMSelector)
 */

package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.gui.entryTypes.CTMPack;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class CTMPacks
{
    private final List<CTMPack> availableCTMPacks = new ArrayList<>();
    // Contains key:value pairs like so : <packName:ID>
    private final Map<String, Integer> packsIDs = new HashMap<>();
    private int packsNumber = 0;

    public static @NotNull ArrayList<String> getEnabledPacks()
    {
        return new ArrayList<>(MinecraftClient.getInstance().getResourcePackManager().getEnabledNames());
    }

    public void clearAvailableCTMPacks()
    {
        availableCTMPacks.clear();
    }

    /**
     * Adds the pack and its ID (determined by the number of packs that have already been added) to the packIds map
     * <p>
     * + adds the same pack to AVAILABLE_CTM_PACKS (its Identifier is defined here)
     * <p>
     * + finishes the initialization of the CTMPack (sets its iconId and its Identifier)
     */
    public void add(@NotNull CTMPack ctmPack)
    {
        if (!packsIDs.containsKey(ctmPack.getName()))
        {
            packsIDs.put(ctmPack.getName(), packsNumber);

            ctmPack.setIconId(packsNumber);
            ctmPack.setIdentifier(new Identifier("ctms", "%s.png".formatted(packsNumber)));

            availableCTMPacks.add(ctmPack);
            packsNumber++;
        }
    }

    public List<CTMPack> getAvailableCTMPacks()
    {
        return availableCTMPacks;
    }

    /**
     * Creates a custom resource pack that will contain the pack.png of each CTM resource pack
     */
    public void createIconsPack()
    {
        if (!getAvailableCTMPacks().isEmpty())
        {
            Path packsPath = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
            Path packPath = Path.of(packsPath + "/__CTMS_Icons__");
            Path mcmetaPath = Path.of(packPath + "/pack.mcmeta");
            Path iconsPackPath = Path.of(packPath + "/assets/ctms");

            // Creates the pack if it doesn't exist or of files directories are missing
            if (!Files.exists(iconsPackPath))
            {
                try
                {
                    Files.createDirectory(packPath);
                    Files.createDirectories(iconsPackPath);
                    Files.createFile(mcmetaPath);
                    Files.writeString(mcmetaPath, "{\"pack\": {\"pack_format\": 18, \"description\": \"CTMS packs icons\"}}");
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            // If the pack exists and there are already icons in it,
            // we remove them so we can load the new ones properly
            else if (Files.exists(iconsPackPath))
            {
                for (File file : new File(iconsPackPath.toString()).listFiles())
                {
                    file.delete();
                }
            }

            // Adds the icon (pack.png) of each CTM pack into a special resource pack that is used for the main screen of the mod
            try
            {
                for (CTMPack ctmPack : getAvailableCTMPacks())
                {
                    if (ctmPack.getName().endsWith(".zip"))
                    {
                        try (ZipFile zipFile = new ZipFile(packsPath + "/" + ctmPack.getName()))
                        {
                            for (FileHeader fileHeader : zipFile.getFileHeaders())
                            {
                                if (fileHeader.toString().equals("pack.png"))
                                {
                                    // Extracts the file 'pack.png' from the zip file to the icons pack
                                    try (ZipFile zipFile1 = new ZipFile(packsPath + "/" + ctmPack.getName()))
                                    {
                                        zipFile1.extractFile("pack.png",
                                                iconsPackPath.toString()
                                        );
                                    }
                                    catch (ZipException e)
                                    {
                                        throw new RuntimeException(e);
                                    }

                                    // Rename the file 'pack.png' (the one we just extracted) to the correct ID
                                    try
                                    {
                                        Files.move(Path.of(iconsPackPath + "/pack.png"),
                                                Path.of(iconsPackPath + "/%d.png".formatted(ctmPack.getIconId()))
                                        );
                                    }
                                    catch (IOException e)
                                    {
                                        throw new RuntimeException(e);
                                    }
                                    break;
                                }
                            }
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException(e);
                        }
                    }
                    else
                    {
                        Files.copy(Path.of(packsPath + "/" + ctmPack.getName() + "/pack.png"),
                                Path.of(iconsPackPath + "/%d.png".formatted(ctmPack.getIconId())),
                                REPLACE_EXISTING
                        );
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        MinecraftClient.getInstance().reloadResources();
    }
}
