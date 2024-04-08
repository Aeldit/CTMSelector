package fr.aeldit.ctms.gui.entryTypes;

import fr.aeldit.ctms.textures.CTMSelector;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
 * The {@link #ctmBlocks} ArrayList contains a {@link CTMBlock} object
 * of each block with CTM properties found in the pack
 * <p>
 * The {@link #unsavedOptions} ArrayList contains a {@link CTMBlock}
 * object of each changed options
 * <p>
 * The second part contains methods to handle the activation /
 * deactivation of each {@code CTMBlock} in this pack
 */
public class CTMPack
{
    private final String name;
    private final boolean isFolder;
    private CTMSelector ctmSelector;
    //private Identifier identifier;

    private final ArrayList<CTMBlock> ctmBlocks = new ArrayList<>();
    private final ArrayList<CTMBlock> unsavedOptions = new ArrayList<>();

    public CTMPack(@NotNull String name, boolean isFolder)
    {
        this.name = name;
        this.isFolder = isFolder;

        /*String var10003 = Util.replaceInvalidChars("file/" + name, Identifier::isPathCharacterValid);
        this.identifier = new Identifier("minecraft", "pack/" + var10003 + "/" + Hashing.sha1().hashUnencodedChars
        ("file/" + name) + "/icon");*/
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

    public boolean hasCtmSelector()
    {
        return ctmSelector != null;
    }

    public void createCtmSelector(boolean isFolder)
    {
        this.ctmSelector = new CTMSelector(this.name, isFolder);
    }

    /*public Identifier getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(int icon_index)
    {
        String packPath = RESOURCE_PACKS_DIR + "/" + name;
        Identifier id = new Identifier("ctms", String.valueOf(icon_index));

        if (isFolder)
        {
            if (Files.exists(Path.of(packPath + "/pack.png")))
            {
                String var10003 = Util.replaceInvalidChars("file/" + name, Identifier::isPathCharacterValid);
                id = new Identifier("minecraft",
                        "pack/" + var10003 + "/" + Hashing.sha1().hashUnencodedChars("file/" + name) + "/icon"
                );

                /*try (InputStream stream = new FileInputStream(packPath + "/pack.png"))
                {
                    MinecraftClient.getInstance().getTextureManager().registerTexture(id, new
                    NativeImageBackedTexture(NativeImage.read(stream)));
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        else
        {
            /*try (ZipFile zipFile = new ZipFile(packPath))
            {
                for (FileHeader fileHeader : zipFile.getFileHeaders())
                {
                    if (fileHeader.toString().equals("pack.png"))
                    {
                        MinecraftClient.getInstance().getTextureManager().registerTexture(id,
                                new NativeImageBackedTexture(NativeImage.read(zipFile.getInputStream(fileHeader)))
                        );
                        break;
                    }
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        this.identifier = id;
    }*/

    //=========================================================================
    // CTMBlocks
    //=========================================================================
    public void addBlock(CTMBlock ctmBlock)
    {
        ctmBlocks.add(ctmBlock);

        if (ctmBlock.getControlsGroup() != null)
        {
            ctmBlock.getControlsGroup().addContainedBLock(ctmBlock);
        }
    }

    public void addAllBlocks(@NotNull ArrayList<CTMBlock> ctmBlockList)
    {
        ctmBlockList.forEach(this::addBlock);
    }

    public ArrayList<CTMBlock> getCtmBlocks()
    {
        return ctmBlocks;
    }

    public void toggle(CTMBlock block)
    {
        if (ctmBlocks.contains(block))
        {
            ctmBlocks.get(ctmBlocks.indexOf(block)).toggle();
        }
    }

    public void resetOptions()
    {
        ctmBlocks.forEach(ctmBlock -> ctmBlock.setEnabled(true));
    }

    public boolean isBlockEnabled(String blockName)
    {
        for (CTMBlock block : ctmBlocks)
        {
            if (block.getBlockName().equals(blockName))
            {
                return block.isEnabled();
            }
        }
        return false;
    }
}
