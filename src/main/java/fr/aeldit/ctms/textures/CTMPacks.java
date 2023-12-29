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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    private static final List<CTMPack> AVAILABLE_CTM_PACKS = new ArrayList<>();
    // Contains key:value pairs like so : <packName:ID>
    private static final Map<String, Integer> packsIDs = new HashMap<>();
    private int packsNumber = 0;

    @Contract(" -> new")
    public static @NotNull ArrayList<String> getEnabledPacks()
    {
        return new ArrayList<>(MinecraftClient.getInstance().getResourcePackManager().getEnabledNames());
    }

    /**
     * Adds the pack and its ID (determined by the number of packs that have already been added) to the packIds map
     * + adds the same pack to AVAILABLE_CTM_PACKS (its Identifier is defined here)
     *
     * @param packName The name of the pack
     */
    public void add(String packName)
    {
        if (!packsIDs.containsKey(packName))
        {
            packsIDs.put(packName, packsNumber);
            AVAILABLE_CTM_PACKS.add(new CTMPack(
                    packName, new Identifier("ctms", "%s.png".formatted(packsNumber++))
            ));
        }
    }

    public static Map<String, Integer> getPacksIDs()
    {
        return packsIDs;
    }

    public static List<CTMPack> getAvailableCtmPacks()
    {
        return AVAILABLE_CTM_PACKS;
    }

    public void createIconsPack() // TODO -> Get pack.png correctly on reload
    {
        if (!getAvailableCtmPacks().isEmpty())
        {
            Path packsPath = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
            Path packPath = Path.of(packsPath + "/__CTMS_Icons__");
            Path mcmetaPath = Path.of(packPath + "/pack.mcmeta");
            Path iconsPackPath = Path.of(packPath + "/assets/ctms");

            if (!Files.exists(packPath))
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

            // Adds the icons (pack.png) of each CTM pack into a special resource pack that is used for the main screen of the mod
            try
            {
                for (CTMPack ctmPack : getAvailableCtmPacks())
                {
                    // If the file already exists, we don't add it
                    if (!Files.exists(Path.of(iconsPackPath + "/%d.png".formatted(getPacksIDs().get(ctmPack.getNameAsString())))))
                    {
                        if (ctmPack.getNameAsString().endsWith(".zip"))
                        {
                            try (ZipFile zipFile = new ZipFile(packsPath + "/" + ctmPack.getNameAsString()))
                            {
                                for (FileHeader fileHeader : zipFile.getFileHeaders())
                                {
                                    if (fileHeader.toString().equals("pack.png"))
                                    {
                                        // Extracts the file 'pack.png' from the zip file to the icons pack
                                        try (ZipFile zipFile1 = new ZipFile(packsPath + "/" + ctmPack.getNameAsString()))
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
                                                    Path.of(iconsPackPath + "/%d.png".formatted(getPacksIDs().get(ctmPack.getNameAsString())))
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
                            if (!Files.exists(Path.of(iconsPackPath + "/%s.png".formatted(ctmPack.getNameAsString()))))
                            {
                                Files.copy(Path.of(packsPath + "/" + ctmPack.getNameAsString().replace(" (folder)", "") + "/pack.png"),
                                        Path.of(iconsPackPath + "/%d.png".formatted(getPacksIDs().get(ctmPack.getNameAsString()))),
                                        REPLACE_EXISTING
                                );
                            }
                        }
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
