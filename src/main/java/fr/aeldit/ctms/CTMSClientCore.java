/*
 * Copyright (c) 2023  -  Made by Aeldit
 *
 *              GNU LESSER GENERAL PUBLIC LICENSE
 *                  Version 3, 29 June 2007
 *
 *  Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 *  Everyone is permitted to copy and distribute verbatim copies
 *  of this license document, but changing it is not allowed.
 *
 *
 * This version of the GNU Lesser General Public License incorporates
 * the terms and conditions of version 3 of the GNU General Public
 * License, supplemented by the additional permissions listed in the LICENSE.txt file
 * in the repo of this mod (https://github.com/Aeldit/CTMSelector)
 */

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

import static fr.aeldit.ctms.util.Utils.*;

public class CTMSClientCore implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        TEXTURES_HANDLING.load();

        // Loads the icons pack when the client starts
        ClientLifecycleEvents.CLIENT_STARTED.register(client ->
                {
                    if (!CTM_PACKS.getAvailableCTMPacks().isEmpty())
                    {
                        MinecraftClient.getInstance().getResourcePackManager().enable("file/__CTMS_Icons__");
                    }
                }
        );

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
