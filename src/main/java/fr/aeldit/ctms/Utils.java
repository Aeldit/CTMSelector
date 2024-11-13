package fr.aeldit.ctms;

import fr.aeldit.ctms.textures.CTMPacks;
import fr.aeldit.ctms.textures.FilesHandling;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Utils
{
    public static final String CTMS_MODID = "ctms";
    public static final String CTM_PATH = "optifine/ctm/";

    public static CTMPacks CTM_PACKS;

    public static final Path RESOURCE_PACKS_DIR = FabricLoader.getInstance().getGameDir().resolve("resourcepacks");

    /**
     * @param packPath     The path to the zip file
     * @param headersBytes A map containing the fileHeaders as keys and the bytes to write in them as values
     */
    public static void writeBytesToZip(String packPath, @NotNull HashMap<String, byte[]> headersBytes)
    {
        // Mounts the zip file and adds files to it using the FileSystem
        // The bytes written in the files are the ones we obtain
        // from the properties files
        HashMap<String, String> env = new HashMap<>(1);
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
        }
    }

    /**
     * Takes the result of the string wanted in 'pretty' form split on its underscores
     *
     * @param s The split string list
     * @return A string containing each word separated by a space, and the first letter of each word uppercased
     */
    public static @NotNull String getPrettyString(String @NotNull [] s)
    {
        // Changes the lowercase and underscore separated string by replacing each '_' by a space
        // and by uppercasing the first letter of each word
        StringBuilder stringBuilder = new StringBuilder();
        int index = 0;

        for (String str : s)
        {
            if (str.length() < 2)
            {
                continue;
            }

            stringBuilder.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));

            if (index < s.length - 1)
            {
                stringBuilder.append(" ");
            }
            index++;
        }
        return stringBuilder.toString();
    }
}
