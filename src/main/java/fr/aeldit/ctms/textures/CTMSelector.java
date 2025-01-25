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
        return zipFile.getFileHeaders().stream()
                      .anyMatch(fileHeader -> fileHeader.toString().equals("ctm_selector.json"));
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
    private final String packPath;
    private final boolean isFolder;

    public CTMSelector(@NotNull String packName, boolean isFolder)
    {
        this.packPath = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);
        this.isFolder = isFolder;
        readFile();
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
                    try (Reader reader = Files.newBufferedReader(ctmSelectorPath))
                    {
                        serializableGroups.addAll(Arrays.asList(gson.fromJson(
                                reader,
                                Group.SerializableGroup[].class
                        )));
                    }
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
                        try (Reader reader = new InputStreamReader(zipFile.getInputStream(fileHeader)))
                        {
                            serializableGroups.addAll(Arrays.asList(gson.fromJson(
                                    reader,
                                    Group.SerializableGroup[].class
                            )));
                        }
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
        packGroups.forEach(groups -> groups.setEnabled(true));
    }

    /**
     * Updates the 'enabled' field in the groups file
     */
    public void updateGroupsStates()
    {
        ArrayList<Group.SerializableGroup> serializableGroupToWrite = new ArrayList<>(packGroups.size());
        packGroups.forEach(group -> {
            group.getContainedBlocksList().forEach(ctmBlock -> ctmBlock.setEnabled(group.isEnabled()));
            serializableGroupToWrite.add(group.getAsRecord());
        });

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