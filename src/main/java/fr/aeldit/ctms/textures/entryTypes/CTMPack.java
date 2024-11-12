package fr.aeldit.ctms.textures.entryTypes;

import fr.aeldit.ctms.textures.CTMSelector;
import fr.aeldit.ctms.textures.Group;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static fr.aeldit.ctms.textures.CTMSelector.getCTMBlocksNamesInProperties;
import static fr.aeldit.ctms.textures.CTMSelector.getCTMBlocksNamesInZipProperties;

/**
 * Represents a CTM pack
 *
 * @apiNote {@link #name} holds the name of the associated resource pack
 * <ul>
 *      <li>if it is a zip file : {@code "MyPackName.zip"}</li>
 *      <li>if it is a folder : {@code "MyPackName"}</li>
 * </ul>
 * <p>
 * {@link #isFolder} holds whether the pack is a folder or a zip file
 * <p>
 * {@link #ctmSelector} holds the associated {@link CTMSelector} object
 * <p>
 * identifier holds the identifier (the texture) that is
 * displayed next to the pack name in the
 * {@link fr.aeldit.ctms.gui.CTMSScreen CTMSScreen}
 * <p>
 * {@link #vanillaOnlyCtmBlocks} contains the blocks when we only have vanilla blocks. Otherwise, this is null
 * <p>
 * {@link #namespacesBlocks} contains for each namespace an ArrayList containing all {@link CTMBlock}
 * object found in the pack. If there are no modded blocks, this is null
 * <p>
 * The second part contains methods to handle the activation /
 * deactivation of each {@code CTMBlock} in this pack
 */
public class CTMPack
{
    private final String name;
    private final boolean isFolder;
    private final CTMSelector ctmSelector;
    private final ArrayList<CTMBlock> vanillaOnlyCtmBlocks;
    private final HashMap<String, ArrayList<CTMBlock>> namespacesBlocks; // HashMap<namespace, blocks in the namespace>
    private static final String[] TYPES = {"matchBlocks", "matchTiles", "ctmDisabled", "ctmTilesDisabled"};
    private static final String CTM_PATH = "optifine/ctm/";

    /**
     * Folder pack initialization
     *
     * @param file The pack directory
     */
    public CTMPack(@NotNull File file)
    {
        this.name = file.getName();
        this.isFolder = true;

        boolean isModded = isPackModded(file);
        // We either use only the vanilla array, or the hashmap
        this.vanillaOnlyCtmBlocks = isModded ? null : new ArrayList<>();
        this.namespacesBlocks = isModded ? new HashMap<>() : null;

        loadBlocks(file);

        this.ctmSelector = new CTMSelector(name);
        addBlocksToGroups();
    }

    /**
     * Zip pack initialization
     *
     * @param zipFile The pack {@code ZipFile}
     */
    public CTMPack(@NotNull ZipFile zipFile)
    {
        this.name = zipFile.getFile().getName();
        this.isFolder = false;

        boolean isModded = isPackModded(zipFile);
        // We either use only the vanilla array, or the hashmap
        this.vanillaOnlyCtmBlocks = isModded ? null : new ArrayList<>();
        this.namespacesBlocks = isModded ? new HashMap<>() : null;

        loadBlocks(zipFile);

        this.ctmSelector = new CTMSelector(name, zipFile);
        addBlocksToGroups(zipFile);
    }

    public String getName()
    {
        return name;
    }

    public Text getNameAsText()
    {
        return isFolder ? Text.of(name + " (folder)") : Text.of(name);
    }

    public boolean isFolder()
    {
        return isFolder;
    }

    public CTMSelector getCtmSelector()
    {
        return ctmSelector;
    }

    public boolean isModded()
    {
        return vanillaOnlyCtmBlocks == null;
    }

