package fr.aeldit.ctms;

import fr.aeldit.ctms.gui.CTMSScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static fr.aeldit.ctms.Utils.CTMS_LOGGER;
import static fr.aeldit.ctms.Utils.TEXTURES_HANDLING;

public class CTMSClientCore implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        TEXTURES_HANDLING.load(true);

        // Loads the icons pack when the client starts
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            if (MinecraftClient.getInstance().getResourcePackManager().getNames().contains("file/__CTMS_Icons__"))
            {
                MinecraftClient.getInstance().getResourcePackManager().enable("file/__CTMS_Icons__");
                MinecraftClient.getInstance().reloadResourcesConcurrently();
            }
        });

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

        CTMS_LOGGER.info("[CTMSelector] Successfully initialized");
    }
}
