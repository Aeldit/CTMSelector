package fr.aeldit.ctms.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.aeldit.ctms.Utils;
import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static fr.aeldit.ctms.Utils.RESOURCE_PACKS_DIR;
import static fr.aeldit.ctms.Utils.getPrettyString;

public class CTMSelector
{
    private final ArrayList<Group> packGroups = new ArrayList<>();
    private final String packPath;
    private final boolean isFolder;

    public CTMSelector(@NotNull String packName, boolean fromFile)
    {
        this.packPath = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);
        this.isFolder = true;

        if (fromFile)
        {
            readFolder();
        }
        else
        {
            getGroupsFromFolderTree();
        }
    }

    public CTMSelector(@NotNull String packName, boolean fromFile, ZipFile zipFile)
    {
        this.packPath = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);
        this.isFolder = false;

        if (fromFile)
        {
            readZipFile(zipFile);
        }
        else
        {
            getGroupsFromZipTree(zipFile);
        }
    }

    public ArrayList<Group> getGroups()
    {
        return packGroups;
    }

    /**
     * @param ctmBlock The {@link CTMBlock} object
     * @return The group that contains the block | null otherwise
     */
    public @Nullable Group getGroupWithBlock(@NotNull CTMBlock ctmBlock)
    {
        for (Group group : packGroups)
        {
            if (group.getContainedBlocksList().contains(ctmBlock))
            {
                return group;
            }
        }
        return null;
    }

    private void getGroupsFromFolderTree()
    {
        Path assetsDir = Path.of("%s/assets/".formatted(packPath));
        if (!Files.exists(assetsDir))
        {
            return;
        }

        File[] namespaces = assetsDir.toFile().listFiles();
        if (namespaces == null)
        {
            return;
        }

        HashMap<String, ArrayList<File>> groups = new HashMap<>();

        for (File namespace : namespaces)
        {
            Path ctmDir = Path.of("%s/optifine/ctm".formatted(namespace));
            if (!Files.exists(ctmDir))
            {
                continue;
            }

            File[] ctmFiles = ctmDir.toFile().listFiles();
            if (ctmFiles == null)
            {
                continue;
            }

            for (File file : ctmFiles)
            {
                if (file.isDirectory())
                {
                    getGroupsInDir(file, groups);
                }
            }
        }

        for (String group : groups.keySet())
        {
            ArrayList<String> filesPaths = new ArrayList<>();
            groups.get(group).forEach(file -> filesPaths.add(file.toString()));

            packGroups.add(
                    new Group(
                            "ctm", getPrettyString(group.substring(group.lastIndexOf("/") + 1).split("_")),
                            null, filesPaths, getIconPath(group), true,
                            Path.of(packPath), null
                    )
            );
        }
    }

    // TODO -> Make work
    private void getGroupsFromZipTree(@NotNull ZipFile zipFile)
    {
        HashMap<String, ArrayList<FileHeader>> groups = new HashMap<>();

        try
        {
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            for (FileHeader fileHeader : fileHeaders)
            {
                String fh = fileHeader.getFileName();
                if (!fh.startsWith("assets/")
                    || StringUtils.countMatches(fh, "/") != 4
                    || !fh.contains("/optifine/ctm/")
                )
                {
                    continue;
                }

                {
                    getGroupsInZipDir(fileHeader, groups, fileHeaders);
                    System.out.println(fileHeader.getFileName());
                }
            }
        }
        catch (ZipException e)
        {
            throw new RuntimeException(e);
        }

        for (String group : groups.keySet())
        {
            ArrayList<String> filesPaths = new ArrayList<>();
            groups.get(group).forEach(file -> filesPaths.add(file.toString()));

            packGroups.add(
                    new Group(
                            "ctm", getPrettyString(group.substring(group.lastIndexOf("/") + 1).split("_")),
                            null, filesPaths, getIconPath(group), true,
                            null, packPath
                    )
            );
        }
    }

    /**
     * Searches every directory until an image file is found (.png extension only)
     *
     * @param fileStrPath The path to the file (as a string)
     * @return The path to the image file (as a string)
     */
    private String getIconPath(String fileStrPath)
    {
        Path path = Path.of(fileStrPath);
        if (!Files.exists(path))
        {
            return "";
        }

        Stack<File> searchingDirsStack = new Stack<>();
        searchingDirsStack.push(path.toFile());

        while (!searchingDirsStack.empty())
        {
            File currentFile = searchingDirsStack.pop();
            if (!Files.exists(currentFile.toPath()))
            {
                break;
            }

            File[] groupDir = currentFile.listFiles();
            if (groupDir == null)
            {
                break;
            }

            // The file 0.png is usually the block with all the borders, so we use this file if it exists
            for (File file : groupDir)
            {
                if (file.isFile() && file.toString().endsWith("0.png"))
                {
                    searchingDirsStack.clear();
                    return file.toString();
                }
            }

            for (File file : groupDir)
            {
                if (file.isFile() && file.getName().endsWith(".png"))
                {
                    searchingDirsStack.clear();
                    return file.toString();
                }

                if (file.isDirectory())
                {
                    searchingDirsStack.push(file);
                }
            }
        }
        searchingDirsStack.clear();
        return "";
    }

    private void getGroupsInDir(@NotNull File dir, HashMap<String, ArrayList<File>> groups)
    {
        Stack<File> searchingDirsStack = new Stack<>();
        searchingDirsStack.push(dir);

        while (!searchingDirsStack.empty())
        {
            File currentDir = searchingDirsStack.pop();

            File[] files = currentDir.listFiles();
            if (files == null)
            {
                return;
            }

            String groupName = currentDir.toString();
            if (!groups.containsKey(groupName))
            {
                groups.put(groupName, new ArrayList<>());
            }

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    if (containsPropertiesFiles(file))
                    {
                        getFilesInDirRec(file, groups.get(groupName));
                    }
                    else
                    {
                        searchingDirsStack.push(file);
                    }
                }
                else if (file.isFile() && file.getName().endsWith(".properties"))
                {
                    groups.get(groupName).add(file);
                }
            }
        }
    }

    private void getGroupsInZipDir(
            @NotNull FileHeader dir, HashMap<String, ArrayList<FileHeader>> groups,
            @NotNull List<FileHeader> fileHeaders
    )
    {
        Stack<FileHeader> searchingDirsStack = new Stack<>();
        searchingDirsStack.push(dir);

        while (!searchingDirsStack.isEmpty())
        {
            String groupName = searchingDirsStack.pop().getFileName();
            if (!groups.containsKey(groupName))
            {
                groups.put(groupName, new ArrayList<>());
            }

            for (FileHeader fileHeader : fileHeaders)
            {
                String fhStr = fileHeader.getFileName();
                if (!fhStr.startsWith(groupName) || fhStr.equals(groupName))
                {
                    continue;
                }

                if (fileHeader.isDirectory())
                {
                    if (zipDirContainsPropertiesFiles(fhStr, fileHeaders))
                    {
                        getFilesInZipDirRec(fhStr, groups.get(groupName), fileHeaders);
                    }
                    else
                    {
                        if (!searchingDirsStack.contains(fileHeader))
                        {
                            searchingDirsStack.push(fileHeader);
                        }
                    }
                }
                else
                {
                    if (fhStr.endsWith(".properties"))
                    {
                        groups.get(groupName).add(fileHeader);
                    }
                }
            }
        }
    }

    private boolean containsPropertiesFiles(@NotNull File dir)
    {
        File[] files = dir.listFiles();
        if (files == null)
        {
            return false;
        }

        for (File file : files)
        {
            if (file.isFile() && file.getName().endsWith(".properties"))
            {
                return true;
            }
        }
        return false;
    }

    private boolean zipDirContainsPropertiesFiles(@NotNull String dir, @NotNull List<FileHeader> fileHeaders)
    {
        int nbSlashes = StringUtils.countMatches(dir, "/");

        for (FileHeader fileHeader : fileHeaders)
        {
            String fhStr = fileHeader.getFileName();
            if (StringUtils.countMatches(fhStr, "/") != nbSlashes)
            {
                continue;
            }

            if (!fileHeader.isDirectory()
                && fileHeader.getFileName().startsWith(dir)
                && fileHeader.getFileName().endsWith(".properties")
            )
            {
                return true;
            }
        }
        return false;
    }

    private void getFilesInDirRec(@NotNull File dir, ArrayList<File> blocks)
    {
        Stack<File> searchingDirsStack = new Stack<>();
        searchingDirsStack.push(dir);

        while (!searchingDirsStack.empty())
        {
            File currentDir = searchingDirsStack.pop();
            File[] files = currentDir.listFiles();
            if (files == null)
            {
                return;
            }

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    searchingDirsStack.push(file);
                }
                else if (file.isFile() && file.getName().endsWith(".properties"))
                {
                    blocks.add(file);
                }
            }
        }
    }

    private void getFilesInZipDirRec(@NotNull String dir, ArrayList<FileHeader> blocks, List<FileHeader> fileHeaders)
    {
        Stack<String> searchingDirsStack = new Stack<>();
        searchingDirsStack.push(dir);

        while (!searchingDirsStack.empty())
        {
            String currentDir = searchingDirsStack.pop();

            int nbSlashes = StringUtils.countMatches(currentDir, "/");

            for (FileHeader fileHeader : fileHeaders)
            {
                // If the current dir doesn't have the same number of slashes as the dir popped from the stack
                String fhStr = fileHeader.getFileName();
                if (StringUtils.countMatches(fhStr, "/") != nbSlashes
                    || !fhStr.startsWith(currentDir) || fhStr.equals(currentDir)
                )
                {
                    continue;
                }

                if (fileHeader.isDirectory())
                {
                    searchingDirsStack.push(fhStr);
                }
                else
                {
                    if (fhStr.endsWith(".properties"))
                    {
                        blocks.add(fileHeader);
                    }
                }
            }
        }
    }

    private void readFolder()
    {
        ArrayList<Group.SerializableGroup> serializableGroups = new ArrayList<>();

        Path ctmSelectorPath = Path.of("%s/ctm_selector.json".formatted(packPath));

        if (Files.exists(ctmSelectorPath))
        {
            try
            {
                Gson gson = new Gson();
                Reader reader = Files.newBufferedReader(ctmSelectorPath);
                serializableGroups.addAll(Arrays.asList(gson.fromJson(
                        reader,
                        Group.SerializableGroup[].class
                )));
                reader.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        // Adds the groups properly initialized to the packGroups array
        for (Group.SerializableGroup cr : serializableGroups)
        {
            packGroups.add(new Group(cr, Path.of(packPath), null));
        }
    }

    private void readZipFile(@NotNull ZipFile zipFile)
    {
        ArrayList<Group.SerializableGroup> serializableGroups = new ArrayList<>();

        try
        {
            for (FileHeader fileHeader : zipFile.getFileHeaders())
            {
                if (fileHeader.getFileName().endsWith("ctm_selector.json"))
                {
                    Gson gson = new Gson();
                    Reader reader = new InputStreamReader(zipFile.getInputStream(fileHeader));
                    serializableGroups.addAll(Arrays.asList(gson.fromJson(
                            reader,
                            Group.SerializableGroup[].class
                    )));
                    reader.close();
                    break;
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // Adds the groups properly initialized to the packGroups array
        for (Group.SerializableGroup group : serializableGroups)
        {
            packGroups.add(new Group(group, null, packPath));
        }
    }

    //=========================================================================
    // Options handling
    //=========================================================================
    public void resetOptions()
    {
        for (Group group : packGroups)
        {
            group.setEnabled(true);
        }
    }

    /**
     * Updates the 'enabled' field in the groups file
     */
    public void updateGroupsStates()
    {
        ArrayList<Group.SerializableGroup> serializableGroupToWrite = new ArrayList<>(packGroups.size());

        for (Group group : packGroups)
        {
            for (CTMBlock ctmBlock : group.getContainedBlocksList())
            {
                ctmBlock.setEnabled(group.isEnabled());
            }
            serializableGroupToWrite.add(group.getAsRecord());
        }

        Path ctmSelectorPath = Path.of("%s/ctm_selector.json".formatted(packPath));

        if (isFolder)
        {
            try
            {
                Gson gsonWriter = new GsonBuilder().setPrettyPrinting().create();
                Writer writer = Files.newBufferedWriter(ctmSelectorPath);
                gsonWriter.toJson(serializableGroupToWrite, writer);
                writer.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            HashMap<String, byte[]> h = new HashMap<>(1);
            byte[] b = toByteArray(serializableGroupToWrite);
            h.put("ctm_selector.json", b);
            Utils.writeBytesToZip(Path.of(packPath).toString(), h);
        }
    }

    //=========================================================================
    // Static part
    //=========================================================================

    public static byte @NotNull [] toByteArray(@NotNull ArrayList<Group.SerializableGroup> groups)
    {
        ArrayList<String> s = new ArrayList<>(groups.size());
        for (Group.SerializableGroup sc : groups)
        {
            StringBuilder sbFiles = new StringBuilder();
            sbFiles.append("[\n");
            for (int i = 0; i < sc.propertiesFilesPaths().size(); ++i)
            {
                sbFiles.append(String.format("\t\t\t\"%s\"", sc.propertiesFilesPaths().get(i)));
                if (i != sc.propertiesFilesPaths().size() - 1)
                {
                    sbFiles.append(",\n");
                }
            }
            sbFiles.append("\n\t\t]");

            s.add(String.format(
                    """
                    \t{
                    \t\t"type": "%s",
                    \t\t"group_name": "%s",
                    \t\t"properties_files": %s,
                    \t\t"icon_path": "%s",
                    \t\t"enabled": %b,
                    \t\t"button_tooltip": "%s"
                    \t}""",
                    sc.type(), sc.groupName(), sbFiles, sc.iconPath(), sc.isEnabled(),
                    sc.buttonTooltip() == null ? "" : sc.buttonTooltip()
            ));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < s.size(); ++i)
        {
            sb.append(s.get(i));
            if (i != s.size() - 1)
            {
                sb.append(",\n");
            }
        }
        sb.append("\n]");
        return sb.toString().getBytes();
    }

    public static @Nullable ArrayList<String> getCTMBlocksNamesInProperties(@NotNull Path path)
    {
        Properties properties = new Properties();
        try
        {
            properties.load(new FileInputStream(String.valueOf(path)));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        if (properties.isEmpty())
        {
            return null;
        }

        ArrayList<String> ctmBlocks = new ArrayList<>(1);

        if (properties.containsKey("matchBlocks"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("matchBlocks").split(" ")));
        }
        else if (properties.containsKey("matchTiles"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("matchTiles").split(" ")));
        }

        if (properties.containsKey("ctmDisabled"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("ctmDisabled").split(" ")));
        }
        else if (properties.containsKey("ctmTilesDisabled"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("ctmTilesDisabled").split(" ")));
        }
        return ctmBlocks;
    }

    /**
     * For each of the following fields : matchBlocks, matchTiles, ctmDisabled and ctmTilesDisabled,
     * we add the blocks to the list that will be returned
     *
     * @param fileHeader The fileHeader of the properties file
     * @param zipFile    The ZipFile object
     * @return a list of all the CTMBlocks contained in the given properties file
     */
    public static @Nullable ArrayList<String> getCTMBlocksNamesInZipProperties(
            @NotNull FileHeader fileHeader, @NotNull ZipFile zipFile
    )
    {
        Properties properties = new Properties();
        try
        {
            properties.load(zipFile.getInputStream(fileHeader));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        if (properties.isEmpty())
        {
            return null;
        }

        ArrayList<String> ctmBlocks = new ArrayList<>(1);

        if (properties.containsKey("matchBlocks"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("matchBlocks").split(" ")));
        }
        else if (properties.containsKey("matchTiles"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("matchTiles").split(" ")));
        }

        if (properties.containsKey("ctmDisabled"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("ctmDisabled").split(" ")));
        }
        else if (properties.containsKey("ctmTilesDisabled"))
        {
            ctmBlocks.addAll(Arrays.asList(properties.getProperty("ctmTilesDisabled").split(" ")));
        }
        return ctmBlocks;
    }
}