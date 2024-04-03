package fr.aeldit.ctms.textures.controls;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record Controls(
        @SerializedName("type") ControlsTypes type, @SerializedName("block_group") String blockGroup,
        @SerializedName("button_title") String buttonTitle, @SerializedName("button_tooltip") String buttonTooltip,
        @SerializedName("properties_files") ArrayList<String> propertiesFilesPaths
)
{
    public @NotNull Set<String> getFilesOrOptionsNames()
    {
        if (propertiesFilesPaths == null)
        {
            return new HashSet<>(0);
        }

        Set<String> paths = new HashSet<>();

        propertiesFilesPaths.forEach(filePath -> {
                    StringBuilder translation = new StringBuilder();

                    for (String str : filePath.split("/")[filePath.split("/").length - 1].replace(".properties", "").split("_"))
                    {
                        translation.append(str.substring(0, 1).toUpperCase()).append(str.substring(1));
                        translation.append(" ");
                    }
                    paths.add(translation.substring(0, translation.length() - 1)); // Removes the space at the end of the string
                }
        );
        return paths;
    }
}
