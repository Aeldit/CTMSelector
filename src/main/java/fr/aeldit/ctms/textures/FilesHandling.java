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
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static fr.aeldit.ctms.textures.CTMBlocks.CTM_BLOCKS_MAP;

public class FilesHandling
{
    private final Path resourcePacksDir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    private final String ctmPath = "assets/minecraft/optifine/ctm/connect/";
    private final Set<Path> folderPaths = new HashSet<>();

    public void load() // TODO -> categories with file tree
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
            if (zipFileOrFolder.isFile()
                    && zipFileOrFolder.getName().endsWith(".zip")
                    && isZipCtmPack(zipFileOrFolder.toString())
            )
            {
                // Controls
                /*if (isZipPackEligible(zipFileOrFolder.toString()))
                {
                    CTM_SELECTOR_ARRAY_LIST.add(new CTMSelector(zipFileOrFolder.getName()));
                }*/

                CTMBlocks packCtmBlocks = new CTMBlocks(zipFileOrFolder.getName());

                for (FileHeader fileHeader : listFilesInZipPack(zipFileOrFolder.toString()))
                {
                    if (fileHeader.toString().contains(ctmPath))
                    {
                        if (fileHeader.toString().endsWith(".properties"))
                        {
                            Properties properties = new Properties();

                            try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(zipFileOrFolder))
                            {
                                properties.load(zipFile.getInputStream(fileHeader));
                            }
                            catch (IOException e)
                            {
                                throw new RuntimeException(e);
                            }

                            if (!properties.isEmpty())
                            {
                                String namespace = fileHeader.toString().split("/")[1];

                                if (namespace.equals("minecraft")
                                        && (properties.containsKey("matchBlocks")
                                        || properties.containsKey("matchTiles")
                                        || properties.containsKey("ctmDisabled")
                                        || properties.containsKey("ctmTilesDisabled"))
                                )
                                {
                                    // Acquires the path used for the Identifier
                                    int index = Arrays.stream(fileHeader.toString().split("/")).toList().indexOf(zipFileOrFolder.getName()) + 2;
                                    StringBuilder tmpPath = new StringBuilder();
                                    String[] splitPath = fileHeader.toString().split("/");

                                    for (int i = 0; i < splitPath.length - 1; i++)
                                    {
                                        if (i > index)
                                        {
                                            tmpPath.append(splitPath[i]).append("/");
                                        }
                                    } // End of the Identifier path acquirement

                                    if (properties.containsKey("method") && properties.containsKey("tiles"))
                                    {
                                        // CTM_COMPACT method
                                        // Comments in the next IF statement are also for the CTM and HORIZONTAL / VERTICAL methods
                                        if (properties.getProperty("method").equals("ctm_compact"))
                                        {
                                            String[] spacedTiles = properties.getProperty("tiles").split(" ");

                                            if (spacedTiles[0].contains("-"))
                                            {
                                                String[] tiles = properties.getProperty("tiles").split("-");

                                                // Basic "start-end" textures
                                                // +
                                                // If the textures are referenced by name and their names are integers
                                                if (tiles.length == 2 && isDigits(tiles[0]) && isDigits(tiles[1]))
                                                {
                                                    // If there are 5 (0-4) textures => the texture when not connected is present,
                                                    // so we use it (texture 0)
                                                    if (Integer.parseInt(tiles[0]) + 4 == Integer.parseInt(tiles[1]))
                                                    {
                                                        packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[0]));
                                                    }
                                                }
                                            }
                                            else // If no "file range" (ex: "0-4") is found for the textures to use, we use the first that comes
                                            {
                                                packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
                                            }
                                        }
                                        // CTM method
                                        else if (properties.getProperty("method").equals("ctm"))
                                        {
                                            String[] spacedTiles = properties.getProperty("tiles").split(" ");

                                            if (spacedTiles[0].contains("-"))
                                            {
                                                String[] tiles = properties.getProperty("tiles").split("-");

                                                if (tiles.length == 2 && isDigits(tiles[0]) && isDigits(tiles[1]))
                                                {
                                                    // If there are 47 (0-46) textures => the texture when not connected is present,
                                                    // so we use it (texture 0)
                                                    if (Integer.parseInt(tiles[0]) + 46 == Integer.parseInt(tiles[1]))
                                                    {
                                                        packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[0]));
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
                                            }
                                        }
                                        // HORIZONTAL and VERTICAL methods
                                        else if (properties.getProperty("method").equals("horizontal")
                                                || properties.getProperty("method").equals("vertical")
                                                || properties.getProperty("method").equals("horizontal+vertical")
                                                || properties.getProperty("method").equals("vertical+horizontal")
                                        )
                                        {
                                            String[] spacedTiles = properties.getProperty("tiles").split(" ");

                                            if (spacedTiles[0].contains("-"))
                                            {
                                                String[] tiles = properties.getProperty("tiles").split("-");

                                                if (tiles.length == 2 && isDigits(tiles[0]) && isDigits(tiles[1]))
                                                {
                                                    // If there are 4 (0-3) textures => the texture when not connected is present,
                                                    // so we use it (texture 3)
                                                    if (Integer.parseInt(tiles[0]) + 3 == Integer.parseInt(tiles[1]))
                                                    {
                                                        packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[1]));
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
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
            }
            else if (zipFileOrFolder.isDirectory()
                    && isFolderCtmPack(zipFileOrFolder.getName())
            )
            {
                // Controls
                /*if (isFolderPackEligible(zipFileOrFolder.toPath()))
                {
                    CTM_SELECTOR_ARRAY_LIST.add(new CTMSelector(zipFileOrFolder.getName()));
                }*/

                CTMBlocks packCtmBlocks = new CTMBlocks(zipFileOrFolder.getName() + " (folder)");

                for (Path path : listFilesInFolderPack(zipFileOrFolder))
                {
                    if (path.toString().contains(ctmPath.replace("/", "\\"))
                            && path.toFile().isFile()
                    )
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
                                    // Acquires the path used for the Identifier
                                    int index = Arrays.stream(path.toString().split("\\\\")).toList().indexOf(zipFileOrFolder.getName()) + 2;
                                    StringBuilder tmpPath = new StringBuilder();
                                    String[] splitPath = path.toString().split("\\\\");

                                    for (int i = 0; i < splitPath.length - 1; i++)
                                    {
                                        if (i > index)
                                        {
                                            tmpPath.append(splitPath[i]).append("/");
                                        }
                                    } // End of the Identifier path acquirement

                                    if (properties.containsKey("method") && properties.containsKey("tiles"))
                                    {
                                        // CTM_COMPACT method
                                        // Comments in the next IF statement are also for the CTM and HORIZONTAL / VERTICAL methods
                                        if (properties.getProperty("method").equals("ctm_compact"))
                                        {
                                            String[] spacedTiles = properties.getProperty("tiles").split(" ");

                                            if (spacedTiles[0].contains("-"))
                                            {
                                                String[] tiles = properties.getProperty("tiles").split("-");

                                                // Basic "start-end" textures
                                                // +
                                                // If the textures are referenced by name and their names are integers
                                                if (tiles.length == 2 && isDigits(tiles[0]) && isDigits(tiles[1]))
                                                {
                                                    // If there are 5 (0-4) textures => the texture when not connected is present,
                                                    // so we use it (texture 0)
                                                    if (Integer.parseInt(tiles[0]) + 4 == Integer.parseInt(tiles[1]))
                                                    {
                                                        packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[0]));
                                                    }
                                                }
                                            }
                                            else // If no "file range" (ex: "0-4") is found for the textures to use, we use the first that comes
                                            {
                                                packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
                                            }
                                        }
                                        // CTM method
                                        else if (properties.getProperty("method").equals("ctm"))
                                        {
                                            String[] spacedTiles = properties.getProperty("tiles").split(" ");

                                            if (spacedTiles[0].contains("-"))
                                            {
                                                String[] tiles = properties.getProperty("tiles").split("-");

                                                if (tiles.length == 2 && isDigits(tiles[0]) && isDigits(tiles[1]))
                                                {
                                                    // If there are 47 (0-46) textures => the texture when not connected is present,
                                                    // so we use it (texture 0)
                                                    if (Integer.parseInt(tiles[0]) + 46 == Integer.parseInt(tiles[1]))
                                                    {
                                                        packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[0]));
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
                                            }
                                        }
                                        // HORIZONTAL and VERTICAL methods
                                        else if (properties.getProperty("method").equals("horizontal")
                                                || properties.getProperty("method").equals("vertical")
                                                || properties.getProperty("method").equals("horizontal+vertical")
                                                || properties.getProperty("method").equals("vertical+horizontal")
                                        )
                                        {
                                            String[] spacedTiles = properties.getProperty("tiles").split(" ");

                                            if (spacedTiles[0].contains("-"))
                                            {
                                                String[] tiles = properties.getProperty("tiles").split("-");

                                                if (tiles.length == 2 && isDigits(tiles[0]) && isDigits(tiles[1]))
                                                {
                                                    // If there are 4 (0-3) textures => the texture when not connected is present,
                                                    // so we use it (texture 3)
                                                    if (Integer.parseInt(tiles[0]) + 3 == Integer.parseInt(tiles[1]))
                                                    {
                                                        packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[1]));
                                                    }
                                                }
                                            }
                                            else
                                            {
                                                packCtmBlocks.addAll(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
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

    @Contract(pure = true)
    private boolean isDigits(@NotNull String s)
    {
        for (char c : s.toCharArray())
        {
            if (!Character.isDigit(c))
            {
                return false;
            }
        }
        return true;
    }

    private @NotNull ArrayList<CTMBlocks.CTMBlock> getCTMBlocksInProperties(
            @NotNull Properties properties, String tmpPath, @NotNull String startTile
    )
    {
        ArrayList<CTMBlocks.CTMBlock> ctmBlocks = new ArrayList<>();

        // If the extension of the file is given
        if (startTile.endsWith(".png"))
        {
            startTile = startTile.replace(".png", "");
        }
        // If the texture file is a full path
        if (startTile.contains("/"))
        {
            tmpPath = "";
        }

        if (properties.containsKey("matchBlocks"))
        {
            for (String block : properties.getProperty("matchBlocks").split(" "))
            {
                ctmBlocks.add(new CTMBlocks.CTMBlock(block,
                        new Identifier(tmpPath + "%s.png".formatted(startTile)),
                        true
                ));
            }
        }
        else if (properties.containsKey("matchTiles"))
        {
            for (String block : properties.getProperty("matchTiles").split(" "))
            {
                ctmBlocks.add(new CTMBlocks.CTMBlock(block,
                        new Identifier(tmpPath + "%s.png".formatted(startTile)),
                        true
                ));
            }
        }

        if (properties.containsKey("ctmDisabled"))
        {
            for (String block : properties.getProperty("ctmDisabled").split(" "))
            {
                ctmBlocks.add(new CTMBlocks.CTMBlock(block,
                        new Identifier(tmpPath + "%s.png".formatted(startTile)),
                        false
                ));
            }
        }
        else if (properties.containsKey("ctmTilesDisabled"))
        {
            for (String block : properties.getProperty("ctmTilesDisabled").split(" "))
            {
                ctmBlocks.add(new CTMBlocks.CTMBlock(block,
                        new Identifier(tmpPath + "%s.png".formatted(startTile)),
                        false
                ));
            }
        }
        return ctmBlocks;
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

        try (net.lingala.zip4j.ZipFile tmpZipFile = new net.lingala.zip4j.ZipFile(packPath))
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

    public void updateUsedTextures(@NotNull String packName) // TODO -> make work with zip files
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

                        // Toggles the options in the file
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

            String packPath = resourcePacksDir + "\\" + packName;

            if (isZipCtmPack(packPath))
            {
                Map<String, Path> headersPath = new HashMap<>();

                for (FileHeader fileHeader : listFilesInZipPack(packPath))
                {
                    if (fileHeader.toString().endsWith(".properties"))
                    {
                        List<String> enabledBlocks = new ArrayList<>();
                        List<String> disabledBlocks = new ArrayList<>();
                        List<String> enabledTiles = new ArrayList<>();
                        List<String> disabledTiles = new ArrayList<>();

                        Properties properties = new Properties();

                        try (net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(packPath))
                        {
                            properties.load(zipFile.getInputStream(fileHeader));
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

                            // Toggles the options in the file
                            if (fileHeader.toString().contains(ctmPath))
                            {
                                boolean changed = false;

                                // ENABLED BLOCKS
                                for (String optionName : enabledBlocks)
                                {
                                    boolean option = CTMBlocks.getOptionValue(packName, optionName);

                                    if (!option)
                                    {
                                        if (properties.containsKey("matchBlocks"))
                                        {
                                            changed = true;
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
                                            changed = true;
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
                                            changed = true;
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
                                            changed = true;
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

                                if (changed)
                                {
                                    Path tmpPath = Path.of(resourcePacksDir + "\\" + "ctmsTmp");

                                    if (!Files.exists(tmpPath))
                                    {
                                        try
                                        {
                                            Files.createDirectories(tmpPath);
                                        }
                                        catch (IOException e)
                                        {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                    Path tmpFilePath = Path.of(tmpPath + "\\"
                                            + fileHeader.toString().split("/")[fileHeader.toString().split("/").length - 1]);
                                    headersPath.put(fileHeader.toString(), tmpFilePath);

                                    try
                                    {
                                        Files.deleteIfExists(tmpFilePath);
                                        Files.createFile(tmpFilePath);
                                    }
                                    catch (IOException e)
                                    {
                                        throw new RuntimeException(e);
                                    }

                                    try (FileOutputStream fos = new FileOutputStream(tmpFilePath.toFile()))
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
                }

                Map<String, String> env = new HashMap<>();
                env.put("create", "true");
                Path path = Paths.get(packPath);
                URI uri = URI.create("jar:" + path.toUri());

                try (FileSystem fs = FileSystems.newFileSystem(uri, env))
                {
                    for (Map.Entry<String, Path> entry : headersPath.entrySet())
                    {
                        Path nf = fs.getPath(entry.getKey());
                        // TODO -> test with an array of bytes instead of a file
                        Files.write(nf, Files.readAllBytes(entry.getValue()), StandardOpenOption.CREATE);
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            MinecraftClient.getInstance().getResourcePackManager().enable("file/" + packName);
            MinecraftClient.getInstance().reloadResources();
        }
    }
}
