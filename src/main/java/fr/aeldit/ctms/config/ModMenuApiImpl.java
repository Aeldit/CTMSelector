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

package fr.aeldit.ctms.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.aeldit.cyanlib.lib.config.CyanLibConfigScreen;
import fr.aeldit.cyanlib.lib.config.CyanLibOptionsStorage;

import java.util.ArrayList;

import static fr.aeldit.ctms.util.Utils.CTMS_MODID;
import static fr.aeldit.ctms.util.Utils.TEXTURES;
import static fr.aeldit.cyanlib.lib.CyanLib.CONFIG_CLASS_INSTANCES;

public class ModMenuApiImpl implements ModMenuApi
{
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return parent -> new CyanLibConfigScreen(
                (CyanLibOptionsStorage) CONFIG_CLASS_INSTANCES.get(CTMS_MODID).get(0),
                parent,
                TEXTURES.getTextureOptions(), new ArrayList<>()
        );
    }
}
