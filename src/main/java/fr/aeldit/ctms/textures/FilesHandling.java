package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static fr.aeldit.ctms.Utils.*;
import static fr.aeldit.ctms.VersionUtils.getIdentifier;

public class FilesHandling
{
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
            if (file.isDirectory())
            {
                CTMPack ctmPack = new CTMPack(file, getAllBlocksInPack(file));
                CTM_PACKS.add(ctmPack);
            }
            else if (file.isFile() && file.getName().endsWith(".zip"))
            {
                try (ZipFile zipFile = new ZipFile(file))
                {
                    CTMPack ctmPack = new CTMPack(zipFile, getAllBlocksInPack(zipFile));
                    CTM_PACKS.add(ctmPack);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //****************************************************************************************************************//
    //**                                             BLOCKS INITIALISATION                                          **//
    //****************************************************************************************************************//
    private @NotNull HashMap<String, List<CTMBlock>> getAllBlocksInPack(@NotNull File file)
    {
        File[] rootFiles = file.listFiles();
        if (rootFiles == null)
        {
            return new HashMap<>();
        }

        for (File rootFile : rootFiles)
        {
            if (!"assets".equals(rootFile.getName()))
            {
                continue;
            }

            File[] assetsFiles = rootFile.listFiles();
            if (assetsFiles == null)
            {
                return new HashMap<>();
            }

            HashMap<String, List<CTMBlock>> allBlocks = new HashMap<>();
            for (File namespaceDir : assetsFiles)
            {
                if (namespaceDir.isDirectory())
                {
                    allBlocks.put(namespaceDir.getName(), getCTMBlocksForNamespace(namespaceDir));
                }
            }
            return allBlocks;
        }
        return new HashMap<>();
    }

    private @NotNull HashMap<String, List<CTMBlock>> getAllBlocksInPack(@NotNull ZipFile zipFile) throws IOException
    {
        HashMap<String, List<CTMBlock>> namespaceBlocks = new HashMap<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();

            String fhStr = entry.toString();
            if (!fhStr.contains("optifine/ctm/"))
            {
                continue;
            }

            if (fhStr.chars().filter(c -> c == '/').count() > 2) // ex: assets/minecraft/block_dir
            {
                String namespace = fhStr.split("/")[1];
                if (!namespaceBlocks.containsKey(namespace))
                {
                    namespaceBlocks.put(namespace, new ArrayList<>());
                }

                if (!fhStr.endsWith(".properties"))
                {
                    continue;
                }

                Properties props = new Properties();
                props.load(zipFile.getInputStream(entry));

                if (props.isEmpty())
                {
                    continue;
                }

                List<CTMBlock> ctmBlocks = namespaceBlocks.get(namespace);
                Identifier identifier = getIdentifierFor(props, zipFile, getParentFileHeader(fhStr), namespace);
                if (props.containsKey(types[0]))
                {
                    ctmBlocks.add(new CTMBlock(props.get(types[0]).toString(), identifier, true, false, fhStr));
                }
                else if (props.containsKey(types[1]))
                {
                    ctmBlocks.add(new CTMBlock(props.get(types[1]).toString(), identifier, true, true, fhStr));
                }
                else if (props.containsKey(types[2]))
                {
                    ctmBlocks.add(new CTMBlock(props.get(types[2]).toString(), identifier, false, false, fhStr));
                }
                else if (props.containsKey(types[3]))
                {
                    ctmBlocks.add(new CTMBlock(props.get(types[3]).toString(), identifier, false, true, fhStr));
                }
            }
        }
        return namespaceBlocks;
    }

    private @NotNull List<CTMBlock> getCTMBlocksForNamespace(File namespaceDir)
    {
        List<CTMBlock> ctmBlocks = new ArrayList<>();

        Stack<File> filesStack = new Stack<>();
        filesStack.push(namespaceDir);

        String namespace = namespaceDir.getName();

        while (!filesStack.empty())
        {
            File fileOrDir = filesStack.pop();
            File[] files = fileOrDir.listFiles();
            if (files == null)
            {
                continue;
            }

            for (File file : files)
            {
                if (file.isDirectory())
                {
                    filesStack.push(file);
                }
                else if (file.isFile() && file.getName().endsWith(".properties"))
                {
                    Properties properties = new Properties();
                    try (FileInputStream fileInputStream = new FileInputStream(file))
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

                    ctmBlocks.add(getCTMBlockFrom(properties, file.getParentFile(), namespace, file.toString()));
                }
            }
        }
        return ctmBlocks;
    }

    private @Nullable CTMBlock getCTMBlockFrom(
            @NotNull Properties properties, @NotNull File parentFile, String namespace, String filePath
    )
    {
        Identifier identifier = getIdentifierFor(properties, parentFile, namespace);

        if (properties.containsKey(types[0]))
        {
            return new CTMBlock(properties.get(types[0]).toString(), identifier, true, false, filePath);
        }
        if (properties.containsKey(types[1]))
        {
            return new CTMBlock(properties.get(types[1]).toString(), identifier, true, true, filePath);
        }
        if (properties.containsKey(types[2]))
        {
            return new CTMBlock(properties.get(types[2]).toString(), identifier, false, false, filePath);
        }
        if (properties.containsKey(types[3]))
        {
            return new CTMBlock(properties.get(types[3]).toString(), identifier, false, true, filePath);
        }
        return null;
    }

    private Identifier getIdentifierFor(Properties properties, @NotNull File parentFile, String namespace)
    {
        File[] neighborFiles = parentFile.listFiles();
        Identifier identifier = getIdentifier("unknown");
        if (neighborFiles != null)
        {
            int firstImage = Integer.parseInt(properties.get("tiles").toString().split("-")[0]);
            String pngFile = "%d.png".formatted(firstImage);
            if (Arrays.stream(neighborFiles).anyMatch(file -> pngFile.equals(file.getName())))
            {
                identifier = Arrays.stream(neighborFiles)
                                   .filter(file -> pngFile.equals(file.getName()))
                                   .findFirst()
                                   .map(file -> getIdentifier(
                                           namespace,
                                           getIdentifierLikePathFrom(namespace, file.getPath())
                                   ))
                                   .orElse(identifier);
            }
        }
        return identifier;
    }

    @Contract("_, _, _, _ -> new")
    private @NotNull Identifier getIdentifierFor(
            @NotNull Properties properties, @NotNull ZipFile zipFile, String parentFh, String namespace
    )
    {
        int firstImage = Integer.parseInt(properties.get("tiles").toString().split("-")[0]);
        String pngFile = "%d.png".formatted(firstImage);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry entry = entries.nextElement();
            if (entry.toString().contains("assets/%s/%s%s".formatted(namespace, parentFh, pngFile)))
            {
                return getIdentifier(namespace, "%s%s".formatted(parentFh, pngFile));
            }
        }
        return getIdentifier("unknown");
    }

