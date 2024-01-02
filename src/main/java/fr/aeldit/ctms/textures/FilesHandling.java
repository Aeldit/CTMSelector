/*
 * Copyright (c) 2023-2024  -  Made by Aeldit
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

import fr.aeldit.ctms.gui.entryTypes.CTMBlock;
import fr.aeldit.ctms.gui.entryTypes.CTMPack;
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
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static fr.aeldit.ctms.util.Utils.CTM_PACKS;

public class FilesHandling
{
    private final Path resourcePacksDir = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");
    private final String ctmPath = "assets/minecraft/optifine/ctm/";
    private final List<Path> folderPaths = new ArrayList<>();

    public boolean packsChanged(@NotNull List<CTMPack> previousAvailableCTMPacks, @NotNull List<CTMPack> availableCTMPacks)
    {
        if (previousAvailableCTMPacks.size() != availableCTMPacks.size())
        {
            return true;
        }

        boolean changed = false;
        int ctr = 0;
        int size = previousAvailableCTMPacks.size();

        for (CTMPack ctmPack : previousAvailableCTMPacks)
        {
            for (CTMPack ctmPack1 : availableCTMPacks)
            {
                if (ctmPack.getName().equals(ctmPack1.getName()))
                {
                    ctr++;
                    break;
                }
            }
        }

        if (ctr != size)
        {
            changed = true;
        }

        ctr = 0;

        for (CTMPack ctmPack : availableCTMPacks)
        {
            for (CTMPack ctmPack1 : previousAvailableCTMPacks)
            {
                if (ctmPack.getName().equals(ctmPack1.getName()))
                {
                    ctr++;
                    break;
                }
            }
        }

        if (ctr != size)
        {
            changed = true;
        }
        return changed;
    }

    public void load()
    {
        load(false);
    }

    public void load(boolean initial)
    {
        // Obtains the ctm packs before the reload to check later if any of them
        // was removed, or if any were added
        List<CTMPack> previousAvailableCTMPacks = null;
        if (CTM_PACKS != null)
        {
            previousAvailableCTMPacks = new ArrayList<>(CTM_PACKS.getAvailableCTMPacks());
            CTM_PACKS.clearAvailableCTMPacks();
        }

        CTM_PACKS = new CTMPacks();

        if (!Files.exists(resourcePacksDir))
        {
            return;
        }

        for (File file : resourcePacksDir.toFile().listFiles())
        {
            if (file.isFile() && file.getName().endsWith(".zip") && isZipCtmPack(file.toString()))
            {
                CTMPack ctmPack = new CTMPack(file.getName(), false);
                CTM_PACKS.add(ctmPack);

                try (ZipFile zipFile = new ZipFile(file))
                {
                    for (FileHeader fileHeader : zipFile.getFileHeaders())
                    {
                        if (fileHeader.toString().contains(ctmPath))
                        {
                            if (fileHeader.toString().endsWith(".properties"))
                            {
                                Properties properties = new Properties();
                                properties.load(zipFile.getInputStream(fileHeader));

                                if (!properties.isEmpty())
                                {
                                    loadOptions(properties, ctmPack, fileHeader.toString(), file, "/");
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
            else if (file.isDirectory() && isFolderCtmPack(file.getName()))
            {
                CTMPack ctmPack = new CTMPack(file.getName(), true);
                CTM_PACKS.add(ctmPack);

                for (Path path : listFilesInFolderPack(file))
                {
                    if (path.toString().contains(Path.of(ctmPath).toString()) && path.toFile().isFile())
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
                                loadOptions(properties, ctmPack, path.toString(), file, "\\\\");
                            }
                        }
                    }
                }
                folderPaths.clear();
            }
        }

        // If the packs changed, we create a new iconsPack
        if (initial || (previousAvailableCTMPacks != null && packsChanged(previousAvailableCTMPacks, CTM_PACKS.getAvailableCTMPacks())))
        {
            CTM_PACKS.createIconsPack(initial);
        }
    }

    private void loadOptions(Properties properties, CTMPack ctmPack, @NotNull String path,
                             @NotNull File zipFileOrFolder, String pathSep
    )
    {
        String namespace = path.split(pathSep)[Arrays.stream(path
                .split(pathSep)).toList().indexOf(zipFileOrFolder.getName()) + 2];

        if (namespace.equals("minecraft")
                && (properties.containsKey("matchBlocks")
                || properties.containsKey("matchTiles")
                || properties.containsKey("ctmDisabled")
                || properties.containsKey("ctmTilesDisabled"))
        )
        {
            // Acquires the path used for the Identifier
            int index = Arrays.stream(path.split(pathSep)).toList().indexOf(zipFileOrFolder.getName()) + 2;
            StringBuilder tmpPath = new StringBuilder();
            String[] splitPath = path.split(pathSep);

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
                                ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[0]));
                            }
                        }
                    }
                    else // If no "file range" (ex: "0-4") is found for the textures to use, we use the first that comes
                    {
                        ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
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
                                ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[0]));
                            }
                        }
                    }
                    else
                    {
                        ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
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
                                ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[1]));
                            }
                        }
                    }
                    else
                    {
                        ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]));
                    }
                }
            }
        }
        // TODO -> implement modded cases
    }

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

    /**
     * Acquires the blocks in the given properties file
     *
     * @param properties The properties object
     * @param tmpPath    The path to the texture
     * @param startTile  The texture
     * @return An arraylist of {@link CTMBlock} containing the blocks with their Identifier initialized
     */
    private @NotNull ArrayList<CTMBlock> getCTMBlocksInProperties(
            Properties properties, String tmpPath, @NotNull String startTile
    )
    {
        ArrayList<CTMBlock> ctmBlocks = new ArrayList<>();

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
                ctmBlocks.add(new CTMBlock(block,
                        new Identifier(tmpPath + "%s.png".formatted(startTile)),
                        true
                ));
            }
        }
        else if (properties.containsKey("matchTiles"))
        {
            for (String block : properties.getProperty("matchTiles").split(" "))
            {
                ctmBlocks.add(new CTMBlock(block,
                        new Identifier(tmpPath + "%s.png".formatted(startTile)),
                        true
                ));
            }
        }

        if (properties.containsKey("ctmDisabled"))
        {
            for (String block : properties.getProperty("ctmDisabled").split(" "))
            {
                ctmBlocks.add(new CTMBlock(block,
                        new Identifier(tmpPath + "%s.png".formatted(startTile)),
                        false
                ));
            }
        }
        else if (properties.containsKey("ctmTilesDisabled"))
        {
            for (String block : properties.getProperty("ctmTilesDisabled").split(" "))
            {
                ctmBlocks.add(new CTMBlock(block,
                        new Identifier(tmpPath + "%s.png".formatted(startTile)),
                        false
                ));
            }
        }
        return ctmBlocks;
    }

    private boolean isZipCtmPack(String packPath)
    {
        try (ZipFile tmpZipFile = new ZipFile(packPath))
        {
            for (FileHeader fileHeader : tmpZipFile.getFileHeaders())
            {
                if (fileHeader.toString().startsWith("assets") && fileHeader.toString().contains(ctmPath))
                {
                    return true;
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean isFolderCtmPack(String packName)
    {
        return Files.exists(Path.of(resourcePacksDir + "/" + packName + "/" + ctmPath));
    }

    /**
     * Lists the files in the given folder
     * <p>
     * {@code folderPaths.clearAvailableCTMPacks()} must be called after the iteration over the result of this functions, to prevent any weird behavior
     *
     * @param packFolder The folder whose files will be listed and returned
     * @return Returns the files present in the folder {@code packFolder}
     */
    private List<Path> listFilesInFolderPack(@NotNull File packFolder)
    {
        for (File file : packFolder.listFiles())
        {
            if (file.isFile() && file.getName().endsWith(".properties"))
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

    public void updateUsedTextures(@NotNull CTMPack ctmPack)
    {
        if (ctmPack.isFolder())
        {
            for (Path path : listFilesInFolderPack(new File(resourcePacksDir + "\\" + ctmPack.getName())))
            {
                List<String> enabledBlocks = new ArrayList<>();
                List<String> enabledTiles = new ArrayList<>();
                List<String> disabledBlocks = new ArrayList<>();
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
                    fillBlocksLists(properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                    // Toggles the options in the file
                    if (path.toString().contains(ctmPath.replace("/", "\\")))
                    {
                        if (path.toString().endsWith(".properties"))
                        {
                            boolean changed = updateProperties(ctmPack, properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                            if (changed)
                            {
                                try (FileOutputStream fos = new FileOutputStream(path.toFile()))
                                {
                                    removeEmptyKeys(properties);
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
            folderPaths.clear();
            MinecraftClient.getInstance().reloadResources();
        }
        else
        {
            // We disable the pack and reload the resources because the reloading makes the zip file accessible for writing
            MinecraftClient.getInstance().getResourcePackManager().disable("file/" + ctmPack.getName());
            MinecraftClient.getInstance().reloadResources();

            String packPath = resourcePacksDir + "\\" + ctmPack.getName();

            if (isZipCtmPack(packPath))
            {
                Map<String, byte[]> headersBytes = new HashMap<>();

                try (ZipFile zipFile = new ZipFile(packPath))
                {
                    for (FileHeader fileHeader : zipFile.getFileHeaders())
                    {
                        if (fileHeader.toString().endsWith(".properties"))
                        {
                            List<String> enabledBlocks = new ArrayList<>();
                            List<String> enabledTiles = new ArrayList<>();
                            List<String> disabledBlocks = new ArrayList<>();
                            List<String> disabledTiles = new ArrayList<>();

                            Properties properties = new Properties();
                            properties.load(zipFile.getInputStream(fileHeader));

                            if (!properties.isEmpty())
                            {
                                // Loads the enabled and disabled options from the file
                                fillBlocksLists(properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                                // Toggles the options in the file
                                if (fileHeader.toString().contains(ctmPath))
                                {
                                    boolean changed = updateProperties(ctmPack, properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                                    if (changed)
                                    {
                                        removeEmptyKeys(properties);

                                        // We take the properties in a byte array
                                        // so we can write it in the zip later
                                        byte[] tmp = properties.toString()
                                                .replace("{", "")
                                                .replace("}", "")
                                                .replace(", ", "\n")
                                                .getBytes();
                                        headersBytes.put(fileHeader.toString(), tmp);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                if (!headersBytes.isEmpty())
                {
                    // Mounts the zip file and adds files to it using the FileSystem
                    // The bytes written in the files are the ones we obtain
                    // from the properties files
                    Map<String, String> env = new HashMap<>();
                    env.put("create", "true");
                    Path path = Paths.get(packPath);
                    URI uri = URI.create("jar:" + path.toUri());

                    try (FileSystem fs = FileSystems.newFileSystem(uri, env))
                    {
                        for (Map.Entry<String, byte[]> entry : headersBytes.entrySet())
                        {
                            Path nf = fs.getPath(entry.getKey());
                            Files.write(nf, entry.getValue(), StandardOpenOption.CREATE);
                        }
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
            MinecraftClient.getInstance().getResourcePackManager().enable("file/" + ctmPack.getName());
            MinecraftClient.getInstance().reloadResources();
        }
        load();
    }

    private void fillBlocksLists(
            @NotNull Properties properties,
            List<String> enabledBlocks, List<String> enabledTiles,
            List<String> disabledBlocks, List<String> disabledTiles
    )
    {
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
    }

    /**
     * Removes the empty keys from the given properties object
     *
     * @param properties The properties object
     */
    private void removeEmptyKeys(@NotNull Properties properties)
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
    }

    private boolean updateProperties(
            CTMPack ctmPack, Properties properties,
            @NotNull List<String> enabledBlocks, @NotNull List<String> enabledTiles,
            @NotNull List<String> disabledBlocks, @NotNull List<String> disabledTiles
    )
    {
        boolean changed = false;

        // ENABLED BLOCKS
        for (String optionName : enabledBlocks)
        {
            boolean option = ctmPack.getOptionValue(optionName);

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
            boolean option = ctmPack.getOptionValue(optionName);

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
            boolean option = ctmPack.getOptionValue(optionName);

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
            boolean option = ctmPack.getOptionValue(optionName);

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
        return changed;
    }
}
