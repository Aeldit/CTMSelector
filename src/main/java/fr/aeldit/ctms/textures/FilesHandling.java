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

import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static fr.aeldit.ctms.textures.CTMBlocks.CTM_BLOCKS_MAP;
import static fr.aeldit.ctms.textures.CTMSelector.isFolderPackEligible;
import static fr.aeldit.ctms.textures.CTMSelector.isZipPackEligible;
import static fr.aeldit.ctms.util.Utils.CTM_SELECTOR_ARRAY_LIST;

public class FilesHandling
{
    private final Path resourcePacksDir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    private final String ctmPath = "assets/minecraft/optifine/ctm/connect/";
    private final Set<Path> folderPaths = new HashSet<>();

    public void load()
    {
        CTM_BLOCKS_MAP.clear();

        if (!Files.exists(resourcePacksDir))
        {
            try
            {
                Files.createDirectories(resourcePacksDir);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        for (File zipFileOrFolder : resourcePacksDir.toFile().listFiles())
        {
            if (zipFileOrFolder.isFile() && zipFileOrFolder.getName().endsWith(".zip"))
            {
                if (isZipCtmPack(zipFileOrFolder.toString()))
                {
                    if (isZipPackEligible(zipFileOrFolder.toString()))
                    {
                        CTM_SELECTOR_ARRAY_LIST.add(new CTMSelector(zipFileOrFolder.getName()));
                    }

                    Map<String, Boolean> currentPackOptions = new HashMap<>();

                    for (FileHeader fileHeader : listFilesInZipPack(zipFileOrFolder.toString()))
                    {
                        if (fileHeader.toString().contains(ctmPath))
                        {
                            if (fileHeader.toString().endsWith(".properties"))
                            {
                                currentPackOptions.put(fileHeader.toString()
                                        .split("/")[fileHeader.toString().split("/").length - 1]
                                        .replace(".properties", ""), true);
                            }
                            else if (fileHeader.toString().endsWith(".txt"))
                            {
                                currentPackOptions.put(fileHeader.toString()
                                        .split("/")[fileHeader.toString().split("/").length - 1]
                                        .replace(".txt", ""), false);
                            }
                        }
                    }
                }
            }
            else if (zipFileOrFolder.isDirectory() && isFolderCtmPack(zipFileOrFolder.getName()))
            {
                if (isFolderPackEligible(zipFileOrFolder.toPath()))
                {
                    CTM_SELECTOR_ARRAY_LIST.add(new CTMSelector(zipFileOrFolder.getName()));
                }

                CTMBlocks packCtmBlocks = new CTMBlocks(zipFileOrFolder.getName() + " (folder)");

                for (Path path : listFilesInFolderPack(zipFileOrFolder))
                {
                    if (path.toString().contains(ctmPath.replace("/", "\\")) && path.toFile().isFile())
                    {
                        if (path.toString().endsWith(".properties"))
                        {
                            Properties properties = new Properties();

                            try
                            {
                                properties.load(new FileInputStream(path.toFile()));
                            }
                            catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }

                            if (!properties.isEmpty())
                            {
                                String namespace = path.toString().split("\\\\")[Arrays.stream(path.toString()
                                        .split("\\\\")).toList().indexOf(zipFileOrFolder.getName()) + 2];

                                if (namespace.equals("minecraft")
                                        && (properties.containsKey("matchBlocks")
                                        || properties.containsKey("matchTiles")
                                        || properties.containsKey("ctmDisabled")
                                        || properties.containsKey("ctmTilesDisabled"))
                                )
                                {
                                    int index = Arrays.stream(path.toString().split("\\\\")).toList().indexOf(zipFileOrFolder.getName()) + 2;
                                    StringBuilder tmpPath = new StringBuilder();
                                    String[] splitPath = path.toString().split("\\\\");

                                    for (int i = 0; i < splitPath.length - 1; i++)
                                    {
                                        if (i > index)
                                        {
                                            tmpPath.append(splitPath[i]).append("/");
                                        }
                                    }

                                    if (properties.containsKey("method") && properties.containsKey("tiles"))
                                    {
                                        // CTM_COMPACT method
                                        if (properties.getProperty("method").equals("ctm_compact"))
                                        {
                                            String[] tiles = properties.getProperty("tiles").split("-"); // TODO -> Handle cases with spaces

                                            if (tiles.length == 2)
                                            {
                                                // If there are 5 textures => the texture when not connected is present,
                                                // so we use it
                                                if (Integer.parseInt(tiles[0]) + 4 == Integer.parseInt(tiles[1]))
                                                {
                                                    if (properties.containsKey("matchBlocks"))
                                                    {
                                                        for (String block : properties.getProperty("matchBlocks").split(" "))
                                                        {
                                                            packCtmBlocks.add(new CTMBlocks.CTMBlock(block,
                                                                    new Identifier(tmpPath + "%s.png".formatted(tiles[0])),
                                                                    true
                                                            ));
                                                        }
                                                    }
                                                    else if (properties.containsKey("matchTiles"))
                                                    {
                                                        for (String block : properties.getProperty("matchTiles").split(" "))
                                                        {
                                                            packCtmBlocks.add(new CTMBlocks.CTMBlock(block,
                                                                    new Identifier(tmpPath + "%s.png".formatted(tiles[0])),
                                                                    true
                                                            ));
                                                        }
                                                    }

                                                    if (properties.containsKey("ctmDisabled"))
                                                    {
                                                        for (String block : properties.getProperty("ctmDisabled").split(" "))
                                                        {
                                                            packCtmBlocks.add(new CTMBlocks.CTMBlock(block,
                                                                    new Identifier(tmpPath + "%s.png".formatted(tiles[0])),
                                                                    false
                                                            ));
                                                        }
                                                    }
                                                    else if (properties.containsKey("ctmTilesDisabled"))
                                                    {
                                                        for (String block : properties.getProperty("ctmTilesDisabled").split(" "))
                                                        {
                                                            packCtmBlocks.add(new CTMBlocks.CTMBlock(block,
                                                                    new Identifier(tmpPath + "%s.png".formatted(tiles[0])),
                                                                    false
                                                            ));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                else // Modded cases TODO -> implement
                                {
                                    System.out.println("Not implemented");
                                }
                            }
                        }
                    }
                }
                folderPaths.clear();
            }
        }
    }

    private boolean isZipCtmPack(String packPath)
    {
        try
        {
            for (FileHeader fileHeader : listFilesInZipPack(packPath))
            {
                if (fileHeader.toString().startsWith("assets") && fileHeader.toString().contains(ctmPath))
                {
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean isFolderCtmPack(String packName)
    {
        return Files.exists(Path.of(resourcePacksDir + "\\" + packName + "\\" + ctmPath));
    }

    private Set<FileHeader> listFilesInZipPack(String packPath)
    {
        Set<FileHeader> fileHeaders;

        try (ZipFile tmpZipFile = new ZipFile(packPath))
        {
            fileHeaders = new HashSet<>(tmpZipFile.getFileHeaders());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return fileHeaders;
    }

    /**
     * Lists the files in the given folder
     * <p>
     * {@code folderPaths.clear()} must be called after the iteration over the result of this functions, to prevent any weird behavior
     *
     * @param packFolder The folder whose files will be listed and returned
     * @return Returns the files present in the folder {@code packFolder}
     */
    private Set<Path> listFilesInFolderPack(@NotNull File packFolder)
    {
        for (File file : packFolder.listFiles())
        {
            if (file.isFile() && file.getName().endsWith(".properties")) // || file.getName().endsWith(".txt")))
            {
                folderPaths.add(file.toPath());
            }
            else if (file.isDirectory())
            {
                listFilesInFolderPack(file);
            }
        }
        return folderPaths;
    }

    public void updateUsedTextures(@NotNull String packName)
    {
        if (packName.endsWith(" (folder)"))
        {
            if (isFolderCtmPack(packName.replace(" (folder)", "")))
            {
                for (Path path : listFilesInFolderPack(new File(resourcePacksDir + "\\" + packName.replace(" (folder)", ""))))
                {
                    List<String> enabledBlocks = new ArrayList<>();
                    List<String> disabledBlocks = new ArrayList<>();
                    List<String> enabledTiles = new ArrayList<>();
                    List<String> disabledTiles = new ArrayList<>();
                    Properties properties = new Properties();

                    try (FileInputStream fileInputStream = new FileInputStream(path.toFile()))
                    {
                        properties.load(fileInputStream);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }

                    if (!properties.isEmpty())
                    {
                        // Loads the enabled and disabled options from the file
                        if (properties.containsKey("matchBlocks"))
                        {
                            enabledBlocks.addAll(Arrays.stream(properties.getProperty("matchBlocks").split(" ")).toList());
                        }
                        else if (properties.containsKey("matchTiles"))
                        {
                            enabledTiles.addAll(Arrays.stream(properties.getProperty("matchTiles").split(" ")).toList());
                        }

                        if (properties.containsKey("ctmDisabled"))
                        {
                            disabledBlocks.addAll(Arrays.stream(properties.getProperty("ctmDisabled").split(" ")).toList());
                        }
                        else if (properties.containsKey("ctmTilesDisabled"))
                        {
                            disabledTiles.addAll(Arrays.stream(properties.getProperty("ctmTilesDisabled").split(" ")).toList());
                        }

                        if (path.toString().contains(ctmPath.replace("/", "\\")))
                        {
                            if (path.toString().endsWith(".properties"))
                            {
                                // ENABLED BLOCKS
                                for (String optionName : enabledBlocks)
                                {
                                    boolean option = CTMBlocks.getOptionValue(packName, optionName);

                                    if (!option)
                                    {
                                        if (properties.containsKey("matchBlocks"))
                                        {
                                            ArrayList<String> matchBlocks = new ArrayList<>(List.of(properties.getProperty("matchBlocks").split(" ")));
                                            matchBlocks.remove(optionName);
                                            properties.put("matchBlocks", matchBlocks.toString()
                                                    .replace("[", "")
                                                    .replace("]", "")
                                                    .replace(",", "")
                                            );

                                            if (properties.containsKey("ctmDisabled"))
                                            {
                                                properties.put("ctmDisabled", properties.getProperty("ctmDisabled") + " " + optionName);
                                            }
                                            else
                                            {
                                                properties.put("ctmDisabled", optionName);
                                            }
                                        }
                                    }
                                }

                                // ENABLED TILES
                                for (String optionName : enabledTiles)
                                {
                                    boolean option = CTMBlocks.getOptionValue(packName, optionName);

                                    if (!option)
                                    {
                                        if (properties.containsKey("matchTiles"))
                                        {
                                            ArrayList<String> matchBlocks = new ArrayList<>(List.of(properties.getProperty("matchTiles").split(" ")));
                                            matchBlocks.remove(optionName);
                                            properties.put("matchTiles", matchBlocks.toString()
                                                    .replace("[", "")
                                                    .replace("]", "")
                                                    .replace(",", "")
                                            );

                                            if (properties.containsKey("ctmTilesDisabled"))
                                            {
                                                properties.put("ctmTilesDisabled", properties.getProperty("ctmTilesDisabled") + " " + optionName);
                                            }
                                            else
                                            {
                                                properties.put("ctmTilesDisabled", optionName);
                                            }
                                        }
                                    }
                                }

                                // DISABLED BLOCKS
                                for (String optionName : disabledBlocks)
                                {
                                    boolean option = CTMBlocks.getOptionValue(packName, optionName);

                                    if (option)
                                    {
                                        if (properties.containsKey("ctmDisabled"))
                                        {
                                            ArrayList<String> ctmDisabled = new ArrayList<>(List.of(properties.getProperty("ctmDisabled").split(" ")));
                                            ctmDisabled.remove(optionName);
                                            properties.put("ctmDisabled", ctmDisabled.toString()
                                                    .replace("[", "")
                                                    .replace("]", "")
                                                    .replace(",", "")
                                            );

                                            if (properties.containsKey("matchBlocks"))
                                            {
                                                properties.put("matchBlocks", properties.getProperty("matchBlocks") + " " + optionName);
                                            }
                                            else
                                            {
                                                properties.put("matchBlocks", optionName);
                                            }
                                        }
                                    }
                                }

                                // DISABLED TILES
                                for (String optionName : disabledTiles)
                                {
                                    boolean option = CTMBlocks.getOptionValue(packName, optionName);

                                    if (option)
                                    {
                                        if (properties.containsKey("ctmTilesDisabled"))
                                        {
                                            ArrayList<String> ctmDisabled = new ArrayList<>(List.of(properties.getProperty("ctmTilesDisabled").split(" ")));
                                            ctmDisabled.remove(optionName);
                                            properties.put("ctmTilesDisabled", ctmDisabled.toString()
                                                    .replace("[", "")
                                                    .replace("]", "")
                                                    .replace(",", "")
                                            );

                                            if (properties.containsKey("matchTiles"))
                                            {
                                                properties.put("matchTiles", properties.getProperty("matchTiles") + " " + optionName);
                                            }
                                            else
                                            {
                                                properties.put("matchTiles", optionName);
                                            }
                                        }
                                    }
                                }

                                try (FileOutputStream fos = new FileOutputStream(path.toFile()))
                                {
                                    if (properties.containsKey("matchBlocks"))
                                    {
                                        if (properties.getProperty("matchBlocks").isEmpty())
                                        {
                                            properties.remove("matchBlocks");
                                        }
                                    }
                                    else if (properties.containsKey("matchTiles"))
                                    {
                                        if (properties.getProperty("matchTiles").isEmpty())
                                        {
                                            properties.remove("matchTiles");
                                        }
                                    }

                                    if (properties.containsKey("ctmDisabled"))
                                    {
                                        if (properties.getProperty("ctmDisabled").isEmpty())
                                        {
                                            properties.remove("ctmDisabled");
                                        }
                                    }
                                    else if (properties.containsKey("ctmTilesDisabled"))
                                    {
                                        if (properties.getProperty("ctmTilesDisabled").isEmpty())
                                        {
                                            properties.remove("ctmTilesDisabled");
                                        }
                                    }
                                    properties.store(fos, null);
                                }
                                catch (IOException e)
                                {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
                folderPaths.clear();
                MinecraftClient.getInstance().reloadResources();
                load();
            }
        }
        else
        {
            // We disable the pack and reload the resources because the reloading makes the zip file accessible for writing
            MinecraftClient.getInstance().getResourcePackManager().disable("file/" + packName);
            MinecraftClient.getInstance().reloadResources();

            if (isZipCtmPack(resourcePacksDir + "\\" + packName))
            {
                Map<String, String> fileNamesMap = new HashMap<>();

                for (FileHeader fileHeader : listFilesInZipPack(resourcePacksDir + "\\" + packName))
                {
                    boolean option = CTMBlocks.getOptionValue(packName, fileHeader.toString()
                            .split("/")[fileHeader.toString().split("/").length - 1]
                            .replace(".properties", "")
                            .replace(".txt", ""));

                    if (fileHeader.toString().endsWith(".properties") && !option)
                    {
                        fileNamesMap.put(fileHeader.toString(), fileHeader.toString().replace(".properties", ".txt"));
                    }
                    else if (fileHeader.toString().endsWith(".txt") && option)
                    {
                        fileNamesMap.put(fileHeader.toString(), fileHeader.toString().replace(".txt", ".properties"));
                    }
                }

                if (!fileNamesMap.isEmpty())
                {
                    try (ZipFile zipFile = new ZipFile(resourcePacksDir + "\\" + packName))
                    {
                        zipFile.renameFiles(fileNamesMap);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
            MinecraftClient.getInstance().getResourcePackManager().enable("file/" + packName);
            MinecraftClient.getInstance().reloadResources();
        }
    }
}
