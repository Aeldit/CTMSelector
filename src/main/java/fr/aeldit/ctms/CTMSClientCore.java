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
 * in the repo of this mod (https://github.com/Aeldit/Cyan)
 */

package fr.aeldit.ctms;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import static fr.aeldit.ctms.util.Utils.*;

public class CTMSClientCore implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        TEXTURES.init();
        CTMS_LIB_UTILS.init(CTMS_MODID, CTMS_OPTIONS_STORAGE, null);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CTMS_CONFIG_COMMANDS.register(dispatcher));
        CTMS_LOGGER.info("[CTMSelector] Successfully initialized");
    }
}
