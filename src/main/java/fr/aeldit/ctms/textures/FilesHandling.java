package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.gui.entryTypes.CTMBlock;
import fr.aeldit.ctms.gui.entryTypes.CTMPack;
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

import static fr.aeldit.ctms.textures.CTMSelector.*;
import static fr.aeldit.ctms.util.Utils.*;

public class FilesHandling
{
    private final String ctmPath = "assets/minecraft/optifine/ctm/";
    private final ArrayList<Path> folderPaths = new ArrayList<>();

    public void load() // TODO -> categories with file tree
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

                    boolean hasControls = hasZipPackControls(zipFile);

                    CTMPack ctmPack = new CTMPack(file.getName(), false, hasControls);
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
                    // in the
                    // Controls object
                    if (hasControls)
                    {
                        for (Controls controls : ctmPack.getCtmSelector().getControls())
                        {
                            for (Path path : controls.getPropertiesFilesPaths())
                            {
                                for (String blockName : getCTMBlocksNamesInProperties(path))
                                {
                                    CTMBlock ctmBlock = ctmPack.getCtmBlockByName(blockName);
                                    if (ctmBlock != null)
                                    {
                                        controls.addContainedBlock(ctmBlock);
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
            else if (file.isDirectory() && isFolderCtmPack(file.getName()))
            {
                boolean hasControls = hasFolderPackControls(file.toPath());

                CTMPack ctmPack = new CTMPack(file.getName(), true, hasControls);
                CTM_PACKS.add(ctmPack);

                for (Path path : listFilesInFolderPack(file))
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
                folderPaths.clear();

                // If the pack has a controls file, we add the already existing CTMBlock objects to the ArrayList in the
                // Controls object
                if (hasControls)
                {
                    for (Controls controls : ctmPack.getCtmSelector().getControls())
                    {
                        for (Path path : controls.getPropertiesFilesPaths())
                        {
                            for (String blockName : getCTMBlocksNamesInProperties(path))
                            {
                                CTMBlock ctmBlock = ctmPack.getCtmBlockByName(blockName);
                                if (ctmBlock != null)
                                {
                                    controls.addContainedBlock(ctmBlock);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadOptions(
            Properties properties, CTMPack ctmPack, @NotNull String path,
            @NotNull File zipFileOrFolder
    )
    {
        String namespace =
                path.split("/")[Arrays.stream(path.split("/")).toList().indexOf(zipFileOrFolder.getName()) + 2];

        if (namespace.equals("minecraft")
                && (properties.containsKey("matchBlocks")
                || properties.containsKey("matchTiles")
                || properties.containsKey("ctmDisabled")
                || properties.containsKey("ctmTilesDisabled"))
        )
        {
            // Acquires the path used for the Identifier
            int index = Arrays.stream(path.split("/")).toList().indexOf(zipFileOrFolder.getName()) + 2;
            StringBuilder tmpPath = new StringBuilder();
            String[] splitPath = path.split("/");

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
                                ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(),
                                        tiles[0]
                                ));
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
                                ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(),
                                        tiles[0]
                                ));
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
                                ctmPack.addAllBlocks(getCTMBlocksInProperties(properties, tmpPath.toString(),
                                        tiles[1]
                                ));
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

    private boolean isFolderCtmPack(String packName)
    {
        return Files.exists(Path.of(RESOURCE_PACKS_DIR + "/" + packName + "/" + ctmPath));
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
    private ArrayList<Path> listFilesInFolderPack(@NotNull File packFolder)
    {
        File[] files = packFolder.listFiles();
        if (files == null)
        {
            return folderPaths;
        }

        for (File file : files)
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
        if (!ctmPack.isFolder())
        {
            String packPath = Path.of(RESOURCE_PACKS_DIR + "/" + ctmPack.getName()).toString();

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
                        ArrayList<String> enabledBlocks = new ArrayList<>();
                        ArrayList<String> enabledTiles = new ArrayList<>();
                        ArrayList<String> disabledBlocks = new ArrayList<>();
                        ArrayList<String> disabledTiles = new ArrayList<>();

                        Properties properties = new Properties();
                        properties.load(zipFile.getInputStream(fileHeader));

                        if (!properties.isEmpty())
                        {
                            // Loads the enabled and disabled options from the file
                            fillBlocksLists(properties, enabledBlocks, enabledTiles, disabledBlocks, disabledTiles);

                            // Toggles the options in the file
                            if (fileHeader.toString().contains(ctmPath))
                            {
                                boolean changed = updateList(ctmPack, enabledBlocks, true, properties,
                                        "matchBlocks", "ctmDisabled"
                                )
                                        || updateList(ctmPack, enabledTiles, true, properties, "matchTiles",
                                        "ctmTilesDisabled"
                                )
                                        || updateList(ctmPack, disabledBlocks, false, properties, "ctmDisabled",
                                        "matchBlocks"
                                )
                                        || updateList(ctmPack, disabledTiles, false, properties,
                                        "ctmTilesDisabled", "matchTiles"
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
                MinecraftClient.getInstance().getResourcePackManager().disable("file/" + ctmPack.getName());
                MinecraftClient.getInstance().reloadResources();

                writeBytesToZip(packPath, headersBytes);

                    /*HashMap<String, String> env = new HashMap<>();
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
                    }*/

                MinecraftClient.getInstance().getResourcePackManager().enable("file/" + ctmPack.getName());
                MinecraftClient.getInstance().reloadResources();
            }
        }
        else
        {
            boolean changed = false;

            // We use Path.of() to be sure that the path is correct, independently of the OS of the user
            for (Path path :
                    listFilesInFolderPack(new File(Path.of(RESOURCE_PACKS_DIR + "/" + ctmPack.getName()).toString())))
            {
                ArrayList<String> enabledBlocks = new ArrayList<>();
                ArrayList<String> enabledTiles = new ArrayList<>();
                ArrayList<String> disabledBlocks = new ArrayList<>();
                ArrayList<String> disabledTiles = new ArrayList<>();

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
                            boolean localChanged = updateList(ctmPack, enabledBlocks, true, properties, "matchBlocks",
                                    "ctmDisabled"
                            )
                                    || updateList(ctmPack, enabledTiles, true, properties, "matchTiles",
                                    "ctmTilesDisabled"
                            )
                                    || updateList(ctmPack, disabledBlocks, false, properties, "ctmDisabled",
                                    "matchBlocks"
                            )
                                    || updateList(ctmPack, disabledBlocks, false, properties, "ctmTilesDisabled",
                                    "matchTiles"
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
                }
            }
            folderPaths.clear();

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

    /**
     * Updates the given properties object to match the options changed by the user
     *
     * @param ctmPack      The {@link CTMPack} object
     * @param blocks       The list containing the blocks changed by the user
     * @param negateOption Whether to negate the option or not (we negate when we want to disable a block)
     * @param properties   The {@link Properties} instance to update
     * @param toRemove     The properties in which we remove the block
     * @param toAdd        The properties in which we add the block
     * @return Whether a property was changed
     */
    private boolean updateList(
            CTMPack ctmPack, @NotNull ArrayList<String> blocks, boolean negateOption, @NotNull Properties properties,
            String toRemove, String toAdd
    )
    {
        boolean changed = false;

        for (String blockName : blocks)
        {
            // Before condition simplification : negateOption ? !ctmPack.isBlockEnabled(blockName) : ctmPack
            // .isBlockEnabled(blockName)
            if (negateOption != ctmPack.isBlockEnabled(blockName))
            {
                if (properties.containsKey(toRemove))
                {
                    ArrayList<String> fileBlocks =
                            new ArrayList<>(List.of(properties.getProperty(toRemove).split(" ")));
                    fileBlocks.remove(blockName);
                    properties.put(toRemove, fileBlocks.toString()
                            .replace("[", "")
                            .replace("]", "")
                            .replace(",", "")
                    );

                    if (properties.containsKey(toAdd))
                    {
                        properties.put(toAdd, properties.getProperty(toAdd) + " " + blockName);
                    }
                    else
                    {
                        properties.put(toAdd, blockName);
                    }

                    changed = true;
                }
            }
        }
        return changed;
    }
}
