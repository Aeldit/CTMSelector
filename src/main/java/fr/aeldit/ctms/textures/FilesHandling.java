package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static fr.aeldit.ctms.Utils.*;

public abstract class FilesHandling
{
    private static final String CTM_PATH = "optifine/ctm/";
    private static final String[] TYPES = {"matchBlocks", "matchTiles", "ctmDisabled", "ctmTilesDisabled"};

    public static void loadCTMPacks()
    {
        CTM_PACKS = new CTMPacks();

        if (!Files.exists(RESOURCE_PACKS_DIR))
        {
            return;
        }

        File[] files = RESOURCE_PACKS_DIR.toFile().listFiles();
        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (file.isFile() && file.getName().endsWith(".zip"))
            {
                try (ZipFile zipFile = new ZipFile(file))
                {
                    if (zipNotACTMPack(zipFile.getFileHeaders()))
                    {
                        zipFile.close();
                        continue;
                    }

                    CTM_PACKS.add(new CTMPack(zipFile));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
            else if (file.isDirectory() && isFolderCtmPack(file.toPath()))
            {
                CTM_PACKS.add(new CTMPack(file));
            }
        }
    }

    private static boolean zipNotACTMPack(@NotNull List<FileHeader> fileHeaders)
    {
        for (FileHeader fileHeader : fileHeaders)
        {
            if (fileHeader.toString().startsWith("assets") && fileHeader.toString().contains(CTM_PATH))
            {
                return false;
            }
        }
        return true;
    }

    private static boolean isFolderCtmPack(Path packPath)
    {
        Path filePath = Path.of("%s/assets".formatted(packPath));
        if (Files.exists(filePath))
        {
            File[] files = filePath.toFile().listFiles();
            if (files == null)
            {
                return false;
            }

            // Iterates over the namespaces
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    if (Files.exists(Path.of("%s/optifine/ctm".formatted(file))))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Lists the files in the given folder
     * <p>
     * {@code folderPaths.clearAvailableCTMPacks()} must be called after the iteration over the result of this function,
     * to prevent any weird behavior
     *
     * @param packFolder The folder whose files will be listed and returned
     * @return Returns the files present in the folder {@code packFolder}
     */
    private static @NotNull ArrayList<Path> getFilesInFolderPack(@NotNull File packFolder)
    {
        ArrayList<Path> paths = new ArrayList<>();
        listFilesInFolderPack(packFolder, paths);
        return paths;
    }

    private static void listFilesInFolderPack(@NotNull File packFolder, ArrayList<Path> paths)
    {
        File[] files = packFolder.listFiles();
        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (file.isFile() && file.getName().endsWith(".properties"))
            {
                paths.add(file.toPath());
            }
            else if (file.isDirectory())
            {
                listFilesInFolderPack(file, paths);
            }
        }
    }

    public static void updateUsedTextures(@NotNull CTMPack ctmPack)
    {
        if (ctmPack.isFolder())
        {
            boolean changed = false;

            // We use Path.of() to be sure that the path is correct, independently of the OS of the user
            ArrayList<Path> paths = getFilesInFolderPack(
                    new File(Path.of("%s/%s".formatted(RESOURCE_PACKS_DIR, ctmPack.getName())).toString())
            );
            for (Path path : paths)
            {
                // ArrayLists have by default a size of 16, but ctm properties usually have only 1 block, so we don't
                // need these 15 extra slots in the array
                ArrayList<String> enabledBlocks = new ArrayList<>(1);
                ArrayList<String> enabledTiles = new ArrayList<>(1);
                ArrayList<String> disabledBlocks = new ArrayList<>(1);
                ArrayList<String> disabledTiles = new ArrayList<>(1);

                Properties properties = new Properties();
                try (FileInputStream fileInputStream = new FileInputStream(path.toFile()))
                {
                    properties.load(fileInputStream);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                if (properties.isEmpty())
                {
                    continue;
                }

                // Loads the enabled and disabled blocks from the properties
                fillBlocksLists(properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                if (!path.toString().replace("\\", "/").contains(CTM_PATH))
                {
                    continue;
                }

                // Toggles the blocks / tiles states (enabled / disabled)
                if (path.toString().endsWith(".properties"))
                {
                    boolean localChanged = updateProperties(
                            ctmPack, properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles
                    );

                    changed |= localChanged;

                    if (localChanged)
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

            if (changed)
            {
                MinecraftClient.getInstance().reloadResources();
            }
        }
        else
        {
            String packPath = Path.of("%s/%s".formatted(RESOURCE_PACKS_DIR, ctmPack.getName())).toString();

            HashMap<String, byte[]> headersBytes = new HashMap<>();

            try (ZipFile zipFile = new ZipFile(packPath))
            {
                List<FileHeader> fileHeaders = zipFile.getFileHeaders();
                if (zipNotACTMPack(fileHeaders))
                {
                    zipFile.close();
                    return;
                }

                for (FileHeader fileHeader : fileHeaders)
                {
                    if (!fileHeader.toString().contains(CTM_PATH))
                    {
                        continue;
                    }

                    if (fileHeader.toString().endsWith(".properties"))
                    {
                        // We initialize then ArrayLists with a size of 1 because it is most likely that there will
                        // be only 1 block in each file. Multiple blocks per file is less common
                        ArrayList<String> enabledBlocks = new ArrayList<>(1);
                        ArrayList<String> enabledTiles = new ArrayList<>(1);
                        ArrayList<String> disabledBlocks = new ArrayList<>(1);
                        ArrayList<String> disabledTiles = new ArrayList<>(1);

                        Properties properties = new Properties();
                        properties.load(zipFile.getInputStream(fileHeader));

                        if (properties.isEmpty())
                        {
                            continue;
                        }

                        // Loads the enabled and disabled blocks/tiles from the file
                        fillBlocksLists(properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                        // Changes the blocks state (enabled/disabled) in the file
                        boolean changed = updateProperties(
                                ctmPack, properties, enabledBlocks, enabledTiles,
                                disabledBlocks, disabledTiles
                        );
                        System.out.println(changed);

                        if (changed)
                        {
                            removeEmptyKeys(properties);

                            // We take the properties in a byte array,
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
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            if (!headersBytes.isEmpty())
            {
                // We disable the pack and reload the resources because the reloading makes the zip file
                // accessible for writing, due to no longer being loaded by Minecraft
                MinecraftClient.getInstance().getResourcePackManager().disable("file/%s".formatted(ctmPack.getName()));
                MinecraftClient.getInstance().reloadResources();

                writeBytesToZip(packPath, headersBytes);

                MinecraftClient.getInstance().getResourcePackManager().enable("file/%s".formatted(ctmPack.getName()));
                MinecraftClient.getInstance().reloadResources();
            }
        }
        loadCTMPacks();
    }

    /**
     * Loads the enabled and disabled options from the file into each ArrayList passed as arguments
     *
     * @param properties     The properties object
     * @param enabledBlocks  The list of enabledBlocks
     * @param enabledTiles   The list of enabledTiles
     * @param disabledBlocks The list of disabledBlocks
     * @param disabledTiles  The list of disabledTiles
     */
    private static void fillBlocksLists(
            @NotNull Properties properties,
            ArrayList<String> enabledBlocks, ArrayList<String> enabledTiles,
            ArrayList<String> disabledBlocks, ArrayList<String> disabledTiles
    )
    {
        if (properties.containsKey("matchBlocks"))
        {
            enabledBlocks.addAll(List.of(properties.getProperty("matchBlocks").split(" ")));
        }
        else if (properties.containsKey("matchTiles"))
        {
            enabledTiles.addAll(List.of(properties.getProperty("matchTiles").split(" ")));
        }

        if (properties.containsKey("ctmDisabled"))
        {
            disabledBlocks.addAll(List.of(properties.getProperty("ctmDisabled").split(" ")));
        }
        else if (properties.containsKey("ctmTilesDisabled"))
        {
            disabledTiles.addAll(List.of(properties.getProperty("ctmTilesDisabled").split(" ")));
        }
    }

    /**
     * Removes the empty keys from the given properties object
     *
     * @param properties The properties object
     */
    private static void removeEmptyKeys(@NotNull Properties properties)
    {
        for (String type : TYPES)
        {
            if (properties.containsKey(type))
            {
                if (properties.getProperty(type).isEmpty())
                {
                    properties.remove(type);
                }
            }
        }
    }

    /**
     * For every block changed by the user, write the changes to the files
     *
     * @param ctmPack    The {@link CTMPack} object
     * @param properties The {@link Properties} instance to update
     * @return Whether a property was changed
     */
    private static boolean updateProperties(
            CTMPack ctmPack, Properties properties,
            @NotNull ArrayList<String> enabledBlocks, @NotNull ArrayList<String> enabledTiles,
            @NotNull ArrayList<String> disabledBlocks, @NotNull ArrayList<String> disabledTiles
    )
    {
        boolean changed = false;

        List<ArrayList<String>> blocks = List.of(enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

        // ENABLED BLOCKS and TILES
        for (int i = 0; i < 2; ++i)
        {
            for (String blockOrTile : blocks.get(i))
            {
                // If the block is enabled in the user's change list, we don't do anything as we are iterating over the
                // enabled blocks
                if (ctmPack.isBlockEnabled(blockOrTile))
                {
                    continue;
                }

                // This property contains the enabled blocks or tiles (depending on the current TYPE)
                String property = properties.getProperty(TYPES[i]);
                if (property == null || property.isEmpty())
                {
                    continue;
                }

                // Stores the fact that we changed at least one value, which means that we will need to reload the
                // textures
                changed = true;

                // We remove the block that was changed by the user
                ArrayList<String> blocksOrTiles = new ArrayList<>(List.of(property.split(" ")));
                blocksOrTiles.remove(blockOrTile);

                // And we write the modified property
                properties.put(
                        TYPES[i], blocksOrTiles.toString()
                                               .replace("[", "")
                                               .replace("]", "")
                                               .replace(",", "")
                );

                // We add the removed block to the opposite property type
                // matchBlocks <=> ctmDisabled
                // matchTiles <=> ctmTilesDisabled
                String opposite = TYPES[i + 2];
                if (properties.containsKey(opposite))
                {
                    properties.put(
                            opposite,
                            "%s %s".formatted(properties.getProperty(opposite), blockOrTile)
                    );
                }
                else
                {
                    properties.put(opposite, blockOrTile);
                }
            }
        }

        // DISABLED BLOCKS and TILES
        for (int i = 2; i < 4; ++i)
        {
            for (String optionName : blocks.get(i))
            {
                // If the block is disabled in the user's change list, we don't do anything as we are iterating over the
                // disabled blocks
                if (!ctmPack.isBlockEnabled(optionName))
                {
                    continue;
                }

                // This property contains the disabled blocks or tiles (depending on the current TYPE)
                String property = properties.getProperty(TYPES[i]);
                if (property == null || property.isEmpty())
                {
                    continue;
                }

                // Stores the fact that we changed at least one value, which means that we will need to reload the
                // textures
                changed = true;

                // We remove the block that was changed by the user
                ArrayList<String> currentType = new ArrayList<>(List.of(property.split(" ")));
                currentType.remove(optionName);

                properties.put(
                        TYPES[i], currentType.toString()
                                             .replace("[", "")
                                             .replace("]", "")
                                             .replace(",", "")
                );

                // We add the removed block to the opposite property type
                // ctmDisabled <=> matchBlocks
                // ctmTilesDisabled <=> matchTiles
                String opposite = TYPES[i - 2];
                if (properties.containsKey(opposite))
                {
                    properties.put(
                            opposite,
                            "%s %s".formatted(properties.getProperty(opposite), optionName)
                    );
                }
                else
                {
                    properties.put(opposite, optionName);
                }
            }
        }
        return changed;
    }
}