    private @NotNull String getIdentifierLikePathFrom(String namespace, @NotNull String path)
    {
        List<String> split = new ArrayList<>(List.of(path.split("/")));
        StringBuilder sb = new StringBuilder();
        int splitSize = split.size();
        for (int i = split.lastIndexOf(namespace) + 1; i < splitSize; ++i)
        {
            sb.append(split.get(i));
            if (i != splitSize - 1)
            {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    private @NotNull String getParentFileHeader(@NotNull String fhStr)
    {
        StringBuilder sb = new StringBuilder();
        String[] split = fhStr.split("/");
        if (split.length < 2)
        {
            return "";
        }

        for (int i = 2; i < split.length - 1; i++)
        {
            sb.append(split[i]).append("/");
        }
        return sb.toString();
    }

    //****************************************************************************************************************//
    //**                                                    UPDATES                                                 **//
    //****************************************************************************************************************//
    public void updatePropertiesFiles(@NotNull CTMPack ctmPack)
    {
        Path packPath = Path.of(
                "%s/resourcepacks/%s".formatted(FabricLoader.getInstance().getGameDir(), ctmPack.getName()));
        if (!Files.exists(packPath))
        {
            return;
        }

        boolean changed = false;

        if (ctmPack.isFolder)
        {
            for (CTMBlock ctmBlock : ctmPack.getCTMBlocks())
            {
                Properties properties = new Properties();
                File file = Path.of(ctmBlock.propertiesPath).toFile();
                try (FileInputStream fis = new FileInputStream(file))
                {
                    properties.load(fis);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                // If the state of the block in the file is not the same as in the memory, we write the state of the one
                // in memory to the file
                if (ctmBlock.isEnabled() != isBlockEnabled(properties, ctmBlock.blockName))
                {
                    changed = true;
                    setBlockInPropertiesToAppropriateState(properties, ctmBlock);
                }

                try (FileOutputStream fos = new FileOutputStream(file))
                {
                    removeEmptyKeys(properties);
                    properties.store(fos, null);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            if (changed)
            {
                MinecraftClient.getInstance().reloadResources();
            }
        }
        else
        {
            HashMap<String, byte[]> headersBytes = new HashMap<>();

            try (ZipFile zipFile = new ZipFile(packPath.toString()))
            {
                for (CTMBlock ctmBlock : ctmPack.getCTMBlocks())
                {
                    ZipEntry entry = zipFile.getEntry(ctmBlock.propertiesPath);
                    if (entry == null)
                    {
                        continue;
                    }

                    Properties properties = new Properties();
                    properties.load(zipFile.getInputStream(entry));

                    // If the state of the block in the file is not the same as in the memory, we write the state of
                    // the one in memory to the file
                    if (ctmBlock.isEnabled() != isBlockEnabled(properties, ctmBlock.blockName))
                    {
                        changed = true;
                        setBlockInPropertiesToAppropriateState(properties, ctmBlock);
                    }

                    if (changed)
                    {
                        removeEmptyKeys(properties);
                        // We take the properties in a byte array, so we can write it in the zip later
                        byte[] tmp = properties.toString()
                                               .replace("{", "")
                                               .replace("}", "")
                                               .replace(", ", "\n")
                                               .getBytes();
                        headersBytes.put(entry.toString(), tmp);
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            if (!headersBytes.isEmpty())
            {
                MinecraftClient.getInstance().getResourcePackManager().disable("file/%s".formatted(ctmPack.getName()));
                MinecraftClient.getInstance().reloadResources();

                writeBytesToZip(packPath.toString(), headersBytes);

                MinecraftClient.getInstance().getResourcePackManager().enable("file/%s".formatted(ctmPack.getName()));
                MinecraftClient.getInstance().reloadResources();
            }
        }
    }

    private boolean isBlockEnabled(@NotNull Properties properties, String blockName)
    {
        return properties.containsKey(types[0]) && properties.get(types[0]).toString().equals(blockName)
               || properties.containsKey(types[1]) && properties.get(types[1]).toString().equals(blockName);
    }

    private void setBlockInPropertiesToAppropriateState(Properties properties, @NotNull CTMBlock ctmBlock)
    {
        if (ctmBlock.isEnabled())
        {
            if (ctmBlock.isTile)
            {
                properties.put(types[1], ctmBlock.blockName);
                properties.remove(types[3]);
            }
            else
            {
                properties.put(types[0], ctmBlock.blockName);
                properties.remove(types[2]);
            }
        }
        else
        {
            if (ctmBlock.isTile)
            {
                properties.put(types[3], ctmBlock.blockName);
                properties.remove(types[1]);
            }
            else
            {
                properties.put(types[2], ctmBlock.blockName);
                properties.remove(types[0]);
            }
        }
    }

    private void removeEmptyKeys(@NotNull Properties properties)
    {
        Arrays.stream(types)
              .filter(properties::containsKey)
              .filter(type -> properties.getProperty(type).isEmpty())
              .forEachOrdered(properties::remove);
    }
}