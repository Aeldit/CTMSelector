package fr.aeldit.ctms.textures;

import fr.aeldit.ctms.textures.entryTypes.CTMBlock;
import fr.aeldit.ctms.textures.entryTypes.CTMPack;
import net.lingala.zip4j.ZipFile;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static fr.aeldit.ctms.Utils.CTM_PACKS;
import static fr.aeldit.ctms.Utils.RESOURCE_PACKS_DIR;

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
            if (file.isDirectory())
            {
                CTMPack ctmPack = new CTMPack(file);
                CTM_PACKS.add(ctmPack);
                for (Map.Entry<String, List<CTMBlock>> entry : getAllBlocks(file).entrySet())
                {
                    ctmPack.addAllBlocks(new ArrayList<>(entry.getValue()), entry.getKey());
                }
                System.out.println(ctmPack.getCTMBlocks());
            }
            else if (file.isFile() && file.getName().endsWith(".zip"))
            {
                try (ZipFile zipFile = new ZipFile(file))
                {
                    CTM_PACKS.add(new CTMPack(zipFile));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private @NotNull HashMap<String, List<CTMBlock>> getAllBlocks(@NotNull File file)
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

                    ctmBlocks.add(getCTMBlockFrom(properties, file.getParentFile(), namespace));
                }
            }
        }
        return ctmBlocks;
    }

    private @Nullable CTMBlock getCTMBlockFrom(
            @NotNull Properties properties, @NotNull File parentFile, String namespace
    )
    {
        Identifier identifier = getIdentifierFor(properties, parentFile, namespace);

        if (properties.containsKey(types[0]))
        {
            return new CTMBlock(properties.get(types[0]).toString(), identifier, true);
        }
        if (properties.containsKey(types[1]))
        {
            return new CTMBlock(properties.get(types[1]).toString(), identifier, true);
        }
        if (properties.containsKey(types[2]))
        {
            return new CTMBlock(properties.get(types[2]).toString(), identifier, false);
        }
        if (properties.containsKey(types[3]))
        {
            return new CTMBlock(properties.get(types[3]).toString(), identifier, false);
        }
        return null;
    }

    private Identifier getIdentifierFor(Properties properties, @NotNull File parentFile, String namespace)
    {
        File[] neighborFiles = parentFile.listFiles();
        Identifier identifier = new Identifier("unknown");
        if (neighborFiles != null)
        {
            int firstImage = Integer.parseInt(properties.get("tiles").toString().split("-")[0]);
            String pngFile = "%d.png".formatted(firstImage);
            if (Arrays.stream(neighborFiles).anyMatch(file -> pngFile.equals(file.getName())))
            {
                identifier = Arrays.stream(neighborFiles)
                                   .filter(file -> pngFile.equals(file.getName()))
                                   .findFirst()
                                   .map(file -> new Identifier(
                                           namespace,
                                           getIdentifierLikePathFrom(namespace, file.getPath())
                                   ))
                                   .orElse(identifier);
            }
        }
        return identifier;
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
}