    public ArrayList<CTMBlock> getAllCTMBlocks()
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            return vanillaOnlyCtmBlocks;
        }

        ArrayList<CTMBlock> blocks = new ArrayList<>();

        for (ArrayList<CTMBlock> ctmBlocks : namespacesBlocks.values())
        {
            blocks.addAll(ctmBlocks);
        }
        return blocks;
    }

    public ArrayList<CTMBlock> getCTMBlocksForNamespace(String namespace)
    {
        return namespacesBlocks.containsKey(namespace) ? namespacesBlocks.get(namespace) : new ArrayList<>(0);
    }

    public @Nullable CTMBlock getCTMBlockByName(String name)
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            for (CTMBlock ctmBlock : vanillaOnlyCtmBlocks)
            {
                if (ctmBlock.getBlockName().equals(name))
                {
                    return ctmBlock;
                }
            }
        }
        else
        {
            for (ArrayList<CTMBlock> ctmBlocks : namespacesBlocks.values())
            {
                for (CTMBlock ctmBlock : ctmBlocks)
                {
                    if (ctmBlock.getBlockName().equals(name))
                    {
                        return ctmBlock;
                    }
                }
            }
        }
        return null;
    }

    public ArrayList<String> getNamespaces()
    {
        return new ArrayList<>(namespacesBlocks.keySet());
    }

    public void addAllBlocks(@NotNull ArrayList<CTMBlock> ctmBlockList, String namespace)
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            vanillaOnlyCtmBlocks.addAll(ctmBlockList);
        }
        else
        {
            if (!namespacesBlocks.containsKey(namespace))
            {
                namespacesBlocks.put(namespace, new ArrayList<>(ctmBlockList.size()));
            }

            namespacesBlocks.get(namespace).addAll(ctmBlockList);
        }
    }

    //=======================================================================//
    //                                 Group                                 //
    //=======================================================================//
    public boolean isBlockDisabledFromGroup(CTMBlock ctmBlock)
    {
        Group group = ctmSelector.getGroupWithBlock(ctmBlock);
        return group != null && !group.isEnabled();
    }

    //=======================================================================//
    //                               CTMBlocks                               //
    //=======================================================================//
    public void toggle(CTMBlock block)
    {
        if (!isBlockDisabledFromGroup(block))
        {
            block.toggle();
        }
    }

    public void resetOptions()
    {
        if (vanillaOnlyCtmBlocks != null)
        {
            for (CTMBlock ctmBlock : vanillaOnlyCtmBlocks)
            {
                if (!isBlockDisabledFromGroup(ctmBlock))
                {
                    ctmBlock.setEnabled(true);
                }
            }
        }
        else
        {
            for (ArrayList<CTMBlock> ctmBlocks : namespacesBlocks.values())
            {
                for (CTMBlock ctmBlock : ctmBlocks)
                {
                    if (!isBlockDisabledFromGroup(ctmBlock))
                    {
                        ctmBlock.setEnabled(true);
                    }
                }
            }
        }
    }

    public boolean isBlockEnabled(String blockName)
    {
        CTMBlock ctmBlock = getCTMBlockByName(blockName);
        if (ctmBlock == null)
        {
            return true;
        }

        if (!isBlockDisabledFromGroup(ctmBlock))
        {
            return ctmBlock.isEnabled();
        }
        return false;
    }

    //================================================================================================================//
    //                                                 INITIALIZATION                                                 //
    //================================================================================================================//
    private boolean isPackModded(@NotNull File packFile)
    {
        Path filePath = Path.of("%s/assets".formatted(packFile.toPath()));
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

    private boolean isPackModded(@NotNull ZipFile zipFile)
    {
        try
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
        }
        catch (ZipException e)
        {
            throw new RuntimeException(e);
        }
        return false;
    }

    //================================================================================================================//
    //                                             BLOCKS INITIALIZATION                                              //
    //================================================================================================================//

    /**
     * Lists all the properties files found inside the given pack directory
     *
     * @param packFolder The directory of the pack
     * @return The list of properties files that was found | {@code null} if none was found
     */
    private @NotNull ArrayList<Path> listFilesInFolderPack(@NotNull File packFolder)
    {
        ArrayList<Path> paths = new ArrayList<>();

        Stack<File> dirsToSearch = new Stack<>();
        dirsToSearch.push(packFolder);

        while (!dirsToSearch.isEmpty())
        {
            File currentDir = dirsToSearch.pop();
            File[] files = currentDir.listFiles();
            if (files == null)
            {
                continue;
            }

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    dirsToSearch.push(file);
                }
                else if (file.isFile() && file.getName().endsWith(".properties"))
                {
                    paths.add(file.toPath());
                }
            }
        }
        return paths;
    }

    /**
     * Loads all the blocks found inside the properties files (themselves found inside the pack's directory)
     */
    private void loadBlocks(File packDir)
    {
        for (Path path : listFilesInFolderPack(packDir))
        {
            if (path.toFile().isFile() && path.toString().replace("\\", "/").contains(CTM_PATH))
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
                        loadBlocksFromProperties(properties, path.toString(), packDir.getName());
                    }
                }
            }
        }
    }

    private void loadBlocks(@NotNull ZipFile zipFile)
    {
        try
        {
            for (FileHeader fileHeader : zipFile.getFileHeaders())
            {
                if (fileHeader.toString().contains(CTM_PATH))
                {
                    if (fileHeader.toString().endsWith(".properties"))
                    {
                        Properties properties = new Properties();
                        properties.load(zipFile.getInputStream(fileHeader));

                        if (!properties.isEmpty())
                        {
                            loadBlocksFromProperties(properties, fileHeader.toString(), zipFile.getFile().getName());
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
     * For each of the 4 properties found in {@link #TYPES} ({@code "matchBlocks"}, {@code "matchTiles"}, {@code
     * "ctmDisabled"}, {@code "ctmTilesDisabled"}), add the blocks found to the list that will be returned.
     * <p>
     * Each of these blocks share the same identifier
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
        String namespace = path.removeFirst();
        StringBuilder s = new StringBuilder();
        for (String str : path)
        {
            s.append(str);
            s.append("/");
        }
        tmpPath = s.toString().replaceFirst(":", "/");

        Identifier identifier = new Identifier(namespace, "%s%s.png".formatted(tmpPath, startTile));

        for (int i = 0; i < 4; ++i)
        {
            String type = TYPES[i];
            boolean isEnabled = type.equals("matchBlocks") || type.equals("matchTiles");

            if (!properties.containsKey(type))
            {
                continue;
            }

            for (String blockName : properties.getProperty(type).split(" "))
            {
                ctmBlocks.add(new CTMBlock(blockName, identifier, isEnabled));
            }
        }
        return ctmBlocks;
    }

    private @NotNull String getPathToIdentifier(String @NotNull [] splitPath, int packNameIndex)
    {
        int index = packNameIndex + 1;
        StringBuilder pathToIdentifier = new StringBuilder();

        for (int i = 0; i < splitPath.length - 1; ++i)
        {
            if (i > index)
            {
                pathToIdentifier.append(splitPath[i]).append("/");
            }
        }
        return pathToIdentifier.toString();
    }

    /**
     * Adds all the blocks found in teh given properties to the pack ({@code this})
     *
     * @param spacedTiles      The tiles property split on spaces
     * @param tiles            The tiles property split on '-'
     * @param pathToIdentifier The path to the directory in which the identifier icon is located
     * @param namespace        The namespace for the blocks
     * @param range            The number of blocks used by the method - 1
     */
    private void addBlocksForRange(
            Properties properties, String @NotNull [] spacedTiles, String @NotNull [] tiles, String pathToIdentifier,
            String namespace, int range
    )
    {
        if (spacedTiles[0].contains("-"))
        {
            if (tiles.length == 2 && isDigits(tiles[0]) && isDigits(tiles[1]))
            {
                // If there are range+1 (0-range) textures => the texture when not connected is present,
                // so we use it (texture 0)
                if (Integer.parseInt(tiles[0]) + range == Integer.parseInt(tiles[1]))
                {
                    addAllBlocks(getCTMBlocksInProperties(properties, pathToIdentifier, tiles[0]), namespace);
                }
            }
        }
        else
        {
            addAllBlocks(getCTMBlocksInProperties(properties, pathToIdentifier, spacedTiles[0]), namespace);
        }
    }

    /**
     * Adds to the given {@code ctmPack} all the blocks found in the given properties, depending on the method
     */
    private void loadBlocksFromProperties(@NotNull Properties properties, @NotNull String path, String packFileName)
    {
        if (!(
                properties.containsKey("matchBlocks") || properties.containsKey("matchTiles")
                || properties.containsKey("ctmDisabled") || properties.containsKey("ctmTilesDisabled")
        ))
        {
            return;
        }

        String[] splitPath = path.split("/");
        int packNameIndex = List.of(splitPath).indexOf(packFileName);
        int namespaceIndex = packNameIndex + 2; // ".../packName/assets/namespace"
        if (namespaceIndex >= splitPath.length)
        {
            return;
        }

        String namespace = splitPath[namespaceIndex];
        String pathToIdentifier = getPathToIdentifier(splitPath, packNameIndex);

        if (properties.containsKey("method") && properties.containsKey("tiles"))
        {
            String method = properties.getProperty("method");
            String tilesProperty = properties.getProperty("tiles");
            if (method.isEmpty() || tilesProperty.isEmpty())
            {
                return;
            }

            String[] spacedTiles = tilesProperty.split(" ");
            String[] tiles = tilesProperty.split("-");

            switch (method)
            {
            case "ctm_compact" -> addBlocksForRange(properties, spacedTiles, tiles, pathToIdentifier, namespace, 4);

            case "ctm" -> addBlocksForRange(properties, spacedTiles, tiles, pathToIdentifier, namespace, 46);

            case "horizontal", "vertical", "horizontal+vertical", "vertical+horizontal" ->
                    addBlocksForRange(properties, spacedTiles, tiles, pathToIdentifier, namespace, 3);
            }
        }
    }

    //================================================================================================================//
    //                                              GROUPS INITIALIZATION                                             //
    //================================================================================================================//

    /**
     * For each group in this pack, add the {@link CTMBlock} instances of the blocks that they contain (the blocks were
     * initialized before, so we get them from the list of existing to not have duplicated {@link CTMBlock} instances)
     */
    private void addBlocksToGroups()
    {
        for (Group group : ctmSelector.getGroups())
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
                    CTMBlock ctmBlock = getCTMBlockByName(blockName);
                    if (ctmBlock != null)
                    {
                        group.addContainedBlock(ctmBlock);
                    }
                }
            }
        }
    }

    private void addBlocksToGroups(ZipFile zipFile)
    {
        for (Group group : ctmSelector.getGroups())
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
                    CTMBlock ctmBlock = getCTMBlockByName(blockName);
                    if (ctmBlock != null)
                    {
                        group.addContainedBlock(ctmBlock);
                    }
                }
            }
        }
    }
}