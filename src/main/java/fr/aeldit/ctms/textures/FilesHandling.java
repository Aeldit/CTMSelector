package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
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

import static fr.aeldit.ctms.Utils.*;
import static fr.aeldit.ctms.textures.CTMSelector.*;

public class FilesHandling
{
    private final String ctmPath = "optifine/ctm/";
    private final String[] types = {"matchBlocks", "matchTiles", "ctmDisabled", "ctmTilesDisabled"};

    public void load()
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
                    if (zipNotACTMPack(zipFile))
                    {
                        zipFile.close();
                        continue;
                    }

                    boolean hasCTMSelector = CTMSelector.hasCTMSelector(zipFile);

                    CTMPack ctmPack = new CTMPack(file.getName(), false, hasCTMSelector, isPackModded(zipFile));
                    CTM_PACKS.add(ctmPack);

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
                                    loadOptions(properties, ctmPack, fileHeader.toString(), file);
                                }
                            }
                        }
                    }

                    // If the pack has a controls file, we add the already existing CTMBlock objects to the ArrayList
                    // in the Group object
                    if (hasCTMSelector)
                    {
                        for (Group group : ctmPack.getCtmSelector().getGroups())
                        {
                            ArrayList<FileHeader> fileHeaders = group.getPropertiesFilesFileHeaders();
                            if (fileHeaders == null)
                            {
                                continue;
                            }

                            for (FileHeader fileHeader : fileHeaders)
                            {
                                ArrayList<String> blocksNames = getCTMBlocksNamesInZipProperties(fileHeader, zipFile);
                                if (blocksNames == null)
                                {
                                    continue;
                                }

                                for (String blockName : blocksNames)
                                {
                                    CTMBlock ctmBlock = ctmPack.getCTMBlockByName(blockName);
                                    if (ctmBlock != null)
                                    {
                                        group.addContainedBlock(ctmBlock);
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
            }
            else if (file.isDirectory() && isFolderCtmPack(file.toPath()))
            {
                boolean hasCTMSelector = hasCTMSelector(file.toPath());

                CTMPack ctmPack = new CTMPack(file.getName(), true, hasCTMSelector, isPackModded(file.toPath()));
                CTM_PACKS.add(ctmPack);

                for (Path path : getFilesInFolderPack(file))
                {
                    if (path.toString().replace("\\", "/").contains(ctmPath) && path.toFile().isFile())
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
                                loadOptions(properties, ctmPack, path.toString(), file);
                            }
                        }
                    }
                }

                // If the pack has a controls file, we add the already existing CTMBlock objects to the ArrayList in the
                // Group object
                if (hasCTMSelector)
                {
                    for (Group group : ctmPack.getCtmSelector().getGroups())
                    {
                        ArrayList<Path> paths = group.getPropertiesFilesPaths();
                        if (paths == null)
                        {
                            continue;
                        }

                        for (Path path : paths)
                        {
                            ArrayList<String> blocksNames = getCTMBlocksNamesInProperties(path);
                            if (blocksNames == null)
                            {
                                continue;
                            }

                            for (String blockName : blocksNames)
                            {
                                CTMBlock ctmBlock = ctmPack.getCTMBlockByName(blockName);
                                if (ctmBlock != null)
                                {
                                    group.addContainedBlock(ctmBlock);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadOptions(
            @NotNull Properties properties, CTMPack ctmPack, @NotNull String path,
            @NotNull File zipFileOrFolder
    )
    {
        String namespace =
                path.split("/")[Arrays.stream(path.split("/")).toList().indexOf(zipFileOrFolder.getName()) + 2];

        if (properties.containsKey("matchBlocks") || properties.containsKey("matchTiles") || properties.containsKey(
                "ctmDisabled") || properties.containsKey("ctmTilesDisabled")
        )
        {
            // Acquires the path used for the Identifier
            int index = Arrays.stream(path.split("/")).toList().indexOf(zipFileOrFolder.getName()) + 1;
            StringBuilder tmpPath = new StringBuilder();
            String[] splitPath = path.split("/");

            for (int i = 0; i < splitPath.length - 1; ++i)
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
                                ctmPack.addAllBlocks(
                                        getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[0]),
                                        namespace
                                );
                            }
                        }
                    }
                    else // If no "file range" (ex: "0-4") is found for the textures to use, we use the first that comes
                    {
                        ctmPack.addAllBlocks(
                                getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]),
                                namespace
                        );
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
                                ctmPack.addAllBlocks(
                                        getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[0]),
                                        namespace
                                );
                            }
                        }
                    }
                    else
                    {
                        ctmPack.addAllBlocks(
                                getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]),
                                namespace
                        );
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
                                ctmPack.addAllBlocks(
                                        getCTMBlocksInProperties(properties, tmpPath.toString(), tiles[1]),
                                        namespace
                                );
                            }
                        }
                    }
                    else
                    {
                        ctmPack.addAllBlocks(
                                getCTMBlocksInProperties(properties, tmpPath.toString(), spacedTiles[0]),
                                namespace
                        );
                    }
                }
            }
        }
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

        ArrayList<String> path = new ArrayList<>(Arrays.stream(tmpPath.split("/")).toList());
        String namespace = path.remove(0);
        StringBuilder s = new StringBuilder();
        for (String str : path)
        {
            s.append(str);
            s.append("/");
        }
        tmpPath = s.toString();

        for (int i = 0; i < 4; ++i)
        {
            if (properties.containsKey(types[i]))
            {
                for (String block : properties.getProperty(types[i]).split(" "))
                {
                    ctmBlocks.add(new CTMBlock(
                            block,
                            new Identifier(namespace, "%s%s.png".formatted(tmpPath, startTile)),
                            types[i].equals("matchBlocks") || types[i].equals("matchTiles")
                    ));
                }
            }
        }
        return ctmBlocks;
    }

    private boolean zipNotACTMPack(@NotNull ZipFile zipFile) throws ZipException
    {
        for (FileHeader fileHeader : zipFile.getFileHeaders())
        {
            if (fileHeader.toString().startsWith("assets") && fileHeader.toString().contains(ctmPath))
            {
                return false;
            }
        }
        return true;
    }

    private boolean isFolderCtmPack(Path packPath)
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

    private boolean isPackModded(@NotNull ZipFile zipFile) throws ZipException
    {
        for (FileHeader fileHeader : zipFile.getFileHeaders())
        {
            String[] s = fileHeader.toString().split("/");
            // If s.length > 1, s[1] is the namespace
            if (s.length > 1 && !s[1].equals("minecraft"))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isPackModded(Path packPath)
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
                if (file.isDirectory() && !file.getName().equals("minecraft"))
                {
                    return true;
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
    private @NotNull ArrayList<Path> getFilesInFolderPack(@NotNull File packFolder)
    {
        ArrayList<Path> paths = new ArrayList<>();
        listFilesInFolderPack(packFolder, paths);
        return paths;
    }

    private void listFilesInFolderPack(@NotNull File packFolder, ArrayList<Path> paths)
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

    public void updateUsedTextures(@NotNull CTMPack ctmPack)
    {
        if (!ctmPack.isFolder())
        {
            String packPath = Path.of("%s/%s".formatted(RESOURCE_PACKS_DIR, ctmPack.getName())).toString();

            HashMap<String, byte[]> headersBytes = new HashMap<>();

            try (ZipFile zipFile = new ZipFile(packPath))
            {
                if (zipNotACTMPack(zipFile))
                {
                    zipFile.close();
                    return;
                }

                for (FileHeader fileHeader : zipFile.getFileHeaders())
                {
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

                        if (!properties.isEmpty())
                        {
                            // Loads the enabled and disabled options from the file
                            fillBlocksLists(properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                            // Toggles the options in the file
                            if (fileHeader.toString().contains(ctmPath))
                            {
                                boolean changed = updateProperties(
                                        ctmPack, properties, enabledBlocks, enabledTiles,
                                        disabledBlocks, disabledTiles
                                );

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
        else
        {
            boolean changed = false;

            // We use Path.of() to be sure that the path is correct, independently of the OS of the user
            ArrayList<Path> paths = getFilesInFolderPack(
                    new File(Path.of("%s/%s".formatted(RESOURCE_PACKS_DIR, ctmPack.getName())).toString())
            );
            for (Path path : paths)
            {
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

                if (!properties.isEmpty())
                {
                    // Loads the enabled and disabled options from the file
                    fillBlocksLists(properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                    // Toggles the options in the file
                    if (path.toString().replace("\\", "/").contains(ctmPath))
                    {
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
                                    removeEmptyKeys(properties); // TODO -> See if removing this fixes Continuity errors
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

            if (changed)
            {
                MinecraftClient.getInstance().reloadResources();
            }
        }
        load();
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
    private void fillBlocksLists(
            @NotNull Properties properties,
            ArrayList<String> enabledBlocks, ArrayList<String> enabledTiles,
            ArrayList<String> disabledBlocks, ArrayList<String> disabledTiles
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
        for (String type : types)
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
     * Updates the given properties object to match the options changed by the user
     *
     * @param ctmPack    The {@link CTMPack} object
     * @param properties The {@link Properties} instance to update
     * @return Whether a property was changed
     */
    private boolean updateProperties(
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
            for (String optionName : blocks.get(i))
            {
                if (!ctmPack.isBlockEnabled(optionName))
                {
                    if (properties.containsKey(types[i]))
                    {
                        changed = true;
                        ArrayList<String> blocksOrTiles =
                                new ArrayList<>(List.of(properties.getProperty(types[i]).split(" ")));
                        System.out.println(blocksOrTiles);
                        blocksOrTiles.remove(optionName);
                        properties.put(types[i], blocksOrTiles.toString()
                                .replace("[", "")
                                .replace("]", "")
                                .replace(",", "")
                        );

                        String opposite = types[i + 2];
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
            }
        }

        // DISABLED BLOCKS and TILES
        for (int i = 2; i < 4; ++i)
        {
            for (String optionName : blocks.get(i))
            {
                if (ctmPack.isBlockEnabled(optionName))
                {
                    if (properties.containsKey(types[i]))
                    {
                        changed = true;
                        ArrayList<String> currentType =
                                new ArrayList<>(List.of(properties.getProperty(types[i]).split(" ")));
                        currentType.remove(optionName);
                        properties.put(types[i], currentType.toString()
                                .replace("[", "")
                                .replace("]", "")
                                .replace(",", "")
                        );

                        String opposite = types[i - 2];
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
            }
        }
        return changed;
    }
}