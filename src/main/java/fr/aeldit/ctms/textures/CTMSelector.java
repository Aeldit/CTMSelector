package fr.aeldit.ctms.textures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.aeldit.ctms.Utils;
import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.lingala.zip4j.ZipFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static fr.aeldit.ctms.Utils.RESOURCE_PACKS_DIR;

public class CTMSelector
{
    private final List<Group> packGroups = new ArrayList<>();
    private final String packPath;
    private final boolean isFolder;

    public CTMSelector(@NotNull String packName, @NotNull CTMPack ctmPack)
    {
        this.packPath = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);
        this.isFolder = true;
        readFile();
        // For all blocks in the pack, add them to their respective group
        ctmPack.getCTMBlocks().forEach(ctmBlock -> packGroups.forEach(group -> group.addContainedBlock(ctmBlock)));
    }

    public CTMSelector(@NotNull String packName, ZipFile zipFile, @NotNull CTMPack ctmPack)
    {
        this.packPath = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);
        this.isFolder = false;
        readFile(zipFile);
        // For all blocks in the pack, add them to their respective group
        ctmPack.getCTMBlocks().forEach(ctmBlock -> packGroups.forEach(group -> group.addContainedBlock(ctmBlock)));
    }

    public List<Group> getGroups()
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
        Path ctmSelectorPath = Path.of("%s/ctm_selector.json".formatted(packPath));

        if (Files.exists(ctmSelectorPath))
        {
            try (Reader reader = Files.newBufferedReader(ctmSelectorPath))
            {
                this.packGroups.addAll(
                        Arrays.stream(new Gson().fromJson(reader, Group.SerializableGroup[].class))
                              .map(sg -> new Group(sg, true, packPath))
                              .toList()
                );
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void readFile(@NotNull ZipFile zipFile)
    {
        try (
                Reader reader = new InputStreamReader(
                        zipFile.getInputStream(zipFile.getFileHeader("ctm_selector.json")))
        ) // Throws if the fileHeader doesn't exist
        {
            // Adds the groups properly initialized to the packGroups array
            this.packGroups.addAll(
                    Arrays.stream(new Gson().fromJson(reader, Group.SerializableGroup[].class))
                          .map(sg -> new Group(sg, false, packPath))
                          .toList()
            );
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
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
        packGroups.forEach(grp -> grp.getContainedBlocksList().forEach(block -> block.setEnabled(grp.isEnabled())));
        List<Group.SerializableGroup> serializableGroupToWrite = packGroups.stream().map(Group::getAsRecord).toList();

        if (isFolder)
        {
            try (Writer writer = Files.newBufferedWriter(Path.of("%s/ctm_selector.json".formatted(packPath))))
            {
                new GsonBuilder().setPrettyPrinting().create().toJson(serializableGroupToWrite, writer);
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

    public static byte @NotNull [] toByteArray(@NotNull List<Group.SerializableGroup> groups)
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
                    \t\t"group_name": "%s",
                    \t\t"properties_files": %s,
                    \t\t"icon_path": "%s",
                    \t\t"enabled": %b,
                    \t\t"button_tooltip": "%s"
                    \t}""",
                    sc.groupName(), sbFiles, sc.iconPath(), sc.isEnabled(),
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

}