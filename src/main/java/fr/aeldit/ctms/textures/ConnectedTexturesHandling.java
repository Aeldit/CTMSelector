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
 * in the repo of this mod (https://github.com/Aeldit/Cyan)
 */

package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.config.CTMSOptionsStorage;
import fr.aeldit.ctms.util.CTMResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static fr.aeldit.ctms.util.Utils.CTMS_OPTIONS_STORAGE;

public class ConnectedTexturesHandling
{
    private final Map<String, Boolean> tmpOptionsMap = new HashMap<>();
    private static final ArrayList<CTMResourcePack> ctmResourcePacks = new ArrayList<>();
    private ZipFile currentZipFile;
    private final Map<String, String> zipFilesContent = new HashMap<>();

    public void init()
    {
        Path path = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

        for (File resourcePackDirOrZip : path.toFile().listFiles())
        {
            // Directory
            if (resourcePackDirOrZip.isDirectory() && resourcePackDirOrZip.getName().startsWith("CTM"))
            {
                //boolean ctmFound = false;
                Path ctmPath = Path.of(resourcePackDirOrZip + "/assets/minecraft/optifine/ctm/connect");

                if (Files.exists(ctmPath))
                {
                    for (File categoryOrBlockDir : ctmPath.toFile().listFiles())
                    {
                        if (categoryOrBlockDir.isDirectory())
                        {
                            ArrayList<CTMSOptionsStorage.BooleanOption> tmpCtmBlocksList = new ArrayList<>();

                            if (categoryOrBlockDir.getName().startsWith("c_"))
                            {
                                for (File textureDir : categoryOrBlockDir.listFiles())
                                {
                                    if (textureDir.isDirectory())
                                    {
                                        tmpCtmBlocksList.addAll(getBlocksInDir(resourcePackDirOrZip, textureDir));
                                    }
                                }
                            }
                            else
                            {
                                tmpCtmBlocksList.addAll(getBlocksInDir(resourcePackDirOrZip, categoryOrBlockDir));
                            }

                            if (!tmpCtmBlocksList.isEmpty())
                            {
                                CTMS_OPTIONS_STORAGE.initPackOptions(resourcePackDirOrZip.getName(), tmpCtmBlocksList, tmpOptionsMap, ctmResourcePacks);
                                //ctmFound = true;
                            }
                        }
                    }
                }

                /*if (ctmFound)
                {
                    ctmResourcePacks.add(new CTMResourcePack(resourcePackDirOrZip.getName(), resourcePackDirOrZip.toPath()));
                }*/
            }

            // Zip File
            else
            {
                Optional<String> fileExtension = getFileExtension(resourcePackDirOrZip.getName());

                if (fileExtension.isPresent() && fileExtension.get().equals("zip"))
                {
                    try (ZipFile zipFile = new ZipFile(resourcePackDirOrZip))
                    {
                        currentZipFile = zipFile;

                        if (pathInZipExists(Path.of("assets/minecraft/optifine/ctm/connect"))) // Good
                        {
                            Enumeration<? extends ZipEntry> entries = zipFile.entries();
                            ArrayList<String> entriesList = new ArrayList<>();
                            ArrayList<CTMSOptionsStorage.BooleanOption> tmpCtmBlocksList = new ArrayList<>();
                            String currentDir = "";
                            ArrayList<CTMSOptionsStorage.BooleanOption> allBlocksInDir = new ArrayList<>();

                            while (entries.hasMoreElements())
                            {
                                ZipEntry entry = entries.nextElement();

                                if (entry.isDirectory())
                                {
                                    tmpCtmBlocksList.addAll(allBlocksInDir);
                                    System.out.println(tmpCtmBlocksList);
                                    allBlocksInDir.clear();
                                    currentDir = entry.getName();
                                }
                                else
                                {
                                    Optional<String> zipedFileExtension = getFileExtension(entry.getName());

                                    if (zipedFileExtension.isPresent())
                                    {
                                        System.out.println(zipedFileExtension.get());
                                        if (zipedFileExtension.get().equals("properties"))
                                        {
                                            String optionName = entry.getName()
                                                    .split("\\\\")[entry.getName().split("\\\\").length - 1]
                                                    .replace(".txt", "");
                                            allBlocksInDir.add(CTMS_OPTIONS_STORAGE.new BooleanOption(
                                                    resourcePackDirOrZip.getName(),
                                                    optionName,
                                                    true,
                                                    Path.of(currentDir)
                                            ));
                                            tmpOptionsMap.put(optionName, false);
                                        }
                                    }
                                    entriesList.add(entry.getName());
                                }
                            }

                            if (!tmpCtmBlocksList.isEmpty())
                            {
                                CTMS_OPTIONS_STORAGE.initPackOptions(resourcePackDirOrZip.getName(), tmpCtmBlocksList, tmpOptionsMap, ctmResourcePacks);
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
    }

    private void writeFilesToZip(@NotNull File resourcePackDirOrZip)
    {
        Optional<String> fileExtension = getFileExtension(resourcePackDirOrZip.getName());

        if (fileExtension.isPresent())
        {
            if (fileExtension.get().equals("txt"))
            {
                try (var zos = new ZipOutputStream(new BufferedOutputStream(
                        Files.newOutputStream(Path.of(resourcePackDirOrZip.toPath().toString().replace(".txt", ".properties")))))
                )
                {
                    for (var entry : zipFilesContent.entrySet())
                    {
                        zos.putNextEntry(new ZipEntry(entry.getKey()));
                        zos.write(entry.getValue().getBytes());
                        zos.closeEntry();
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            else if (fileExtension.get().equals("properties"))
            {
                try (var zos = new ZipOutputStream(new BufferedOutputStream(
                        Files.newOutputStream(Path.of(resourcePackDirOrZip.toPath().toString().replace(".properties", ".txt")))))
                )
                {
                    for (var entry : zipFilesContent.entrySet())
                    {
                        zos.putNextEntry(new ZipEntry(entry.getKey()));
                        zos.write(entry.getValue().getBytes());
                        zos.closeEntry();
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private Optional<String> getFileExtension(String fileName)
    {
        return Optional.of(fileName).filter(f -> f.contains(".")).map(f -> f.substring(fileName.lastIndexOf(".") + 1));
    }

    private boolean pathInZipExists(Path path)
    {
        try
        {
            Enumeration<? extends ZipEntry> entries = currentZipFile.entries();

            while (entries.hasMoreElements())
            {
                if (entries.nextElement().isDirectory())
                {
                    if (Path.of(entries.nextElement().getName()).equals(path))
                    {
                        return true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return false;
    }

    private @NotNull ArrayList<CTMSOptionsStorage.BooleanOption> getBlocksInDir(File packDir, @NotNull File dir)
    {
        ArrayList<CTMSOptionsStorage.BooleanOption> allBlocksInDir = new ArrayList<>();

        for (File currentDir : dir.listFiles())
        {
            if (currentDir.getName().endsWith(".properties"))
            {
                String optionName = currentDir.getName()
                        .split("\\\\")[currentDir.getName().split("\\\\").length - 1]
                        .replace(".properties", "");
                allBlocksInDir.add(CTMS_OPTIONS_STORAGE.new BooleanOption(
                        packDir.getName(),
                        optionName,
                        true,
                        dir.toPath()
                ));
                tmpOptionsMap.put(optionName, true);
            }
            else if (currentDir.getName().endsWith(".txt"))
            {
                String optionName = currentDir.getName()
                        .split("\\\\")[currentDir.getName().split("\\\\").length - 1]
                        .replace(".txt", "");
                allBlocksInDir.add(CTMS_OPTIONS_STORAGE.new BooleanOption(
                        packDir.getName(),
                        optionName,
                        true,
                        dir.toPath()
                ));
                tmpOptionsMap.put(optionName, false);
            }
        }
        return allBlocksInDir;
    }

    private void toggleBlockConnection(String packName, @NotNull File propertiesOrTxtFile)
    {
        if (CTMS_OPTIONS_STORAGE.getBooleanOption(packName, propertiesOrTxtFile.getName()
                .replace(".properties", "")
                .replace(".txt", ""))
        )
        {
            if (propertiesOrTxtFile.getName().endsWith(".txt"))
            {
                propertiesOrTxtFile.renameTo(new File(
                        propertiesOrTxtFile.getPath().replace(propertiesOrTxtFile.getName(), "")
                                + propertiesOrTxtFile.getName().replace(".txt", ".properties"))
                );
            }
        }
        else
        {
            if (propertiesOrTxtFile.getName().endsWith(".properties"))
            {
                propertiesOrTxtFile.renameTo(new File(
                        propertiesOrTxtFile.getPath().replace(propertiesOrTxtFile.getName(), "")
                                + propertiesOrTxtFile.getName().replace(".properties", ".txt"))
                );
            }
        }
    }

    public void updateUsedTextures(String packName)
    {
        Path ctmPath = FabricLoader.getInstance().getGameDir().resolve("resourcepacks/" + packName + "/assets/minecraft/optifine/ctm/connect");

        if (Files.exists(ctmPath))
        {
            for (File categoryOrBlockDir : ctmPath.toFile().listFiles())
            {
                if (categoryOrBlockDir.isDirectory())
                {
                    if (categoryOrBlockDir.getName().startsWith("c_"))
                    {
                        for (File textureDir : categoryOrBlockDir.listFiles())
                        {
                            if (textureDir.isDirectory())
                            {
                                for (File currentDir : textureDir.listFiles())
                                {
                                    if (currentDir.isFile() && (currentDir.getName().endsWith(".properties") || currentDir.getName().endsWith(".txt")))
                                    {
                                        toggleBlockConnection(packName, currentDir);
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        for (File currentDir : categoryOrBlockDir.listFiles())
                        {
                            if (currentDir.isFile() && (currentDir.getName().endsWith(".properties") || currentDir.getName().endsWith(".txt")))
                            {
                                toggleBlockConnection(packName, currentDir);
                            }
                        }
                    }
                }
            }
        }
        MinecraftClient.getInstance().reloadResources();
    }

    public static ArrayList<CTMResourcePack> getCtmResourcePacks()
    {
        return ctmResourcePacks;
    }
}
