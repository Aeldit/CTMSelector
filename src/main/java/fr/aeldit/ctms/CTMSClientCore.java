package fr.aeldit.ctms;

import fr.aeldit.ctms.gui.CTMSScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.LoggerFactory;

import static fr.aeldit.ctms.Utils.CTMS_MODID;
import static fr.aeldit.ctms.Utils.TEXTURES_HANDLING;

public class CTMSClientCore implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        TEXTURES_HANDLING.load();

        /*for (CTMPack ctmPack : CTM_PACKS.getAvailableCTMPacks())
        {
            ctmPack.setIdentifier(CTM_PACKS.getIcons_index());
        }*/

        /*ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            for (CTMPack ctmPack : CTM_PACKS.getAvailableCTMPacks())
            {
                ctmPack.setIdentifier(CTM_PACKS.getIcons_index());
            }
        });*/

        KeyBinding packScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "cyanlib.keybindings.openScreen.config",
                InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_U,
                "cyanlib.keybindings.category"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (packScreenKey.wasPressed())
            {
                client.setScreen(new CTMSScreen(null));
            }
        });

        LoggerFactory.getLogger(CTMS_MODID).info("[CTMSelector] Successfully initialized");
    }
}
