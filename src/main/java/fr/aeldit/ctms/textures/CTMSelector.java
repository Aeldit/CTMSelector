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

import static fr.aeldit.ctms.Utils.*;

public class CTMSelector
{
    private final String packPath;
    private final boolean isFolder;
    private final ArrayList<Group> packGroups = new ArrayList<>();

    public CTMSelector(@NotNull String packName)
    {
        this.packPath = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);
        this.isFolder = true;

        Path ctmSelectorPath = Path.of(this.packPath + "/ctm_selector.json");
        if (Files.exists(ctmSelectorPath))
        {
            getCTMSelectorFromFile(ctmSelectorPath);
        }
        else
        {
            getGroupsFromFolderTree();
        }
    }

    public CTMSelector(@NotNull String packName, @NotNull ZipFile zipFile)
    {
        this.packPath = "%s/%s".formatted(RESOURCE_PACKS_DIR, packName);
        this.isFolder = false;

        boolean hasCTMSelectorFile = false;
        try
        {
            for (FileHeader fileHeader : zipFile.getFileHeaders())
            {
                if (fileHeader.toString().equals("ctm_selector.json"))
                {
                    hasCTMSelectorFile = true;
                    break;
                }
            }
        }
        catch (ZipException e)
        {
            throw new RuntimeException(e);
        }

        if (hasCTMSelectorFile)
        {
            getCTMSelectorFromFile(zipFile);
        }
        else
        {
            getGroupsFromZipTree(zipFile);
        }
    }

    private void getCTMSelectorFromFile(Path ctmSelectorPath)
    {
        ArrayList<Group.SerializableGroup> serializableGroups = new ArrayList<>();
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(ctmSelectorPath))
        {
            serializableGroups.addAll(
                    Arrays.asList(
                            gson.fromJson(
                                    reader,
                                    Group.SerializableGroup[].class
                            )
                    )
            );
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        // Adds the groups properly initialized to the packGroups array
        for (Group.SerializableGroup cr : serializableGroups)
        {
            packGroups.add(new Group(cr, Path.of(packPath)));
        }
    }

    private void getCTMSelectorFromFile(@NotNull ZipFile zipFile)
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
                    serializableGroups.addAll(
                            Arrays.asList(
                                    gson.fromJson(
                                            reader,
                                            Group.SerializableGroup[].class
                                    )
                            )
                    );
                    System.out.println(serializableGroups);
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
            packGroups.add(new Group(group, zipFile));
        }
    }

    /**
     * @param dir                 The dir we want to check
     * @param currentGroupToAddTo The group we are adding to
     * @return whether the dir is inside a dir that is a group
     */
    private boolean isInGroupDir(@NotNull File dir, String currentGroupToAddTo)
    {
        for (String s : dir.toString().split("/"))
        {
            if (s.equals(currentGroupToAddTo))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isInGroupDir(@NotNull FileHeader dir, String currentGroupToAddTo)
    {
        for (String s : dir.toString().split("/"))
        {
            if (s.equals(currentGroupToAddTo))
            {
                return true;
            }
        }
        return false;
    }

    private @NotNull ArrayList<File> getAllPropertiesInDirRec(File dir)
    {
        ArrayList<File> propertiesFiles = new ArrayList<>();

        Stack<File> searchingDirsStack = new Stack<>();
        searchingDirsStack.push(dir);

        while (!searchingDirsStack.isEmpty())
        {
            File fileOrDir = searchingDirsStack.pop();
            File[] files = fileOrDir.listFiles();
            if (files == null)
            {
                continue;
            }

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    searchingDirsStack.push(file);
                }
                else if (file.isFile() && file.getName().endsWith(".properties"))
                {
                    propertiesFiles.add(file);
                }
            }
        }
        return propertiesFiles;
    }

    private @NotNull ArrayList<FileHeader> getAllPropertiesInDirRec(
            @NotNull FileHeader dir, @NotNull List<FileHeader> fileHeaders
    )
    {
        ArrayList<FileHeader> propertiesFiles = new ArrayList<>();
        String dirString = dir.toString();
        for (FileHeader fh : fileHeaders)
        {
            if (fh.toString().contains(dirString) && fh.toString().endsWith(".properties"))
            {
                propertiesFiles.add(fh);
            }
        }
        return propertiesFiles;
    }

    private void getGroupsInDir(@NotNull File dir, @NotNull HashMap<String, ArrayList<File>> groups)
    {
        Stack<File> searchingDirsStack = new Stack<>();
        searchingDirsStack.push(dir);

        String currentGroupToAddTo = dir.getName();
        if (!groups.containsKey(currentGroupToAddTo))
        {
            groups.put(currentGroupToAddTo, new ArrayList<>());
        }

        while (!searchingDirsStack.empty())
        {
            File fileOrDir = searchingDirsStack.pop();

            File[] files = fileOrDir.listFiles();
            if (files == null)
            {
                continue;
            }

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    if (isInGroupDir(file, currentGroupToAddTo))
                    {
                        groups.get(currentGroupToAddTo).addAll(getAllPropertiesInDirRec(file));
                        continue;
                    }

                    currentGroupToAddTo = file.getName();
                    if (!groups.containsKey(currentGroupToAddTo))
                    {
                        groups.put(currentGroupToAddTo, new ArrayList<>());
                    }
                    getAllPropertiesInDirRec(file);
                }
                else if (file.isFile() && file.getName().endsWith(".properties"))
                {
                    groups.get(currentGroupToAddTo).add(file);
                }
            }
        }
    }

    private void getGroupsInZipDir(
            @NotNull FileHeader dir, @NotNull HashMap<String, ArrayList<FileHeader>> groups,
            @NotNull List<FileHeader> fileHeaders
    )
    {
        Stack<FileHeader> searchingDirsStack = new Stack<>();
        searchingDirsStack.push(dir);

        String currentGroupToAddTo = getLastDirForFileHeader(dir);
        if (!groups.containsKey(currentGroupToAddTo))
        {
            groups.put(currentGroupToAddTo, new ArrayList<>());
        }

        while (!searchingDirsStack.isEmpty())
        {
            String fileOrDir = searchingDirsStack.pop().getFileName();

            // For all fileHeaders inside fileOrDir
            for (FileHeader fh : fileHeaders)
            {
                String fhStr = fh.getFileName();
                if (fhStr.equals(fileOrDir) || !fhStr.startsWith(dir.toString()))
                {
                    continue;
                }

                if (fh.isDirectory())
                {
                    if (isInGroupDir(fh, currentGroupToAddTo))
                    {
                        groups.get(currentGroupToAddTo).addAll(getAllPropertiesInDirRec(fh, fileHeaders));
                        continue;
                    }

                    currentGroupToAddTo = getLastDirForFileHeader(fh);
                    if (!groups.containsKey(currentGroupToAddTo))
                    {
                        groups.put(currentGroupToAddTo, new ArrayList<>());
                    }
                    getAllPropertiesInDirRec(fh, fileHeaders);
                }
                else if (!fh.isDirectory() && fh.getFileName().endsWith(".properties"))
                {
                    groups.get(currentGroupToAddTo).add(fh);
                }
            }
        }
    }

    /**
     * @param dirFileHeader The dir to check for
     * @param fileHeaders   THe list of all fileHeaders in the Zip file
     * @return An ArrayList containing all the fileHeaders that are in the current one
     */
    private @NotNull ArrayList<FileHeader> getCTMFileHeadersInDir(
            @NotNull FileHeader dirFileHeader, @NotNull List<FileHeader> fileHeaders
    )
    {
        ArrayList<FileHeader> fileHeadersInDir = new ArrayList<>();
        String dirFileHeaderString = dirFileHeader.toString();
        for (FileHeader fh : fileHeaders)
        {
            String fhStr = fh.toString();
            if (fhStr.startsWith(dirFileHeaderString)
                && fhStr.contains(CTM_PATH)
                && StringUtils.countMatches(fhStr, "/") == 5
                && fhStr.endsWith("/")
            )
            {
                fileHeadersInDir.add(fh);
            }
        }
        return fileHeadersInDir;
    }

    private @Nullable String getLastDirForFileHeader(@NotNull FileHeader fh)
    {
        String[] fhStr = fh.toString().split("/");
        return fhStr.length == 0 ? null : fhStr[fhStr.length - 1];
    }

    private boolean subDirContainsProperties(@NotNull File dir)
    {
        File[] files = dir.listFiles();
        if (files == null)
        {
            return false;
        }

        for (File file : files)
        {
            if (file.isDirectory())
            {
                File[] subDirFiles = file.listFiles();
                if (subDirFiles == null)
                {
                    continue;
                }

                for (File subDirFile : subDirFiles)
                {
                    if (subDirFile.isFile() && subDirFile.getName().endsWith(".properties"))
                    {
                        return true;
                    }
                }
            }
            else if (file.isFile() && file.getName().endsWith(".properties"))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Searches every directory until the 0.png image file is found
     *
     * @param propertiesFilesPaths The path to the files ArrayList
     * @return The path to the image file (as a string)
     */
    private @NotNull String getIconPath(@NotNull ArrayList<File> propertiesFilesPaths)
    {
        for (File propertiesFile : propertiesFilesPaths)
        {
            String fileStr = propertiesFile.toString();
            String fileName = fileStr.split("/")[fileStr.split("/").length - 1];
            Path dir = Path.of(fileStr.replace(fileName, ""));
            if (!Files.exists(dir))
            {
                continue;
            }

            File[] files = dir.toFile().listFiles();
            if (files == null)
            {
                continue;
            }

            for (File file : files)
            {
                if (file.isFile() && file.getName().equals("0.png"))
                {
                    String[] splitOnSlashes = file.toString().split("/");
                    int assetsIndex = List.of(splitOnSlashes).lastIndexOf("assets");
                    if (assetsIndex == -1 || splitOnSlashes.length < assetsIndex + 3)
                    {
                        break;
                    }
                    return "%s:%s".formatted(
                            splitOnSlashes[assetsIndex + 1], // namespace is at index 'assetsIndex + 1'
                            StringUtils.join(splitOnSlashes, "/", assetsIndex + 2, splitOnSlashes.length)
                    );
                }
            }
        }

        // If no 0.png image was found, we search for any .png file
        for (File propertiesFile : propertiesFilesPaths)
        {
            String fileStr = propertiesFile.toString();
            String fileName = fileStr.split("/")[fileStr.split("/").length - 1];
            Path dir = Path.of(fileStr.replace(fileName, ""));
            if (!Files.exists(dir))
            {
                continue;
            }

            File[] files = dir.toFile().listFiles();
            if (files == null)
            {
                continue;
            }

            for (File file : files)
            {
                if (file.isFile() && file.getName().endsWith(".png"))
                {
                    String[] splitOnSlashes = file.toString().split("/");
                    int assetsIndex = List.of(splitOnSlashes).lastIndexOf("assets");
                    if (assetsIndex == -1 || splitOnSlashes.length < assetsIndex + 3)
                    {
                        break;
                    }
                    return "%s:%s".formatted(
                            splitOnSlashes[assetsIndex + 1], // namespace is at index 'assetsIndex + 1'
                            StringUtils.join(splitOnSlashes, "/", assetsIndex + 2, splitOnSlashes.length)
                    );
                }
            }
        }
        return "";
    }

    private @NotNull String getIconPathZip(
            @NotNull ArrayList<String> propertiesFilesPaths, List<FileHeader> fileHeaders
    )
    {
        for (String propertiesFilePath : propertiesFilesPaths)
        {
            String fileName = propertiesFilePath.split("/")[propertiesFilePath.split("/").length - 1];
            String fhDir = propertiesFilePath.replace(fileName, "");

            for (FileHeader fh : fileHeaders)
            {
                if (fh.toString().startsWith(fhDir) && !fh.isDirectory())
                {
                    String fhFileName = getLastDirForFileHeader(fh);
                    if (fhFileName != null && fhFileName.equals("0.png"))
                    {
                        String[] splitOnSlashes = fh.toString().split("/");
                        if (splitOnSlashes.length < 3 || !splitOnSlashes[0].equals("assets"))
                        {
                            break;
                        }
                        return "%s:%s".formatted(
                                splitOnSlashes[1], // namespace is at index 1
                                StringUtils.join(splitOnSlashes, "/", 2, splitOnSlashes.length - 1)
                        );
                    }
                }
            }
        }

        // If no 0.png image was found, we search for any .png file
        for (String propertiesFilePath : propertiesFilesPaths)
        {
            String fileName = propertiesFilePath.split("/")[propertiesFilePath.split("/").length - 1];
            String fhDir = propertiesFilePath.replace(fileName, "");

            for (FileHeader fh : fileHeaders)
            {
                if (fh.toString().startsWith(fhDir) && !fh.isDirectory())
                {
                    String fhFileName = getLastDirForFileHeader(fh);
                    if (fhFileName != null && fhFileName.endsWith(".png"))
                    {
                        String[] splitOnSlashes = fh.toString().split("/");
                        if (splitOnSlashes.length < 3 || !splitOnSlashes[0].equals("assets"))
                        {
                            break;
                        }
                        return "%s:%s".formatted(
                                splitOnSlashes[1], // namespace is at index 1
                                StringUtils.join(splitOnSlashes, "/", 2, splitOnSlashes.length - 1)
                        );
                    }
                }
            }
        }
        return "";
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

            File[] files = ctmDir.toFile().listFiles();
            if (files == null)
            {
                continue;
            }

            for (File fileOrDir : files)
            {
                if (fileOrDir.isDirectory() && subDirContainsProperties(fileOrDir))
                {
                    getGroupsInDir(fileOrDir, groups);
                }
            }
        }

        for (String group : groups.keySet())
        {
            ArrayList<String> propertiesFilesPaths = new ArrayList<>();

            // Transform the fileHeaders to make them in the form 'namespace:path/to/file.properties'
            for (File file : groups.get(group))
            {
                String[] split = file.toString().split("/");
                int assetsIndex = List.of(split).indexOf("assets");
                if (assetsIndex == -1)
                {
                    continue;
                }

                if (split.length < assetsIndex + 3)
                {
                    continue;
                }

                propertiesFilesPaths.add("%s:%s".formatted(
                        split[assetsIndex + 1],
                        StringUtils.join(split, "/", assetsIndex + 2, split.length - 1)
                ));
            }

            packGroups.add(new Group(
                    "ctm",
                    getPrettyString(group.split("_")),
                    null,
                    propertiesFilesPaths,
                    getIconPath(groups.get(group)),
                    true,
                    Path.of(packPath)
            ));
        }
    }

    private void getGroupsFromZipTree(@NotNull ZipFile zipFile)
    {
        HashMap<String, ArrayList<FileHeader>> groups = new HashMap<>();

        try
        {
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            for (FileHeader namespaceFileHeader : fileHeaders)
            {
                String fhStr = namespaceFileHeader.toString();
                if (!fhStr.startsWith("assets/"))
                {
                    continue;
                }

                // If we are not on a namespace, we continue
                if (StringUtils.countMatches(fhStr, "/") != 2)
                {
                    continue;
                }

                ArrayList<FileHeader> fileHeadersInNamespace = getCTMFileHeadersInDir(namespaceFileHeader, fileHeaders);
                for (FileHeader fileHeader : fileHeadersInNamespace)
                {
                    getGroupsInZipDir(fileHeader, groups, fileHeaders);
                }
            }

            for (String group : groups.keySet())
            {
                ArrayList<String> filesPaths = new ArrayList<>();

                // Transform the fileHeaders to make them in the form 'namespace:path/to/file.properties'
                for (FileHeader fh : groups.get(group))
                {
                    String[] split = fh.toString().split("/");
                    if (split.length < 3 || !split[0].equals("assets"))
                    {
                        continue;
                    }

                    filesPaths.add("%s:%s".formatted(
                            split[1],
                            StringUtils.join(split, "/", 2, split.length - 1)
                    ));
                }

                packGroups.add(new Group(
                        "ctm",
                        getPrettyString(group.split("_")),
                        null,
                        filesPaths,
                        getIconPathZip(filesPaths, fileHeaders),
                        true,
                        zipFile
                ));
            }
        }
        catch (ZipException e)
        {
            throw new RuntimeException(e);
        }
    }

    // OLD

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

    //=========================================================================
    // Options handling
    //=========================================================================
    public void resetOptions()
    {
        for (Group group : packGroups)
        {
            group.isEnabled = true;
        }
    }

    /**
     * Looks at each group's state (enabled/disabled) and toggles all the blocks contained by each group
     */
    public void updateGroupsStates()
    {
        ArrayList<Group.SerializableGroup> serializableGroupToWrite = new ArrayList<>(packGroups.size());

        for (Group group : packGroups)
        {
            boolean isEnabled = group.isEnabled;
            for (CTMBlock ctmBlock : group.getContainedBlocksList())
            {
                ctmBlock.setEnabled(isEnabled);
            }
            serializableGroupToWrite.add(group.getAsRecord());
        }


        if (isFolder)
        {
            Path ctmSelectorPath = Path.of("%s/ctm_selector.json".formatted(packPath));
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

            s.add("""
                  \t{
                  \t\t"type": "%s",
                  \t\t"group_name": "%s",
                  \t\t"properties_files": %s,
                  \t\t"icon_path": "%s",
                  \t\t"enabled": %b,
                  \t\t"button_tooltip": "%s"
                  \t}""".formatted(
                          sc.type(), sc.groupName(), sbFiles.toString(), sc.iconPath(), sc.isEnabled(),
                          sc.buttonTooltip() == null ? "" : sc.buttonTooltip()
                  )
            );
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