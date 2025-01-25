package fr.aeldit.ctms.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.aeldit.ctms.Utils;
import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static fr.aeldit.ctms.Utils.RESOURCE_PACKS_DIR;
import static fr.aeldit.ctms.Utils.getPrettyString;

public class CTMSelector
{
    //=========================================================================
    // Static part
    //=========================================================================
    public static boolean hasCTMSelector(@NotNull Path packPath)
    {
        return Files.exists(Path.of("%s/ctm_selector.json".formatted(packPath)));
    }

    public static boolean hasCTMSelector(@NotNull ZipFile zipFile) throws ZipException
    {
        for (FileHeader fileHeader : zipFile.getFileHeaders())
        {
            if (fileHeader.toString().equals("ctm_selector.json"))
            {
                return true;
            }
        }
        return false;
    }

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

    //=========================================================================
    // Non-static part
    //=========================================================================
    private final ArrayList<Group> packGroups = new ArrayList<>();
    private final String packName, packPath;
    private final boolean isFolder;

    public CTMSelector(@NotNull String packName, boolean isFolder, boolean fromFile)
    {
        this.packName = packName;
        this.packPath = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);
        this.isFolder = isFolder;

        if (fromFile)
        {
            readFile();
        }
        else
        {
            getGroupsFromFolderTree();
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
        return packGroups.stream()
                         .filter(group -> group.getContainedBlocksList().contains(ctmBlock))
                         .findFirst()
                         .orElse(null);
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

        groups.keySet().stream().map(group -> new Group(
                "ctm", getPrettyString(group.substring(group.lastIndexOf("/") + 1).split("_")),
                null, groups.get(group).stream().map(File::toString)
                            .collect(Collectors.toCollection(ArrayList::new)), getIconPath(group), true,
                Path.of(packPath), null
        )).forEach(packGroups::add);
    }

    private String getIconPath(String strPath)
    {
        Path path = Path.of(strPath);
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
                        System.out.println(groups.get(groupName));
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

    private void readFile()
    {
        ArrayList<Group.SerializableGroup> serializableGroups = new ArrayList<>();

        if (isFolder)
        {
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
        }
        else
        {
            try (ZipFile zipFile = new ZipFile(packPath))
            {
                for (FileHeader fileHeader : zipFile.getFileHeaders())
                {
                    if (fileHeader.toString().endsWith("ctm_selector.json"))
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
        }

        // Adds the groups properly initialized to the packGroups array
        serializableGroups.stream()
                          .map(cr -> new Group(cr, isFolder ? Path.of(packPath) : null, isFolder ? null : packPath))
                          .forEach(packGroups::add);
    }

    //=========================================================================
    // Options handling
    //=========================================================================
    public void toggle(@NotNull Group group)
    {
        if (packGroups.contains(group))
        {
            group.toggle();
        }
    }

    public void resetOptions()
    {
        for (Group groups : packGroups)
        {
            groups.setEnabled(true);
        }
    }

    /**
     * Updates the 'enabled' field in the groups file
     */
    public void updateGroupsStates()
    {
        ArrayList<Group.SerializableGroup> serializableGroupToWrite = new ArrayList<>(packGroups.size());

        for (Group cr : packGroups)
        {
            for (CTMBlock ctmBlock : cr.getContainedBlocksList())
            {
                ctmBlock.setEnabled(cr.isEnabled());
            }
            serializableGroupToWrite.add(cr.getAsRecord());
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
}