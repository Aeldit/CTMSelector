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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

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
    private final String packName;
    private final boolean isFolder;

    public CTMSelector(@NotNull String packName, boolean isFolder, boolean fromFile)
    {
        this.packName = packName;
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
        for (Group group : packGroups)
        {
            for (CTMBlock block : group.getContainedBlocksList())
            {
                if (block == ctmBlock)
                {
                    return group;
                }
            }
        }
        return null;
    }

    private void getGroupsFromFolderTree()
    {
        Path assetsDir = Path.of("%s/%s/assets/".formatted(RESOURCE_PACKS_DIR, packName));
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

            // TODO -> Check if the pack is a folder or a Zip file
            packGroups.add(
                    new Group(
                            "ctm", getPrettyString(group.substring(group.lastIndexOf("/") + 1).split("_")),
                            null, filesPaths, getIconPath(group), true,
                            Path.of(assetsDir.toString().replace("assets/", "")), null
                    )
            );
        }
    }

    private String getIconPath(String strPath)
    {
        Path path = Path.of(strPath);
        if (!Files.exists(path))
        {
            return "";
        }

        File[] groupDir = path.toFile().listFiles();
        if (groupDir == null)
        {
            return "";
        }

        for (File file : groupDir)
        {
            if (file.isFile() && file.getName().endsWith(".png"))
            {
                return file.toString();
            }
            else if (file.isDirectory())
            {
                return getIconPath(file.toString());
            }
        }
        return "";
    }

    private void getGroupsInDir(@NotNull File dir, HashMap<String, ArrayList<File>> groups)
    {
        File[] files = dir.listFiles();
        if (files == null)
        {
            return;
        }

        String groupName = dir.toString();
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
                    getGroupsInDir(file, groups);
                }
            }
            else if (file.isFile() && file.getName().endsWith(".properties"))
            {
                groups.get(groupName).add(file);
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
        File[] files = dir.listFiles();
        if (files == null)
        {
            return;
        }

        for (File file : files)
        {
            if (file.isDirectory())
            {
                getFilesInDirRec(file, blocks);
            }
            else if (file.isFile() && file.getName().endsWith(".properties"))
            {
                blocks.add(file);
            }
        }
    }

    private void readFile()
    {
        ArrayList<Group.SerializableGroup> serializableGroups = new ArrayList<>();
        String packPathString = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);

        if (isFolder)
        {
            Path ctmSelectorPath = Path.of("%s/ctm_selector.json".formatted(packPathString));

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
            try (ZipFile zipFile = new ZipFile(packPathString))
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
        for (Group.SerializableGroup cr : serializableGroups)
        {
            packGroups.add(
                    new Group(
                            cr.type(), cr.groupName(), cr.buttonTooltip(),
                            cr.propertiesFilesPaths(), cr.iconPath(), cr.isEnabled(),
                            isFolder ? Path.of(packPathString) : null,
                            isFolder ? null : packPathString
                    )
            );
        }
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

        Path ctmSelectorPath = Path.of("%s/%s/ctm_selector.json".formatted(RESOURCE_PACKS_DIR, packName));

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
            Utils.writeBytesToZip(Path.of("%s/%s".formatted(RESOURCE_PACKS_DIR, packName)).toString(), h);
        }
    }
}
